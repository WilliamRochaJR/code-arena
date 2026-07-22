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

## API

- [Contratos REST](api/contracts.md)

## Decisoes

- [Como usamos ADRs](decisions/README.md)
- [ADR 0001: usar um monorepositorio](decisions/0001-use-monorepo.md)
- [ADR 0002: usar Git Flow simplificado](decisions/0002-use-simplified-git-flow.md)
- [ADR 0003: manter o repositorio privado durante o desenvolvimento](decisions/0003-keep-repository-private-during-development.md)

## Guias

- [Fluxo de trabalho com Git](guides/git-workflow.md)
- [Comandos de terminal](guides/terminal-commands.md)

## Desenvolvimento

- [Definition of Done](development/definition-of-done.md)

## Politica de documentacao

- Uma decisao arquitetonica relevante deve gerar um ADR.
- Um procedimento necessario para desenvolver, testar ou publicar deve ter um
  guia com comandos verificaveis.
- Pull Requests registram objetivo, criterios de aceite, validacoes e limitacoes
  de cada entrega.
- Segredos, tokens, credenciais e dados pessoais nao podem aparecer nos
  documentos nem no historico Git.
- Comandos exploratorios triviais nao precisam ser registrados; comandos que
  alteram estado ou ajudam a reproduzir o trabalho devem ser documentados.
