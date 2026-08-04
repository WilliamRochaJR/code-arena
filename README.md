# Code Arena

Plataforma de questionarios tecnicos criada para demonstrar o desenvolvimento
de uma aplicacao web completa, desde o refinamento ate a entrega.

O projeto sera composto por uma aplicacao React, um SDK TypeScript reutilizavel,
uma biblioteca de componentes e uma API Java 21 com Spring Boot.

## Componentes

- [Aplicacao web](apps/web/README.md) - React, TypeScript e Vite.
- [API Spring Boot](apps/api/README.md) - Java 21, Spring Boot e PostgreSQL.
- [Java Quiz SDK](packages/java-quiz-sdk/README.md) - cliente REST TypeScript
  reutilizavel e independente de React.
- Biblioteca de UI - scaffold em `packages/ui`; a documentacao sera adicionada
  com os primeiros componentes.
- [Infraestrutura Terraform](infrastructure/terraform/README.md) - bootstrap da
  arquitetura AWS temporaria, ainda sem recursos provisionaveis.

## Documentacao tecnica

Consulte o [indice da documentacao](docs/README.md) para acompanhar produto,
arquitetura, contratos, decisoes, qualidade e processo de desenvolvimento.

## Executar localmente com Docker

O Compose inicia PostgreSQL, API e frontend, nessa ordem, usando healthchecks:

```bash
docker compose up -d --build --wait
```

Depois da inicializacao, acesse:

- frontend: `http://localhost:3000`;
- API: `http://localhost:8080`;
- Prometheus: `http://localhost:9090`;
- Grafana: `http://localhost:3001`;
- PostgreSQL: `localhost:5433`.

O Grafana permite acesso local anonimo somente para leitura. O datasource
Prometheus e o dashboard **Code Arena API** sao provisionados automaticamente,
sem credenciais ou configuracao manual.

As migracoes Flyway sao aplicadas pela API durante a inicializacao. Os valores
padrao servem exclusivamente para desenvolvimento local. Para alterar portas ou
credenciais, copie `.env.example` para `.env`; o arquivo `.env` e ignorado pelo
Git. Encerre os servicos preservando os dados com:

```bash
docker compose down
```
