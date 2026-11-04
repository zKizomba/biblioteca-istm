# Product backlog e sprint

## Histórias de utilizador

| ID | História | Critérios de aceitação |
| --- | --- | --- |
| US01 | Como bibliotecário, quero registar um exemplar para o disponibilizar no catálogo. | Código interno, título e autor obrigatórios; código duplicado rejeitado. |
| US02 | Como bibliotecário, quero consultar exemplares para localizar um título. | Consulta por código interno e listagem alfabética mostram dados e disponibilidade. |
| US03 | Como bibliotecário, quero registar um leitor para identificar quem requisita livros. | Identificador, nome e e-mail obrigatórios; e-mail válido e sem duplicados. |
| US04 | Como bibliotecário, quero consultar leitores para confirmar o registo. | Pesquisa por identificador e listagem de leitores disponíveis. |
| US05 | Como bibliotecário, quero emprestar um livro disponível a um leitor registado. | Livro e leitor existentes; livro disponível; empréstimo activo criado e disponibilidade alterada. |
| US06 | Como bibliotecário, quero rejeitar um empréstimo incompatível. | Livro indisponível, leitor desconhecido ou dados inválidos não criam empréstimo. |
| US07 | Como bibliotecário, quero registar a devolução para voltar a disponibilizar o livro. | Só empréstimo activo; data não anterior à saída; livro fica disponível. |
| US08 | Como bibliotecário, quero consultar empréstimos para acompanhar a circulação. | Listagem indica código, livro, leitor, data de saída e estado/data de devolução. |
| US09 | Como equipa, queremos executar testes automaticamente para detectar regressões. | Maven executa testes unitários e integração; JaCoCo mede o pacote de negócio e impõe pelo menos 70%. |
| US10 | Como equipa, queremos validar alterações continuamente. | GitHub Actions executa `mvn clean verify` em pull requests e em push para `main`; guarda JAR e cobertura como artefactos. |
| US11 | Como colaborador, quero iniciar sessão para proteger os registos da biblioteca. | Só sessões autenticadas acedem ao painel e aos formulários; logout termina a sessão; formulários têm protecção CSRF. |
| US12 | Como administrador, quero criar contas e atribuir perfis. | Administrador gere contas; Bibliotecário não acede à gestão de contas; palavras-passe são codificadas com BCrypt. |
| US13 | Como bibliotecário, quero ver empréstimos vencidos e o tempo de atraso. | Prazo é calculado no empréstimo; painel e página de suspensões mostram alertas para operações activas vencidas. |
| US14 | Como bibliotecário, quero aplicar a sanção definida pelo professor após atraso. | Devolução tardia regista suspensão temporária; suspensão activa e empréstimo vencido bloqueiam novos empréstimos; não há cobrança de dinheiro. |
| US15 | Como bibliotecário, quero pesquisar o catálogo e leitores. | A pesquisa filtra por código/título/autor e por identificador/nome/e-mail. |
| US16 | Como leitor, quero reservar exemplares e consultar o meu histórico. | A implementar depois de validar autenticação de leitores e política de reservas com a biblioteca. |
| US17 | Como biblioteca, queremos enviar alertas por e-mail ou outro canal. | A implementar depois da confirmação do canal, destinatários e consentimento institucional. |

## Sprint e responsabilidades

O enunciado prevê um sprint único. Registem aqui os nomes reais antes da entrega: Product Owner (docente): **a confirmar**; Scrum Master: **a designar**; Developers: **a preencher com os membros do grupo**. Não foram inventados nomes ou contribuições.

### Definition of Done

- Código compila com Java 17 e Maven.
- Testes unitários e de integração passam.
- Cobertura das linhas no pacote de negócio é pelo menos 70%.
- A alteração foi revista por outro membro quando houver equipa/repositório.
- Pipeline fica verde antes da integração na branch principal.
- README e documentação reflectem o comportamento realmente implementado.
- Prazos e sanções estão confirmados pela instituição antes de produção.

### Daily Scrum assíncrono

Usar um registo diário com três campos: **Feito**, **A seguir**, **Impedimentos**. Inserir actualizações reais durante o sprint; não preencher retrospectivamente com dados inventados.

### Retrospectiva e métricas

Preencher após a execução real do sprint e da actividade Git: o que correu bem, o que melhorar e uma acção para o próximo ciclo. Métricas exigidas pelo enunciado: tempo médio de fecho de pull requests, número de execuções do pipeline e percentagem de execuções falhadas. Estes valores dependem da actividade real no GitHub e ficam pendentes até essa fase; não são estimados neste projecto local.
