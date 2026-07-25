# Comandos de terminal

Este documento registra comandos importantes usados para construir e operar o
Code Arena. Ele funciona como referencia de estudo e trilha reproduzivel; nao e
uma transcricao de toda consulta exploratoria.

## Convencoes

- Execute os comandos a partir da raiz do repositorio, salvo indicacao contraria.
- Leia a explicacao antes de executar comandos que alteram branches ou estado.
- Nunca copie credenciais reais para exemplos.
- Atualize este guia quando um novo procedimento se tornar parte do projeto.

## Verificar ferramentas locais

```bash
java -version
javac -version
mvn -version
node --version
npm --version
yarn --version
```

Versoes verificadas no inicio do projeto:

```text
Java: 21.0.11
Maven: 3.6.3
Node.js: 22.12.0
npm: 10.9.0
Yarn: 1.22.22
```

Gradle e pnpm nao estavam instalados e nao sao necessarios para a stack
escolhida.

## Inicializar o repositorio

```bash
git init -b main
git remote add origin git@github.com:william-rocha/code-arena.git
```

O primeiro comando cria o repositorio com `main` como branch inicial. O segundo
associa o checkout local ao repositorio GitHub.

## Migrar para outro repositorio

O projeto foi transferido temporariamente para
`https://github.com/WilliamRochaJR/code-arena` enquanto uma restricao de billing
impedia o GitHub Actions na conta original. Antes da troca, as referencias do
repositorio antigo foram atualizadas:

```bash
git status --short --branch
git remote -v
git branch -vv
git tag --list
git fetch --prune origin
```

O remote original foi preservado, e o novo repositorio passou a ser o `origin`:

```bash
git remote rename origin old-origin
git remote add origin https://github.com/WilliamRochaJR/code-arena.git
gh auth setup-git
git remote -v
```

HTTPS foi usado porque a chave SSH disponivel autenticava a conta anterior. O
GitHub CLI forneceu ao Git a autenticacao da nova conta sem incluir tokens nos
comandos ou na configuracao versionada.

As branches permanentes atualizadas foram enviadas diretamente das referencias
do repositorio antigo:

```bash
git push origin \
  refs/remotes/old-origin/main:refs/heads/main \
  refs/remotes/old-origin/develop:refs/heads/develop
```

Depois do envio, as branches locais foram alinhadas ao novo remote:

```bash
git fetch origin
git branch -f main origin/main
git branch -f develop origin/develop
git branch --set-upstream-to=origin/main main
git branch --set-upstream-to=origin/develop develop
git switch develop
```

Por fim, o estado foi conferido sem modificar o repositorio:

```bash
git status --short --branch
git branch -vv
git remote -v
git ls-remote --heads origin
```

Git transfere commits, branches e tags, mas nao transfere Pull Requests, Issues,
Rulesets, secrets nem configuracoes do repositorio. Esses itens precisam ser
recriados no destino. O remote `old-origin` permanece disponivel como referencia
e nao deve receber novos pushes durante a migracao temporaria.

## Conferir remoto e estado

```bash
git remote -v
git status --short --branch
git branch -vv
git log --oneline --decorate --graph --all --max-count=15
```

Esses comandos ajudam a confirmar remoto, branch atual, tracking e historico sem
alterar arquivos.

## Publicacao inicial das branches

```bash
git push -u origin main
git push -u origin develop
git push -u origin feature/001-project-documentation
```

`-u` configura a branch remota como upstream da branch local.

## Sincronizar referencias

```bash
git fetch --prune origin
```

O comando baixa referencias e remove localmente referencias de branches remotas
que ja foram apagadas.

## Atualizar sem merge implicito

```bash
git pull --ff-only origin develop
```

`--ff-only` falha se a atualizacao exigiria um merge, permitindo investigar a
divergencia antes de modificar o historico.

## Restauracao de develop ocorrida no projeto

A exclusao automatica de branches removeu `develop` quando ela foi usada como
origem de um Pull Request para `main`. Como `origin/main` continha todo o
historico aprovado, a restauracao foi feita com:

```bash
git switch develop
git merge --ff-only origin/main
git push -u origin develop
```

O merge em fast-forward atualizou a referencia local sem criar um commit novo; o
push recriou a branch remota. O incidente motivou a observacao sobre branches
permanentes no guia de Git.

## Validar documentacao antes do commit

```bash
git diff --check
git status --short --branch
git diff --stat
```

Essas verificacoes identificam problemas de whitespace, mostram o estado e
resumem o tamanho da alteracao.

## Criar a aplicacao React com Vite

```bash
npm create vite@latest apps/web -- --template react-ts --no-interactive
```

O comando usa o criador oficial do Vite para gerar uma aplicacao React com
TypeScript em `apps/web`. O template e revisado antes de instalar dependencias.

## Instalar os npm workspaces

```bash
npm install
```

Executado na raiz, instala dependencias, cria `package-lock.json` e conecta os
workspaces declarados no `package.json`.

O primeiro teste com ESLint 10 revelou que ele exige Node 22.13 ou superior. Como
o ambiente local usa Node 22.12, a configuracao foi ajustada para uma combinacao
sem warnings de engine:

```text
Node.js: 22.12.0
TypeScript: 5.9.3
ESLint: 9.39.5
typescript-eslint: 8.55.0
```

Essa decisao evita exigir uma atualizacao local apenas para o lint e mantem as
versoes dentro das faixas suportadas entre si.

## Executar a aplicacao web

```bash
npm run dev:web
```

O script inicia o Vite no workspace `@code-arena/web`.

## Validar todos os workspaces

```bash
npm run format:check
npm run lint
npm run typecheck
npm test
npm run build
```

- `format:check` verifica o padrao do Prettier sem alterar arquivos.
- `lint` executa ESLint no monorepositorio.
- `typecheck` valida os tipos de cada workspace.
- `test` executa Vitest nos workspaces que possuem o script.
- `build` gera a aplicacao e os pacotes.

## Instalar protecoes locais de commit

```bash
npm install --save-dev \
  husky@^9.1.7 \
  lint-staged@16.4.0 \
  @commitlint/cli@^21.2.1 \
  @commitlint/config-conventional@^21.2.0
npx husky init
```

O Husky conecta hooks ao Git. `lint-staged` executa ESLint e Prettier apenas nos
arquivos selecionados para o commit. Commitlint valida mensagens no formato
Conventional Commits.

Hooks configurados:

```text
pre-commit --> npx lint-staged
commit-msg --> npx commitlint --edit "$1"
```

O script `prepare` reinstala os hooks automaticamente depois de `npm install`.
Hooks ajudam no ambiente local, mas podem ser ignorados; o GitHub Actions sera a
validacao independente antes do merge.

## Comandos futuros

Comandos de npm workspaces, frontend, SDK, UI, Spring Boot, Docker, testes e
deploy serao adicionados quando as respectivas entregas forem implementadas e
validadas.
