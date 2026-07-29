# Code Arena API

API REST do Code Arena, responsavel pelas regras de negocio, autorizacao,
persistencia das tentativas e calculo dos resultados dos questionarios.

## Stack

| Tecnologia              | Finalidade                               |
| ----------------------- | ---------------------------------------- |
| Java 21                 | Linguagem e runtime                      |
| Spring Boot 4.1         | Fundacao da aplicacao                    |
| Spring Web MVC          | API REST                                 |
| Spring Security         | Autenticacao e autorizacao               |
| Spring Data JPA         | Persistencia                             |
| PostgreSQL              | Banco de dados relacional                |
| Flyway                  | Versionamento do schema                  |
| Actuator                | Health checks e endpoints operacionais   |
| Micrometer + Prometheus | Instrumentacao e exposicao de metricas   |
| SLF4J + Logback         | API e implementacao de logging           |
| JUnit + Testcontainers  | Testes com PostgreSQL real em container  |
| Maven Wrapper           | Build reproduzivel sem Maven global fixo |

As versoes exatas ficam no [`pom.xml`](pom.xml) e sao gerenciadas pelo parent
do Spring Boot.

## Requisitos locais

- Java 21;
- Docker com Docker Compose.

Nao e necessario instalar uma versao global do Maven. Use o wrapper
`apps/api/mvnw`.

## Executar localmente

Na raiz do repositorio, inicie o PostgreSQL:

```bash
docker compose up -d postgres
```

Depois inicie a API com o perfil local:

```bash
cd apps/api
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

A API usa `http://localhost:8080` e acessa o PostgreSQL do Compose pela porta
local `5433`. A porta `5432` permanece interna ao container. As credenciais
padrao do Compose servem somente para desenvolvimento local e podem ser
substituidas por `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

## Observabilidade

O bootstrap expoe:

- `GET /actuator/health`;
- `GET /actuator/health/liveness`;
- `GET /actuator/health/readiness`;
- `GET /actuator/prometheus`.

Health e metricas sao publicos nesta fundacao para permitir verificacoes locais.
Qualquer outro endpoint e negado explicitamente ate a integracao de autenticacao
ser implementada.

Logs usam SLF4J com Logback e sao escritos no console. Tokens, credenciais e
outros dados sensiveis nao devem ser registrados.

## Testes e build

```bash
cd apps/api
./mvnw verify
```

O teste de contexto usa Testcontainers e inicia um PostgreSQL isolado. O Docker
precisa estar disponivel.

## Documentacao relacionada

- [Visao da arquitetura](../../docs/architecture/overview.md)
- [Observabilidade](../../docs/architecture/observability.md)
- [Autenticacao](../../docs/architecture/authentication.md)
- [Contratos REST](../../docs/api/contracts.md)
- [Definition of Done](../../docs/development/definition-of-done.md)
