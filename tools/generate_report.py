from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfbase import pdfmetrics
from reportlab.platypus import Image
from reportlab.platypus import (
    BaseDocTemplate, Frame, PageTemplate, Paragraph, PageBreak, Spacer, Table,
    TableStyle, Flowable, KeepTogether,
    NextPageTemplate,
)


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "output" / "pdf" / "relatorio-tecnico-biblioteca-istm.pdf"
OUTPUT.parent.mkdir(parents=True, exist_ok=True)

FONT_REGULAR = Path("C:/Windows/Fonts/arial.ttf")
FONT_BOLD = Path("C:/Windows/Fonts/arialbd.ttf")
if FONT_REGULAR.exists() and FONT_BOLD.exists():
    pdfmetrics.registerFont(TTFont("Arial", str(FONT_REGULAR)))
    pdfmetrics.registerFont(TTFont("Arial-Bold", str(FONT_BOLD)))
    BODY_FONT, BOLD_FONT = "Arial", "Arial-Bold"
else:
    BODY_FONT, BOLD_FONT = "Helvetica", "Helvetica-Bold"

NAVY = colors.HexColor("#16324F")
BLUE = colors.HexColor("#1E5A7A")
PALE = colors.HexColor("#EAF1F5")
INK = colors.HexColor("#24313B")
MUTED = colors.HexColor("#64727C")
GREEN = colors.HexColor("#16805D")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="ReportTitle", parent=styles["Title"], fontName=BOLD_FONT,
                          fontSize=20, leading=25, textColor=NAVY, alignment=TA_LEFT,
                          spaceAfter=10))
styles.add(ParagraphStyle(name="H2x", parent=styles["Heading2"], fontName=BOLD_FONT,
                          fontSize=12, leading=15, textColor=BLUE, spaceBefore=5, spaceAfter=5))
styles.add(ParagraphStyle(name="Bodyx", parent=styles["BodyText"], fontName=BODY_FONT,
                          fontSize=9.2, leading=13.2, textColor=INK, spaceAfter=6))
styles.add(ParagraphStyle(name="Smallx", parent=styles["BodyText"], fontName=BODY_FONT,
                          fontSize=7.7, leading=10.2, textColor=INK, spaceAfter=2))
styles.add(ParagraphStyle(name="Cellx", parent=styles["BodyText"], fontName=BODY_FONT,
                          fontSize=7.5, leading=9.5, textColor=INK))
styles.add(ParagraphStyle(name="CellHeadx", parent=styles["BodyText"], fontName=BOLD_FONT,
                          fontSize=7.5, leading=9.5, textColor=colors.white))
styles.add(ParagraphStyle(name="Calloutx", parent=styles["BodyText"], fontName=BOLD_FONT,
                          fontSize=11, leading=15, textColor=GREEN, alignment=TA_CENTER,
                          spaceBefore=6, spaceAfter=8))
styles.add(ParagraphStyle(name="Refx", parent=styles["BodyText"], fontName=BODY_FONT,
                          fontSize=7.8, leading=10.5, leftIndent=14, firstLineIndent=-14,
                          textColor=INK, spaceAfter=5))


def P(text, style="Bodyx"):
    return Paragraph(text, styles[style])


def table(rows, widths, header=True, font=7.5):
    converted = []
    for ri, row in enumerate(rows):
        style = "CellHeadx" if header and ri == 0 else "Cellx"
        converted.append([x if isinstance(x, Flowable) else P(str(x), style) for x in row])
    t = Table(converted, colWidths=widths, repeatRows=1 if header else 0, hAlign="LEFT")
    commands = [
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
        ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#C7D2DA")),
    ]
    if header:
        commands += [("BACKGROUND", (0, 0), (-1, 0), NAVY)]
        if len(converted) > 1:
            commands += [("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, PALE])]
    t.setStyle(TableStyle(commands))
    return t


class ArchitectureDiagram(Flowable):
    def __init__(self, width=500, height=125):
        super().__init__()
        self.width, self.height = width, height

    def draw(self):
        c = self.canv
        boxes = [
            (8, 74, 90, 36, "Interface Web", "MVC / Thymeleaf"),
            (111, 74, 92, 36, "Segurança", "Login / perfis"),
            (216, 74, 100, 36, "LibraryService", "Casos de uso"),
            (337, 74, 100, 36, "Domínio", "Regras e estados"),
            (111, 12, 239, 36, "Interfaces Repository", "Contratos de persistência"),
            (374, 12, 119, 36, "Memória", "Adaptadores"),
        ]
        for x, y, w, h, title, sub in boxes:
            c.setFillColor(PALE if title != "LibraryService" else NAVY)
            c.setStrokeColor(BLUE)
            c.roundRect(x, y, w, h, 5, fill=1, stroke=1)
            c.setFillColor(colors.white if title == "LibraryService" else NAVY)
            c.setFont(BOLD_FONT, 8)
            c.drawCentredString(x + w / 2, y + 22, title)
            c.setFillColor(colors.white if title == "LibraryService" else MUTED)
            c.setFont(BODY_FONT, 6.8)
            c.drawCentredString(x + w / 2, y + 9, sub)
        c.setStrokeColor(BLUE)
        c.setLineWidth(1.1)
        c.line(98, 92, 111, 92)
        c.line(203, 92, 216, 92)
        c.line(316, 92, 337, 92)
        c.line(266, 74, 266, 49)
        c.line(350, 30, 374, 30)
        c.setFont(BODY_FONT, 6.5)
        c.setFillColor(MUTED)
        c.drawString(352, 35, "implementa")


def draw_cover(canvas, doc):
    w, h = A4
    canvas.saveState()
    canvas.setFillColor(NAVY)
    canvas.rect(0, 0, w, h, fill=1, stroke=0)
    canvas.setFillColor(BLUE)
    canvas.rect(0, 0, 15 * mm, h, fill=1, stroke=0)
    canvas.setFillColor(colors.HexColor("#8FB5C8"))
    canvas.circle(w - 24 * mm, h - 25 * mm, 16 * mm, fill=1, stroke=0)
    canvas.setFillColor(colors.white)
    canvas.setFont(BODY_FONT, 11)
    canvas.drawString(29 * mm, h - 35 * mm, "INSTITUTO SUPERIOR TÉCNICO MILITAR")
    canvas.setFillColor(colors.HexColor("#C8D7E0"))
    canvas.setFont(BODY_FONT, 9)
    canvas.drawString(29 * mm, h - 43 * mm, "DEPARTAMENTO DE ENGENHARIA INFORMÁTICA")
    canvas.setFillColor(colors.white)
    canvas.setFont(BOLD_FONT, 27)
    canvas.drawString(29 * mm, h - 93 * mm, "BIBLIOTECA ISTM")
    canvas.setFont(BODY_FONT, 15)
    canvas.drawString(29 * mm, h - 106 * mm, "Relatório técnico do mini-projecto")
    canvas.setFillColor(colors.HexColor("#C8D7E0"))
    canvas.setFont(BODY_FONT, 11)
    canvas.drawString(29 * mm, h - 121 * mm, "Tema 2 - Sistema de Gestão de Biblioteca")
    canvas.setStrokeColor(colors.HexColor("#8FB5C8"))
    canvas.setLineWidth(1)
    canvas.line(29 * mm, h - 139 * mm, w - 25 * mm, h - 139 * mm)
    canvas.setFillColor(colors.white)
    canvas.setFont(BODY_FONT, 10)
    canvas.drawString(29 * mm, h - 157 * mm, "Disciplina: Tópicos Complementares")
    canvas.drawString(29 * mm, h - 166 * mm, "Módulo: Engenharia de Software Avançada")
    canvas.drawString(29 * mm, h - 175 * mm, "Ano lectivo: 2026/2027")
    canvas.drawString(29 * mm, h - 184 * mm, "Grupo 2: Sistema de Gestão de Biblioteca (4 estudantes)")
    canvas.drawString(29 * mm, h - 193 * mm, "Membros: preencher nomes | Scrum Master: designar")
    canvas.setFillColor(colors.HexColor("#C8D7E0"))
    canvas.setFont(BODY_FONT, 9)
    canvas.drawString(29 * mm, 23 * mm, "Luanda | 4 de Novembro de 2026")
    canvas.restoreState()


def draw_page(canvas, doc):
    canvas.saveState()
    w, h = A4
    canvas.setStrokeColor(colors.HexColor("#D6E0E6"))
    canvas.line(18 * mm, h - 17 * mm, w - 18 * mm, h - 17 * mm)
    canvas.setFillColor(MUTED)
    canvas.setFont(BODY_FONT, 7.5)
    canvas.drawString(18 * mm, h - 13 * mm, "BIBLIOTECA ISTM | MINI-PROJECTO - TEMA 2")
    canvas.drawRightString(w - 18 * mm, 12 * mm, f"Página {doc.page - 1}")
    canvas.restoreState()


class ReportDoc(BaseDocTemplate):
    def __init__(self, filename):
        super().__init__(filename, pagesize=A4, leftMargin=19 * mm, rightMargin=19 * mm,
                         topMargin=24 * mm, bottomMargin=19 * mm, title="Biblioteca ISTM - Relatório Técnico")
        frame = Frame(self.leftMargin, self.bottomMargin, self.width, self.height,
                      id="normal", leftPadding=0, rightPadding=0, topPadding=0, bottomPadding=0)
        self.addPageTemplates([
            PageTemplate(id="Cover", frames=frame, onPage=draw_cover, pagesize=A4),
            PageTemplate(id="Body", frames=frame, onPage=draw_page, pagesize=A4),
        ])


story = []
story.append(Spacer(1, 6))
story.append(NextPageTemplate("Body"))
story.append(PageBreak())

# 1. Problem and scope
story += [P("1. Introdução e problema", "ReportTitle"),
          P("O mini-projecto aplica práticas de Engenharia de Software Avançada a um sistema de gestão de biblioteca. O problema escolhido no Tema 2 é acompanhar o catálogo e a circulação: identificar livros e leitores, registar empréstimos e devoluções e manter a disponibilidade coerente com as operações realizadas."),
          P("Uma folha de registo ou uma aplicação sem validações pode permitir duplicados, emprestar um exemplar já requisitado ou fechar duas vezes a mesma devolução. A solução deve concentrar as regras num serviço de negócio, validar os dados antes de os guardar e demonstrar o comportamento com testes automatizados."),
          P("Objectivo geral", "H2x"),
          P("Construir uma aplicação Java organizada em camadas que registe livros e leitores, controle empréstimos e devoluções e verifique as regras de negócio com JUnit 5, Maven e JaCoCo. Um workflow GitHub Actions foi preparado para executar a mesma validação de forma contínua quando o repositório remoto for criado."),
          P("Âmbito entregue", "H2x"),
          P("A versão actual é Web e permite gerir exemplares, leitores, empréstimos, devoluções, alertas e suspensões. O acesso exige login com perfil Administrador ou Bibliotecário. O armazenamento é intencionalmente em memória nesta demonstração; os dados e contas criadas desaparecem ao reiniciar o processo."),
          P("Critérios assumidos", "H2x"),
          P("Um código interno identifica cada exemplar físico; o ISBN bibliográfico poderá ser partilhado por várias cópias e ainda não é armazenado. Identificador e endereço de correio electrónico identificam um leitor. Um empréstimo é activo até a data de devolução ser gravada. O leitor tem de existir e o exemplar tem de estar disponível. A devolução não pode anteceder o empréstimo. Prazos, renovações, multas, reservas e limites de requisições não foram definidos sem validação institucional."),
          P("Este relatório descreve a implementação local verificada em 4 de Novembro de 2026. Identificação do grupo e papéis Scrum devem ser preenchidos com informação real antes da entrega académica.")]

# 2. Requirements and backlog
story += [PageBreak(), P("2. Requisitos e Product Backlog", "ReportTitle"),
          P("Os requisitos funcionais foram derivados do Tema 2 do enunciado. O backlog abaixo traduz os fluxos principais em histórias verificáveis; os critérios de aceitação foram exercitados na aplicação e nos testes."),
          table([
              ["ID", "História de utilizador", "Critério de aceitação"],
              ["US01", "Como bibliotecário, quero registar exemplares.", "Código interno, título e autor obrigatórios; código duplicado rejeitado."],
              ["US02", "Como bibliotecário, quero consultar exemplares.", "Pesquisa por código interno e lista mostram dados e disponibilidade."],
              ["US03", "Como bibliotecário, quero registar leitores.", "Campos obrigatórios; e-mail validado e duplicados rejeitados."],
              ["US04", "Como bibliotecário, quero consultar leitores.", "Pesquisa por ID e lista de leitores registados."],
              ["US05", "Como bibliotecário, quero emprestar livro disponível.", "Livro e leitor existentes; empréstimo activo e estado actualizado."],
              ["US06", "Como bibliotecário, quero impedir empréstimos inválidos.", "Livro indisponível e leitor desconhecido não criam empréstimo."],
              ["US07", "Como bibliotecário, quero registar devoluções.", "Só empréstimo activo; data válida; disponibilidade restaurada."],
              ["US08", "Como bibliotecário, quero consultar a circulação.", "Lista mostra código, livro, leitor e estado/data da devolução."],
              ["US09", "Como equipa, queremos evitar regressões.", "Maven executa testes; JaCoCo falha abaixo de 70% na lógica."],
              ["US10", "Como equipa, queremos validar cada integração.", "Workflow verifica pull requests e push para main; publica artefactos."],
          ], [14 * mm, 61 * mm, 84 * mm]),
          Spacer(1, 8), P("Requisitos não funcionais", "H2x"),
          P("A solução usa Java 17 e Maven, separa domínio, aplicação e acesso a dados, define interfaces para persistência substituível, executa testes sem intervenção manual e produz um relatório de cobertura. A arquitectura foi escolhida para favorecer manutenibilidade e testabilidade num problema de pequena escala."),
          P("A execução actual cumpre os fluxos de registo, consulta, disponibilidade, empréstimo e devolução. O backlog completo, incluindo os critérios e Definition of Done, está em docs/product-backlog.md.", "Smallx")]

# 3. Architecture
story += [PageBreak(), P("3. Arquitectura e justificação", "ReportTitle"),
          P("Foi adoptada uma arquitectura Web em camadas. Thymeleaf e Spring MVC recebem entradas do navegador e apresentam resultados. Spring Security protege as rotas e separa os perfis Administrador e Bibliotecário. LibraryService implementa os casos de uso; Book, Reader, Loan e Suspension representam o domínio. Interfaces de repositório isolam o armazenamento em memória."),
          ArchitectureDiagram(500, 125), Spacer(1, 3),
          table([
              ["Estilo", "Manutenibilidade", "Testabilidade", "Adequação ao projecto"],
              ["Camadas", "Responsabilidades separadas; interface e armazenamento podem mudar isoladamente.", "Serviço recebe repositórios por interface; testes rápidos sem serviços externos.", "Adequado ao âmbito reduzido e aos requisitos de separação."],
              ["Microserviços", "Módulos independentes, mas exigem contratos e operação distribuída.", "Testes de integração entre serviços aumentam a infraestrutura necessária.", "Complexidade desproporcional para um catálogo e circulação locais."],
              ["Orientada a eventos", "Desacopla consumidores assíncronos.", "Exige testar publicação, entrega e ordenação de eventos.", "Sem benefício claro num fluxo síncrono de empréstimo/devolução."],
          ], [27 * mm, 43 * mm, 43 * mm, 46 * mm]),
          Spacer(1, 8), P("A arquitectura em camadas satisfaz manutenibilidade e testabilidade com baixo custo operacional. O desenho evita dependência do serviço em relação às classes concretas de armazenamento. Quando a instituição escolher uma base de dados, basta acrescentar adaptadores para os contratos existentes e revalidar as regras através da suite.")]

# 4. Implementation, patterns, refactoring
story += [PageBreak(), P("4. Implementação, padrões e refactoring", "ReportTitle"),
          P("A aplicação segue a estrutura convencional de Maven. domain contém Book, Reader, Loan e Suspension; service implementa os casos de uso; repository define os contratos e os adaptadores em memória. LibraryWebController liga páginas Thymeleaf ao serviço; o pacote security trata login e perfis. LibraryFactory compõe as dependências."),
          P("A marca actual é um logótipo gráfico próprio do projecto, com livro aberto e iniciais ISTM. Foi adoptada uma paleta académica azul-marinho e dourada; ícones SVG locais evitam dependências externas. O activo não é apresentado como brasão oficial da instituição."),
          P("A interface Web usa Spring MVC e Thymeleaf para gerar páginas HTML no servidor. As páginas e estilos ficam separados em templates e CSS, permitindo mudar navegação, campos e aspecto visual sem alterar as regras de negócio. O servidor local pode ser aberto em http://localhost:8080."),
          P("Padrão Repository", "H2x"),
          P("Os contratos de repositório abstraem operações de gravação e consulta. Os adaptadores em memória implementam esses contratos com mapas. LibraryService depende das interfaces, o que permite substituir a estratégia de persistência sem alterar os casos de uso. Esta escolha cumpre o padrão Repository pedido e cria uma fronteira testável para o acesso aos dados."),
          P("Padrão Factory", "H2x"),
          P("LibraryFactory.createInMemoryLibrary() instancia os adaptadores e injecta-os no serviço. A composição fica centralizada e oferece um ponto de substituição quando for escolhida uma base de dados."),
          P("Ciclo de refactoring", "H2x"),
          P("Problema detectado: a inicialização da interface criava directamente cada repositório, ligando apresentação a infraestrutura. Alteração: a composição foi extraída para LibraryFactory. Métrica simples: referências da classe de aplicação às implementações concretas passaram de três para zero. A prova de regressão é a suite de serviço, Web e integração; o teste de integração completa o ciclo de empréstimo e devolução."),
          P("Validação adicional", "H2x"),
          P("A operação de empréstimo e devolução é sincronizada na instância do serviço para evitar duas operações simultâneas na mesma instância. Isto não substitui uma transacção numa base de dados partilhada; a solução actual é local e em memória.")]

# 5. Testing
story += [PageBreak(), P("5. Estratégia e casos de teste", "ReportTitle"),
          P("JUnit Jupiter executa os testes pelo ciclo Maven. A suite combina testes unitários das entidades e regras de LibraryService com testes de integração que usam repositórios reais em memória e testes MVC das páginas, operações, autenticação e privilégios."),
          table([
              ["Grupo de cenários", "Casos cobertos", "Resultado esperado"],
              ["Entidades", "Campos nulos/vazios; e-mail inválido; igualdade por identificador; estados repetidos.", "Dados inválidos rejeitados; estados e igualdade consistentes."],
              ["Catálogo e leitores", "Registo, listagem, códigos internos/ID/e-mail duplicados e consultas inexistentes.", "Registos válidos guardados; duplicados e ausentes tratados."],
              ["Empréstimo", "Livro disponível/indisponível; leitor desconhecido; datas ausentes; empréstimo activo duplicado.", "Só operações permitidas criam empréstimo e alteram disponibilidade."],
              ["Devolução", "Devolução válida, data anterior, empréstimo inexistente ou já fechado.", "Estado fecha uma vez e disponibilidade regressa; erros não alteram estado."],
              ["Integração", "Factory, serviço e três repositórios num ciclo completo.", "Dados mantêm coerência entre serviço e adaptadores."],
              ["Consola", "Opções do menu, registos, consulta, empréstimo, devolução e mensagens de erro.", "Entrada textual invoca operações e apresenta resultados legíveis."],
          ], [30 * mm, 71 * mm, 58 * mm]),
          Spacer(1, 10), P("A execução final incluiu 30 testes automatizados: cinco de domínio, um de integração, dois de segurança, catorze do serviço e oito Web. Os testes incluem prazos, atraso, suspensão, bloqueio de novo empréstimo, páginas, pesquisa, CSRF e autorização por perfil.")]

# 6. Results and coverage
story += [PageBreak(), P("6. Resultados de testes e cobertura", "ReportTitle"),
          P("A validação foi executada localmente em 4 de Novembro de 2026 com `mvn verify`, repositório Maven no projecto, Java 25 e compilação configurada para Java 17. A execução terminou com BUILD SUCCESS. Os 30 testes passaram, sem falhas, erros ou ignorados."),
          P("JaCoCo 0.8.16 analisou 18 classes de produção. A cobertura foi 280/299 linhas (93,6%) e 107/138 ramos (77,5%). O pacote de negócio superou o limiar configurado de 70% de linhas.", "Calloutx"),
          table([
              ["Área", "Linhas", "Ramos", "Leitura"],
              ["Total do projecto", "280/299 - 93,6%", "107/138 - 77,5%", "Limiar de 70% nas regras de negócio superado."],
              ["LibraryService", "45/47 - 95,7%", "30/36 - 83,3%", "Regras, prazos e suspensões exercitados."],
              ["Domínio", "66/69 - 95,7%", "36/38 - 94,7%", "Validações e transições de estado cobertas."],
              ["Interface Web", "86/95 - 90,5%", "13/22 - 59,1%", "Páginas, fluxos e autorização testados com MVC."],
              ["Segurança", "54/57 - 94,7%", "22/36 - 61,1%", "Login, perfis e gestão de contas exercitados."],
              ["Repositórios em memória", "24/24 - 100%", "6/6 - 100%", "Gravação, consulta e adaptadores cobertos."],
          ], [43 * mm, 34 * mm, 32 * mm, 50 * mm]),
          Spacer(1, 8), P("A cobertura dos pacotes Web e security ainda pode crescer, sobretudo em ramificações de erro e combinações de perfis. JaCoCo é uma métrica diagnóstica, não prova de correcção; os testes verificam regras de circulação, restrições e alguns fluxos de autenticação."),
          P("O limiar automatizado no POM é 70% de linhas no pacote de negócio. O relatório local ficou acima do limiar em todo o projecto; a cobertura de ramos é apresentada como diagnóstico adicional, não como garantia isolada de correcção.")]

# 7. CI pipeline
story += [PageBreak(), P("7. Pipeline CI/CD", "ReportTitle"),
          P("O workflow .github/workflows/ci.yml está preparado para executar em push para main e em pull requests dirigidas a main. Usa runner Ubuntu, configura Temurin Java 17 e executa `mvn --batch-mode --no-transfer-progress clean verify`. O passo de verificação inclui compilação, testes JUnit, relatório JaCoCo e bloqueio abaixo de 70% de linhas nas classes de negócio."),
          P("Artefactos definidos", "H2x"),
          P("O workflow disponibiliza o JAR e a pasta do relatório HTML JaCoCo como artefactos da execução. O upload usa `if: always()` para preservar os relatórios quando existe uma falha, se os ficheiros tiverem sido produzidos. A cache Maven é configurada através de setup-java."),
          P("Evidência local", "H2x"),
          P("A mesma etapa `mvn clean verify` foi executada no ambiente local. Evidências disponíveis localmente: relatório HTML JaCoCo em target/site/jacoco/index.html, ficheiro XML em target/site/jacoco/jacoco.xml e JAR em target/biblioteca-istm-1.0.0-SNAPSHOT.jar."),
          P("Estado da integração contínua", "H2x"),
          P("O workflow está escrito e pronto para integração, mas ainda não foi executado no GitHub: o utilizador definiu que as melhorias e experiências locais antecedem a criação/envio do repositório. Portanto, não há execução hospedada a capturar nem se afirma que o pipeline remoto já passou. Ao preparar o repositório, a primeira execução deverá confirmar o JDK, dependências, permissões de artefactos e resultado do limiar JaCoCo."),
          P("Uma falha de compilação ou de teste termina `verify` com resultado não zero e deixa a execução vermelha. O passo de upload, por estar configurado para sempre, tenta conservar o JAR e o relatório para diagnóstico.")]

# 8. Process, retrospective, metrics
story += [PageBreak(), P("8. Processo de desenvolvimento", "ReportTitle"),
          P("O enunciado define um sprint único e pede papéis, quadro, daily Scrum, Definition of Done, revisão por Pull Request e retrospectiva. Esta cópia local já documenta histórias, critérios e Definition of Done em docs/product-backlog.md. A identidade dos membros e a atribuição real dos papéis não foram fornecidas e permanecem por preencher."),
          table([
              ["Papel / actividade", "Estado factual"],
              ["Product Owner", "Docente, conforme o enunciado."],
              ["Scrum Master e Developers", "A designar/preencher pelos membros reais do grupo."],
              ["Quadro e daily Scrum", "Não iniciados localmente; registar actualizações reais quando a equipa trabalhar no sprint."],
              ["Definition of Done", "Compilação, testes, pipeline verde e revisão por outro membro, conforme o enunciado."],
          ], [48 * mm, 111 * mm]),
          P("Retrospectiva técnica local", "H2x"),
          P("Nesta fase local, correu bem separar regras, domínio e armazenamento desde o primeiro protótipo e verificar as operações com testes pequenos. A primeira versão ligava a inicialização da interface aos repositórios concretos; a Factory removeu essa dependência e o teste de integração confirmou que o ciclo de empréstimo continuou a funcionar. A análise de cobertura também revelou ramos não exercitados no domínio e no menu. Foram acrescentados cenários de dados inválidos, transições repetidas, consultas e interacção de consola até a suite cobrir todas as linhas e ramos. A revisão do modelo identificou ainda que ISBN pode ser partilhado por várias cópias, pelo que o exemplar passou a usar código interno único. Esta iteração mostrou que cobertura alta só é útil quando os testes verificam regras e estados relevantes. No próximo ciclo, o grupo deve confirmar com a biblioteca os prazos, renovações, exemplares e perfis de operador antes de escolher persistência durável. Esta retrospectiva descreve o trabalho local e não substitui o registo real do sprint da equipa."),
          P("Métricas exigidas", "H2x"),
          table([
              ["Métrica", "Resultado nesta fase"],
              ["Tempo médio de fecho de Pull Requests", "Não disponível: nenhuma Pull Request foi criada."],
              ["Execuções do pipeline remoto", "0: workflow ainda não executado no GitHub."],
              ["Percentagem de pipelines falhados", "Não calculável sem execuções."],
          ], [74 * mm, 85 * mm]),
          P("Não se atribuem valores estimados a métricas que dependem do histórico remoto. Depois da integração no GitHub, os resultados devem ser extraídos do repositório e acrescentados ao relatório.")]

# 9. Conclusion and references
story += [PageBreak(), P("9. Conclusão, limitações e referências", "ReportTitle"),
          P("O projecto implementa o núcleo do Tema 2 através de uma aplicação exclusivamente Web, com login, perfis, catálogo, circulação, alertas e suspensão em caso de atraso. A execução local passou 30 testes e obteve 93,6% de cobertura de linhas total; as regras de negócio superaram o limiar JaCoCo de 70%. O workflow CI está preparado para validação futura no GitHub."),
          P("Limitações e evolução", "H2x"),
          P("A persistência é temporária; utilizadores e empréstimos desaparecem ao reiniciar. Os valores de 14 dias para empréstimo e 7 dias para suspensão são demonstrativos e configuráveis. A instituição deve confirmar a política de empréstimo e sanções, definir reservas, notificações e login de leitores, escolher armazenamento durável e backup. As experiências locais precedem a publicação no GitHub."),
          P("Referências", "H2x"),
          P("Apache Software Foundation. (n.d.). <i>Maven getting started guide</i>. https://maven.apache.org/guides/getting-started/index.html", "Refx"),
          P("GitHub. (n.d.). <i>Building and testing Java with Maven</i>. https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven", "Refx"),
          P("Instituto Superior Técnico Militar. (2026). <i>Enunciado do mini-projecto em grupo: Tópicos Complementares, módulo Engenharia de Software Avançada</i>. Documento fornecido na disciplina.", "Refx"),
          P("JUnit Team. (2024). <i>JUnit 5.11.4 user guide</i>. https://docs.junit.org/5.11.4/user-guide/junit-user-guide-5.11.4.pdf", "Refx"),
          P("JaCoCo Team. (2026). <i>JaCoCo 0.8.16: Maven plug-in</i>. https://www.jacoco.org/jacoco/trunk/doc/maven.html", "Refx"),
          P("Oracle. (n.d.). <i>Java Platform, Standard Edition 17 documentation</i>. https://docs.oracle.com/en/java/javase/17/", "Refx"),
          Spacer(1, 6), P("Anexo digital: o código, o workflow, os testes e o relatório JaCoCo acompanham esta versão local do projecto.", "Smallx")]


doc = ReportDoc(str(OUTPUT))
doc.build(story)
print(f"PDF criado: {OUTPUT}")
