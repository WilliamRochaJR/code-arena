# Git

Os fluxos entre branches estao no [guia de Git](../git-workflow.md).

## Inicializar e associar um repositorio

```bash
git init -b main
git remote add origin <url-do-repositorio>
```

Cria o repositorio com `main` e registra o remote `origin`.

## Conferir estado e historico

```bash
git status --short --branch
git branch -vv
git remote -v
git log --oneline --decorate --graph --all --max-count=<quantidade>
```

Sao consultas de branch atual, upstream, remotes e commits recentes.

## Sincronizar referencias

```bash
git fetch --prune origin
git pull --ff-only origin <branch>
```

`fetch --prune` atualiza referencias e elimina referencias remotas obsoletas.
`pull --ff-only` interrompe se a atualizacao exigiria um merge.

## Criar, trocar e remover uma branch

```bash
git switch <branch-existente>
git switch -c <tipo>/<descricao>
git branch -d <branch-mesclada>
```

`-d` se recusa a apagar trabalho que o Git nao reconhece como integrado. Nao
use `-D` sem investigar.

## Revisar alteracoes

```bash
git diff
git diff --check
git diff --stat
git diff --name-status
```

Essas variantes mostram conteudo, whitespace invalido, tamanho e tipos de
alteracao.

## Preparar e criar um commit

```bash
git add <caminhos>
git diff --cached
git diff --cached --check
git diff --cached --stat
git commit -m "tipo(escopo): descricao"
```

Revise exatamente o stage antes do commit. Husky executa os hooks configurados.

## Corrigir o ultimo commit nao publicado

```bash
git commit --amend --no-edit
```

Recria o commit e muda seu hash. Use antes do push ou com coordenacao explicita.

## Publicar uma branch

```bash
git push -u origin <branch>
```

`-u` configura o upstream. Push direto em `main` ou `develop` nao faz parte do
fluxo normal.

## Alterar o URL de um remote

```bash
git remote set-url origin <nova-url>
```

Altera somente a configuracao local.

## Migrar referencias

```bash
git remote rename origin old-origin
git remote add origin <url-do-novo-repositorio>
git push origin \
  refs/remotes/old-origin/main:refs/heads/main \
  refs/remotes/old-origin/develop:refs/heads/develop
```

Git transfere commits, branches e tags. Pull Requests, Rulesets e secrets devem
ser recriados no destino.

## Alinhar branches a um novo origin

```bash
git branch -f main origin/main
git branch -f develop origin/develop
git branch --set-upstream-to=origin/main main
git branch --set-upstream-to=origin/develop develop
```

`branch -f` move referencias. Use somente depois de comparar o historico.

## Restaurar uma branch remota

```bash
git switch <branch>
git merge --ff-only <referencia-com-o-historico>
```

O fast-forward restaura a referencia sem criar um merge commit. Depois, use o
comando ja catalogado em [Publicar uma branch](#publicar-uma-branch).

## Criar e publicar uma tag anotada

```bash
git tag -a v<versao> -m "Code Arena <versao>"
git show v<versao>
git push origin v<versao>
```

Consulte a [estrategia de versionamento](../versioning-and-releases.md) antes de
publicar.
