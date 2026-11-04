# Biblioteca ISTM

Aplicação Web local para gestão do acervo e da circulação da Biblioteca ISTM. Esta versão implementa o núcleo do Tema 2 e os fluxos de empréstimo, devolução, atraso e suspensão temporária, seguindo os diagramas de referência fornecidos.

## Executar localmente

Requisitos: Java 17 ou superior e Maven 3.9 ou superior.

```powershell
mvn spring-boot:run
```

Abra [http://localhost:8080](http://localhost:8080). Na primeira execução, o terminal mostra credenciais temporárias geradas para as contas iniciais `admin` e `bibliotecario`; copie-as para a página de início de sessão. As contas e os dados desta demonstração ficam na memória e são recriados quando a aplicação reinicia.

Para credenciais estáveis no computador local, defina segredos no ambiente antes de iniciar o Maven:

```powershell
$env:BIBLIOTECA_ADMIN_PASSWORD = 'substitua-por-uma-palavra-passe-segura'
$env:BIBLIOTECA_LIBRARIAN_PASSWORD = 'defina-ou-omita-para-gerar-uma-temporaria'
mvn spring-boot:run
```

Não use valores de demonstração num servidor institucional. Antes de disponibilizar o sistema, devem ser definidas credenciais, armazenamento persistente, cópias de segurança e uma política aprovada pela biblioteca.

## Regras configuráveis de circulação

Esta versão usa valores de demonstração para permitir experiências locais: prazo de empréstimo de 14 dias e suspensão de 7 dias após devolução em atraso. Os valores podem ser alterados com `LIBRARY_LOAN_DURATION_DAYS` e `LIBRARY_SUSPENSION_DAYS`. A instituição deve confirmar os prazos antes do uso real.

Não há pagamento nem cobrança monetária. Um livro devolvido depois do prazo gera uma sanção temporária de suspensão; enquanto existir atraso por resolver ou suspensão activa, o leitor não pode iniciar outro empréstimo. O painel apresenta alertas de atraso.

## Perfis e acesso

- **Administrador:** gere contas e atribui os perfis Administrador e Bibliotecário.
- **Bibliotecário:** gere exemplares, leitores, empréstimos, devoluções e consulta suspensões.
- As páginas de gestão requerem sessão autenticada e os formulários usam protecção CSRF.
- As palavras-passe são armazenadas em memória com BCrypt; a aplicação nunca as mostra na listagem.

## Verificação

```powershell
mvn clean verify
```

O comando compila, executa testes automatizados, gera o relatório JaCoCo e falha se a cobertura das regras de negócio ficar abaixo de 70%. O relatório HTML fica em `target/site/jacoco/index.html`.

## Estrutura

- `domain`: entidades, empréstimos e sanções de suspensão
- `service`: casos de uso e regras de negócio
- `repository`: contratos e adaptadores em memória
- `security`: autenticação, contas de colaboradores e codificação de palavras-passe
- `web`: rotas e controladores da interface
- `src/main/resources/templates`: páginas Thymeleaf
- `src/main/resources/static`: CSS e marca gráfica da Biblioteca ISTM

A marca gráfica actual é uma identidade própria do projecto, não o brasão oficial da instituição. A interface usa uma paleta académica de azul-marinho e dourado e ícones SVG locais. Os templates e estilos podem ser ajustados sem alterar as regras de negócio.

## Próximas etapas

- Confirmar durações, sanções, perfis e regras com a biblioteca.
- Acrescentar persistência durável e backup antes de dados reais.
- Evoluir os casos de uso ainda representados nos diagramas, como reservas, histórico do leitor e notificações externas.
- Depois da revisão local, criar/ligar o repositório no GitHub Desktop e confirmar a visibilidade antes de publicar.

## Documentação

- [Arquitectura e requisitos](docs/architecture.md)
- [Padrões e refactoring](docs/design-and-refactoring.md)
- [Backlog e processo de sprint](docs/product-backlog.md)
- [Relatório técnico](docs/technical-report.md)
