# Roadmap de implementacao

Cada entrega deve nascer de uma issue com criterios de aceite, usar uma branch
curta e terminar em Pull Request para `develop`. Lint, testes e build relevantes
precisam passar antes do merge.

## Entrega 1 - Especificacao do projeto

Branch: `feature/001-project-documentation`

- Definir escopo e regras do MVP.
- Registrar arquitetura, dominio e autenticacao.
- Definir contratos REST iniciais.
- Dividir a implementacao em entregas incrementais.

## Entrega 2 - Fundacao do monorepositorio

Branch: `feature/002-monorepo-setup`

- Configurar npm workspaces.
- Criar `apps/web`, `packages/java-quiz-sdk` e `packages/ui`.
- Adicionar configuracoes compartilhadas de TypeScript, ESLint e Prettier.
- Configurar Husky, lint-staged e Commitlint.
- Criar templates de issue e Pull Request.
- Criar pipeline inicial de lint, testes e build.

## Entrega 3 - Bootstrap da API

Branch: `feature/003-api-bootstrap`

- Gerar Spring Boot pelo Spring Initializr em `apps/api`.
- Usar Java 21, Maven Wrapper e Spring Boot 4.1.0.
- Adicionar Web, Validation, Data JPA, PostgreSQL, Flyway, Security, OAuth2
  Resource Server, Actuator e suporte a testes com Testcontainers.
- Configurar perfis, verificacao inicial de saude e metricas para Prometheus.
- Usar SLF4J com Logback e logs direcionados ao console.
- Adicionar PostgreSQL ao Docker Compose para desenvolvimento.

## Entrega 4 - Persistencia do dominio

Branch: `feature/004-quiz-persistence`

- Criar migrations de usuarios, categorias, questoes, alternativas e tentativas.
- Mapear entidades e repositories.
- Criar seed inicial de categorias e questoes.
- Testar constraints e consultas com PostgreSQL/Testcontainers.

## Entrega 5 - API de tentativas

Branch: `feature/005-quiz-attempt-api`

- Criar e consultar tentativas.
- Salvar e substituir respostas.
- Concluir, corrigir e calcular resultados.
- Listar historico paginado.
- Implementar Problem Details e OpenAPI.
- Cobrir regras de negocio e integracao REST.

Inicialmente, testes usam uma identidade controlada. A seguranca de producao e
integrada em uma entrega posterior.

## Entrega 6 - SDK TypeScript

Branch: `feature/006-java-quiz-sdk`

- Criar cliente HTTP e recursos de usuario, categorias e tentativas.
- Expor contratos TypeScript publicos.
- Implementar token provider, timeout, `AbortSignal` e erros tipados.
- Testar sucesso, falhas HTTP, falhas de rede e cancelamento.
- Gerar build ESM e declaracoes TypeScript.

## Entrega 7 - Biblioteca de UI

Branch: `feature/007-ui-library`

- Definir tokens visuais essenciais.
- Criar Button, Card, RadioGroup, ProgressBar, Alert e Loading.
- Garantir navegacao por teclado, foco e semantica.
- Adicionar Storybook e testes de componentes.

## Entrega 8 - Aplicacao web

Branch: `feature/008-web-quiz-flow`

- Criar dashboard, configuracao, execucao, resultado e historico.
- Consumir a API exclusivamente pelo SDK.
- Usar TanStack Query, React Hook Form e Zod quando aplicavel.
- Implementar layout mobile-first e estados de interface.
- Cobrir componentes e fluxo principal com testes.

## Entrega 9 - Cognito e autorizacao

Branch: `feature/009-cognito-authentication`

- Configurar Authorization Code com PKCE e login Google.
- Implementar Auth Provider, callback, logout e rotas protegidas.
- Validar access tokens na API.
- Associar recursos ao `sub` e impedir acesso cruzado.
- Testar respostas `401`, `403` e isolamento entre usuarios.

## Entrega 10 - Empacotamento e qualidade ponta a ponta

Branch: `feature/010-containerization-e2e`

- Criar Dockerfiles multi-stage para web e API.
- Criar composicao completa com health checks.
- Adicionar Prometheus, Grafana e um dashboard local versionado.
- Executar containers sem privilegios quando aplicavel.
- Adicionar testes E2E com Playwright.
- Documentar execucao local com e sem containers.

## Entrega 11 - CI/CD e AWS

Branch: `feature/011-aws-infrastructure`

- Modelar infraestrutura com Terraform.
- Publicar frontend, API e PostgreSQL nos servicos AWS definidos.
- Usar OIDC no GitHub Actions e evitar chaves de longa duracao.
- Configurar logs, metricas, segredos e smoke tests de deploy.

## Entrega 12 - Primeiro release

Branch: `release/1.0.0`

- Revisar documentacao e criterios do MVP.
- Executar testes, builds, analise de seguranca e validacao manual.
- Atualizar changelog.
- Integrar em `main` e `develop`.
- Criar a tag `v1.0.0`.

## Depois do MVP

- CRUD administrativo de questoes.
- Ranking e estatisticas de evolucao.
- Internacionalizacao e tema escuro.
- Importacao controlada de questoes.
- Revisao direcionada de respostas incorretas.
- Centralizacao de logs com Loki no Grafana.
- Tracing com OpenTelemetry e Tempo.
