# Fluxo de trabalho com Git

Este guia descreve o Git Flow simplificado do Code Arena. As setas usadas na
documentacao representam a direcao de um Pull Request, nao comandos de terminal.

```text
feature/* --> develop --> release/* --> main
                    ^                    |
                    `------ hotfix/* ----'
```

## Branches permanentes

- `main`: estado estavel e releases.
- `develop`: integracao da proxima versao.

Essas branches nao devem ser removidas. A opcao do GitHub que apaga head branches
depois do merge exige cuidado: nunca abra um PR tendo `develop` como origem
apenas para sincroniza-la com `main`, a menos que esteja preparado para restaura-
la.

## Iniciar uma feature

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git switch -c feature/NNN-descricao-curta
```

- `fetch --prune` atualiza referencias e remove referencias remotas obsoletas.
- `switch develop` seleciona a branch de integracao.
- `pull --ff-only` atualiza sem criar merge commit inesperado.
- `switch -c` cria e seleciona a branch da entrega.

## Inspecionar alteracoes

```bash
git status --short --branch
git diff
git diff --check
```

Antes do commit, revise os arquivos, execute as validacoes relevantes e proponha
uma mensagem Conventional Commit.

## Criar um commit

```bash
git add caminho/do/arquivo
git diff --cached
git commit -m "tipo(escopo): descreve a intencao"
```

Exemplos:

```text
feat(api): create quiz attempt
fix(sdk): prevent repeated token refresh
test(ui): cover progress bar accessibility
docs(architecture): record authentication decision
```

Commits devem representar uma intencao. Implementacao e seus testes podem fazer
parte do mesmo commit quando formam uma unidade coerente.

## Publicar e abrir o PR

```bash
git push -u origin feature/NNN-descricao-curta
```

No GitHub, confirme explicitamente:

```text
base: develop
compare: feature/NNN-descricao-curta
```

O PR registra objetivo, criterios de aceite, validacoes, riscos e itens fora do
escopo.

## Depois do merge de uma feature

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git branch -d feature/NNN-descricao-curta
```

`-d` recusa excluir uma branch que o Git local nao reconheca como integrada. Nao
use `-D` sem investigar.

## Preparar uma release

```bash
git switch develop
git pull --ff-only origin develop
git switch -c release/1.0.0
```

O Pull Request da release usa:

```text
base: main
compare: release/1.0.0
```

Depois do merge e da validacao:

```bash
git switch main
git pull --ff-only origin main
git tag -a v1.0.0 -m "Code Arena 1.0.0"
git push origin v1.0.0
```

As correcoes exclusivas da release tambem precisam voltar para `develop` por um
fluxo explicito que nao use `develop` como head descartavel.

## Hotfix

```bash
git switch main
git pull --ff-only origin main
git switch -c hotfix/1.0.1-descricao-curta
```

O hotfix entra em `main` por PR e sua correcao tambem deve ser incorporada em
`develop`.

## Acoes proibidas no fluxo normal

```text
push direto em main ou develop
git push --force em branches compartilhadas
git reset --hard para descartar trabalho sem revisao
commitar tokens, senhas, chaves ou arquivos .env reais
```

## Protecao no GitHub

Enquanto o repositorio estiver privado no plano atual, o GitHub informa que os
Rulesets nao serao aplicados. Mesmo assim, devem ficar documentadas as regras:

- exigir Pull Request para `main` e `develop`;
- bloquear force push e exclusao das branches permanentes;
- exigir resolucao de conversas;
- exigir checks de CI quando eles existirem;
- apagar automaticamente somente branches curtas depois do merge.
