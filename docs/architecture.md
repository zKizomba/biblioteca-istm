# Arquitectura e requisitos

## Problema e âmbito

A Biblioteca ISTM precisa de registar livros e leitores e controlar a circulação dos livros. Esta versão implementa o catálogo, o registo de leitores e o ciclo de empréstimo e devolução. As regras institucionais de prazo, renovação e limite de empréstimos ainda carecem de decisão.

## Arquitectura em camadas

```mermaid
flowchart TD
    WEB[Interface Web<br/>Spring MVC + Thymeleaf]
    SEC[Spring Security<br/>Login e perfis]
    APP[Serviço LibraryService]
    DOM[Domínio Book, Reader, Loan]
    PORTS[Interfaces BookRepository, ReaderRepository, LoanRepository]
    ADAPTERS[Repositórios em memória]
    SEC --> WEB
    WEB --> APP
    APP --> DOM
    APP --> PORTS
    ADAPTERS -. implementam .-> PORTS
```

O navegador apresenta páginas HTML e envia formulários ao controlador Spring MVC. Spring Security exige autenticação e aplica os perfis Administrador e Bibliotecário antes de o controlador chamar `LibraryService`. Thymeleaf gera as páginas no servidor e CSS define o aspecto responsivo. O serviço executa os casos de uso e valida regras de empréstimo; as entidades protegem as suas invariantes. Os contratos Repository isolam o armazenamento e os adaptadores actuais guardam registos em mapas em memória. `LibraryFactory` compõe esses adaptadores.

## Casos de uso (alinhamento com os diagramas de referência)

```mermaid
flowchart LR
    Leitor((Leitor))
    Bibliotecario((Bibliotecário))
    Admin((Administrador))
    subgraph Sistema[Biblioteca ISTM]
      Pesquisar[Pesquisar acervo]
      Historico[Consultar empréstimos]
      Reservar[Reservar exemplar · planeado]
      Registar[Gerir exemplares e leitores]
      Emprestar[Registar empréstimo]
      Devolver[Registar devolução]
      Prazos[Verificar prazo e pendências]
      Suspender[Registar suspensão temporária]
      Alertar[Mostrar alertas de atraso]
      Contas[Gerir contas e privilégios]
    end
    Leitor --> Pesquisar
    Leitor --> Historico
    Leitor -. próxima fase .-> Reservar
    Bibliotecario --> Registar
    Bibliotecario --> Emprestar
    Bibliotecario --> Devolver
    Bibliotecario --> Alertar
    Admin --> Contas
    Emprestar --> Prazos
    Devolver --> Prazos
    Prazos --> Suspender
    Prazos --> Alertar
```

As contas existentes são de colaboradores. Pesquisa por leitor autenticado, histórico individual e reservas ficam planeados, porque exigem credenciais de leitores e regras de fila ainda não definidas. Alertas aparecem no sistema; envio externo de notificações não está ligado. Pagamentos e multas monetárias foram excluídos conforme a orientação dada em aula.

## Sequência do empréstimo e atraso

```mermaid
sequenceDiagram
    actor Bibliotecario
    participant Web as Interface Web
    participant Service as LibraryService
    participant Reader as Leitor
    participant Book as Exemplar
    Bibliotecario->>Web: seleccionar leitor e exemplar
    Web->>Service: emprestar(código, leitor, data)
    Service->>Reader: verificar existência e suspensão
    Service->>Service: verificar empréstimos vencidos
    Service->>Book: verificar disponibilidade
    alt leitor regular e exemplar disponível
        Service->>Service: criar empréstimo com prazo configurado
        Service-->>Web: confirmação e data limite
    else pendência, suspensão ou exemplar indisponível
        Service-->>Web: recusar e apresentar motivo
    end
    Note over Web,Service: Empréstimos vencidos aparecem como alerta no painel.
    Bibliotecario->>Web: registar devolução
    Web->>Service: devolver(id, data)
    alt devolvido dentro do prazo
        Service-->>Web: fechar empréstimo e libertar exemplar
    else devolvido fora do prazo
        Service->>Service: criar suspensão temporária
        Service-->>Web: fechar empréstimo, libertar exemplar e informar sanção
    end
```

## Modelo de domínio actual

```mermaid
classDiagram
    class Book {
      +String code
      +String title
      +String author
      +boolean available
    }
    class Reader {
      +String id
      +String name
      +String email
    }
    class Loan {
      +String id
      +LocalDate borrowedOn
      +LocalDate dueOn
      +LocalDate returnedOn
      +isOverdueOn(date)
    }
    class Suspension {
      +String id
      +LocalDate startsOn
      +LocalDate endsOn
      +isActiveOn(date)
    }
    Reader "1" --> "0..*" Loan : requisita
    Book "1" --> "0..*" Loan : exemplar
    Loan "1" --> "0..1" Suspension : pode gerar
```

## Justificação do estilo

As camadas favorecem a **manutenibilidade**: interface, regras e armazenamento têm responsabilidades distintas. A apresentação Web pode ser alterada nos templates e no CSS sem reescrever as regras. Favorecem também a **testabilidade**: os serviços podem receber repositórios diferentes e as implementações em memória permitem testes rápidos sem infraestrutura externa.

Microserviços poderiam escalar componentes de forma independente, mas acrescentariam comunicação de rede, implantação e observabilidade que não se justificam para o âmbito pequeno e local desta aplicação. Uma arquitectura orientada a eventos ajudaria a desacoplar reacções assíncronas, mas o fluxo actual é síncrono e curto. A arquitectura em camadas mantém menor complexidade operacional e cobre as necessidades de manutenção e testes do problema.

## Requisitos funcionais implementados

| ID | Requisito | Estado |
| --- | --- | --- |
| RF01 | Registar e listar exemplares; impedir código interno duplicado | Implementado |
| RF02 | Registar e listar leitores; validar campos e e-mail | Implementado |
| RF03 | Consultar exemplar por código interno e leitor por identificador | Implementado |
| RF04 | Consultar disponibilidade do livro | Implementado |
| RF05 | Emprestar apenas livros disponíveis a leitores existentes | Implementado |
| RF06 | Devolver apenas empréstimos activos | Implementado |
| RF07 | Registar a data e consultar empréstimos | Implementado |
| RF08 | Calcular prazo de devolução configurável e detectar atraso | Implementado |
| RF09 | Suspender temporariamente novo empréstimo após devolução tardia | Implementado |
| RF10 | Alertar bibliotecário sobre empréstimos em atraso e suspensões activas | Implementado |
| RF11 | Autenticar colaboradores e separar privilégios de administrador e bibliotecário | Implementado |
| RF12 | Pesquisar exemplares e leitores | Implementado |

## Requisitos não funcionais

- Java 17 e Maven.
- Domínio, serviços e acesso a dados separados.
- Persistência substituível através de interfaces de repositório; implementação inicial em memória.
- Palavras-passe codificadas com BCrypt, sessão de autenticação, controlo por perfil e protecção CSRF.
- Sanção aplicada é suspensão temporária; não há pagamento nem multa monetária.
- Testes JUnit 5 executados por Maven.
- Cobertura mínima de 70% das linhas no pacote de negócio, medida por JaCoCo.
- Pipeline GitHub Actions descrito em `.github/workflows/ci.yml` para validar futuras integrações.

## Limites actuais

O armazenamento em memória não conserva dados entre execuções. O sistema não é multiutilizador distribuído e a sincronização das operações está limitada à instância de serviço. Os prazos actuais são valores de demonstração configuráveis, ainda sujeitos a confirmação institucional. Reservas, histórico do leitor, notificações externas e matrículas de leitores para login estão no backlog; as contas implementadas são de colaboradores.
