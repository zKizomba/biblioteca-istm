# Padrões de desenho e refactoring

## Repository

`BookRepository`, `ReaderRepository` e `LoanRepository` definem contratos de acesso a dados. `InMemoryBookRepository`, `InMemoryReaderRepository` e `InMemoryLoanRepository` implementam esses contratos com mapas concorrentes. `LibraryService` depende dos contratos, e não dos mapas. O padrão isola a persistência e permite substituir o armazenamento sem reescrever os casos de uso.

## Factory

`LibraryFactory.createInMemoryLibrary()` constrói os três repositórios em memória e os injeta no serviço. A aplicação não precisa conhecer cada adaptador concreto. A Factory centraliza a composição e oferece um ponto único para trocar as implementações quando uma persistência durável for aprovada.

## Ciclo de refactoring protegido por testes

**Problema:** a inicialização da aplicação conhecia directamente os três repositórios concretos, espalhando detalhes de infraestrutura na camada de interface.

**Alteração:** a criação do serviço e dos repositórios foi movida para `LibraryFactory`; a aplicação passou a depender apenas do serviço.

**Métrica simples:** as referências da classe de interface às implementações concretas de repositório passaram de três para zero. A métrica pode ser confirmada pela leitura dos imports de `LibraryApplication` e `LibraryFactory`.

**Prova de comportamento:** `LibraryIntegrationTest` percorre registo, empréstimo, indisponibilidade, devolução e disponibilidade restaurada usando a Factory e os repositórios reais em memória. `LibraryServiceTest` cobre validações, prazo, atraso, bloqueio de novo empréstimo e suspensão; `LibraryWebControllerTest` verifica autenticação, privilégios, páginas e operações Web. Os resultados da nova execução ficam em `docs/technical-report.md` depois da validação final.

## Apresentação Web

`LibraryWebController` encaminha pedidos do navegador para `LibraryService`. Os templates Thymeleaf e o CSS estão separados da lógica, pelo que podemos alterar cores, navegação e campos das páginas sem modificar as regras. Spring Security exige sessão e separa os perfis Administrador e Bibliotecário. As contas desta versão são voláteis e servem para a experiência local.

## Sanção por atraso

`Loan` guarda a data limite de devolução. Um empréstimo activo vencido gera alerta e bloqueia novos empréstimos do mesmo leitor. Quando o exemplar é devolvido tarde, o serviço regista uma `Suspension` temporária em vez de cobrar multa em dinheiro. Os dias de empréstimo e de suspensão são configuração local, não uma política institucional aprovada.

## Decisões de validação

O código interno identifica unicamente cada exemplar físico. O ISBN, que pode ser partilhado por várias cópias da mesma edição, ainda não é armazenado e poderá ser acrescentado como metadado bibliográfico. O identificador e o e-mail identificam leitores; e-mails são comparados sem distinção entre maiúsculas e minúsculas. O serviço rejeita livro/leitor inexistente, livro indisponível, datas ausentes, devolução anterior ao empréstimo e devolução repetida.
