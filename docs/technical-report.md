# Relatório técnico da Biblioteca ISTM

O relatório em PDF está em [output/pdf/relatorio-tecnico-biblioteca-istm.pdf](../output/pdf/relatorio-tecnico-biblioteca-istm.pdf). O fonte editável é [tools/generate_report.py](../tools/generate_report.py).

## Evidência da execução local

- Data: 4 de Novembro de 2026.
- Build: `mvn verify`, com repositório Maven local dentro do projecto.
- Resultado: BUILD SUCCESS.
- Testes: 30 passaram; 0 falhas; 0 erros; 0 ignorados.
- JaCoCo 0.8.16: 280/299 linhas (93,6%) e 107/138 ramos (77,5%) no total analisado.
- Regras de negócio (`service`): 45/47 linhas (95,7%), acima do limiar configurado de 70%.
- Classes de produção analisadas: 18.
- Segurança Web testada: login obrigatório, separação de perfis, CSRF, gestão de contas, alertas, prazo e suspensão.
- Artefactos: `target/site/jacoco/index.html`, `target/site/jacoco/jacoco.xml` e `target/biblioteca-istm-1.0.0-SNAPSHOT.jar`.

## Limites da evidência

O workflow de GitHub Actions ainda não foi executado num repositório remoto. Não existem Pull Requests ou execuções hospedadas para medir o tempo de fecho e a percentagem de falhas. O armazenamento, as contas e as configurações são voláteis nesta versão demonstrativa. Prazos e sanções carecem de aprovação da biblioteca. Os nomes dos membros e a designação do Scrum Master também aguardam confirmação do grupo.

## Fontes técnicas

- Apache Software Foundation. (n.d.). *Maven getting started guide*. https://maven.apache.org/guides/getting-started/index.html
- GitHub. (n.d.). *Building and testing Java with Maven*. https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven
- Instituto Superior Técnico Militar. (2026). *Enunciado do mini-projecto em grupo: Tópicos Complementares, módulo Engenharia de Software Avançada*. Documento da disciplina.
- JUnit Team. (2024). *JUnit 5.11.4 user guide*. https://docs.junit.org/5.11.4/user-guide/junit-user-guide-5.11.4.pdf
- JaCoCo Team. (2026). *JaCoCo 0.8.16 Maven plug-in*. https://www.jacoco.org/jacoco/trunk/doc/maven.html
- Oracle. (n.d.). *Java Platform, Standard Edition 17 documentation*. https://docs.oracle.com/en/java/javase/17/
- VMware. (2026). *Spring Boot 3.5 reference documentation*. https://docs.spring.io/spring-boot/3.5/reference/
