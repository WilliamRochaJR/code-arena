# Documentacao do Code Arena

Este diretorio registra o produto, a arquitetura, as decisoes e os procedimentos
necessarios para compreender e reproduzir o projeto.

## Produto

- [Escopo do MVP](product/mvp.md)
- [Roadmap](roadmap.md)

## Arquitetura

- [Visao geral](architecture/overview.md)
- [Modelo de dominio](architecture/domain-model.md)
- [Autenticacao e autorizacao](architecture/authentication.md)
- [Observabilidade](architecture/observability.md)

## API

- [Contratos REST](api/contracts.md)

## Decisoes

- [Como usamos ADRs](decisions/README.md)
- [ADR 0001: usar um monorepositorio](decisions/0001-use-monorepo.md)
- [ADR 0002: usar Git Flow simplificado](decisions/0002-use-simplified-git-flow.md)
- [ADR 0003: manter o repositorio privado durante o desenvolvimento](decisions/0003-keep-repository-private-during-development.md)
- [ADR 0004: usar Semantic Versioning e tags anotadas](decisions/0004-use-semantic-versioning.md)

## Guias

- [Instrucoes para agentes de IA](../AGENTS.md)
- [Fluxo de trabalho com Git](guides/git-workflow.md)
- [Configuracao do repositorio no GitHub](guides/github-repository-settings.md)
- [Versionamento, releases e tags](guides/versioning-and-releases.md)
- [Diario de comandos do terminal](guides/terminal-commands.md)
- [Referencia de comandos](guides/command-reference.md)

## Desenvolvimento

- [Estrategia de qualidade de codigo](development/code-quality.md)
- [Integracao continua](development/continuous-integration.md)
- [Definition of Done](development/definition-of-done.md)

## Politica de documentacao

- Uma decisao arquitetonica relevante deve gerar um ADR.
- Um procedimento necessario para desenvolver, testar ou publicar deve ter um
  guia com comandos verificaveis.
- Pull Requests registram objetivo, criterios de aceite, validacoes e limitacoes
  de cada entrega.
- Segredos, tokens, credenciais e dados pessoais nao podem aparecer nos
  documentos nem no historico Git.
- Todos os blocos de terminal executados no trabalho do repositorio devem ser
  registrados cronologicamente, inclusive repeticoes, leituras e diagnosticos.
  Cada registro explica objetivo e resultado; artefatos pessoais externos e
  valores sensiveis ficam fora do diario.
