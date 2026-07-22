# ADR 0002: Usar Git Flow simplificado

## Status

Aceita

## Data

2026-07-22

## Contexto

O projeto precisa demonstrar entregas pequenas, Pull Requests, releases e
correcoes sem introduzir uma ferramenta que esconda as operacoes do Git. O
desenvolvimento e individual, mas deve reproduzir um processo de equipe.

## Decisao

Usar `main` e `develop` como branches permanentes, com branches curtas
`feature/*`, `fix/*`, `release/*` e `hotfix/*`. As operacoes serao feitas com Git
puro, sem instalar a CLI `git-flow`.

- Features e fixes de desenvolvimento partem de `develop` e voltam por PR.
- Releases partem de `develop` e entram em `main` por PR.
- Hotfixes partem de `main` e precisam ser incorporados em `main` e `develop`.
- `main` representa versoes estaveis; `develop`, a proxima versao.

## Alternativas consideradas

### GitHub Flow

Possui menos branches permanentes, mas oferece menos oportunidade de praticar a
separacao entre integracao e release desejada neste portfolio.

### CLI git-flow

Fornece atalhos para iniciar e finalizar branches, mas adiciona uma dependencia
local e pode esconder merges que queremos executar e compreender explicitamente.

### Desenvolvimento direto em main

E simples, mas nao registra um ciclo de revisao adequado ao objetivo do projeto.

## Consequencias

### Positivas

- Fluxo explicito e reproduzivel apenas com Git.
- Historico de features e releases visivel no GitHub.
- Pratica alinhada a Pull Requests e revisao de codigo.

### Negativas

- Mais branches e sincronizacoes do que no GitHub Flow.
- A base do PR precisa ser selecionada com atencao.
- A exclusao automatica de head branches pode apagar `develop` se ela for usada
  como origem de um PR; branches permanentes precisam ser restauradas nesse caso.
