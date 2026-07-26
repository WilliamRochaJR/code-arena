# Comandos de terminal

Este documento registra todos os comandos de terminal diferentes executados no
trabalho do repositorio durante a construcao e operacao do Code Arena. Ele
funciona como referencia de estudo, catalogo de diagnostico e trilha
reproduzivel.

Cada forma diferente de comando deve aparecer ao menos uma vez, inclusive quando
for usada apenas para leitura, inspecao ou diagnostico. Repeticoes com a mesma
finalidade nao precisam gerar entradas duplicadas; argumentos variaveis podem
ser representados por placeholders. Comandos usados apenas para criar artefatos
pessoais fora do repositorio nao fazem parte deste catalogo.

## Convencoes

- Execute os comandos a partir da raiz do repositorio, salvo indicacao contraria.
- Leia a explicacao antes de executar comandos que alteram branches ou estado.
- Nunca copie credenciais reais para exemplos.
- Registre um comando novo na mesma entrega em que ele for usado.
- Documente o objetivo e se o comando apenas le ou tambem altera estado.
- Use placeholders como `<numero>` e `<branch>` para valores que variam.
- Nunca registre tokens, credenciais ou saidas que revelem valores sensiveis.

## Inspecionar arquivos e localizar texto

```bash
rg --files docs apps packages
rg -n "<padrao>" <arquivos-ou-diretorios>
sed -n '<inicio>,<fim>p' <arquivo>
```

- `rg --files` lista arquivos sem alterar o repositorio.
- `rg -n` localiza texto e mostra os numeros das linhas correspondentes.
- `sed -n` imprime somente o intervalo solicitado de um arquivo.

Para descobrir se uma ferramenta esta instalada e onde esta o executavel:

```bash
command -v <ferramenta>
```

O comando apenas consulta o ambiente e nao instala a ferramenta.

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

## Criar, trocar e remover uma branch local

```bash
git switch develop
git switch -c <tipo>/<descricao>
git branch -d <branch-mesclada>
```

- `git switch` troca a branch atual.
- `git switch -c` cria e seleciona uma branch.
- `git branch -d` remove apenas uma branch local reconhecida como integrada. A
  opcao `-D` nao deve ser usada sem investigar e confirmar o descarte.

## Revisar e criar um commit

```bash
git diff
git add <caminhos>
git diff --cached
git diff --cached --check
git diff --cached --stat
git commit -m "tipo(escopo): descricao"
```

- `git diff` mostra mudancas ainda fora do stage.
- `git add` seleciona explicitamente os arquivos do commit.
- `git diff --cached` permite revisar exatamente o que sera commitado.
- `--check` identifica whitespace invalido, e `--stat` resume o tamanho.
- `git commit` cria o commit local e dispara os hooks do Husky.

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

## Conferir autenticacao e acesso ao GitHub

```bash
gh --version
gh auth status
ssh -T git@github.com
git ls-remote <remote-ou-url>
```

- `gh --version` mostra a versao instalada do GitHub CLI.
- `gh auth status` informa quais contas estao autenticadas, sem exibir o token
  completo.
- `ssh -T` confirma qual conta do GitHub esta associada a chave SSH.
- `git ls-remote` lista referencias acessiveis sem clonar nem enviar commits.

## Consultar repositorio, Pull Requests e Actions

```bash
gh repo view <owner>/<repositorio> \
  --json nameWithOwner,visibility,defaultBranchRef,url,viewerPermission

gh pr status --repo <owner>/<repositorio>

gh pr view <numero-ou-branch> \
  --repo <owner>/<repositorio> \
  --json number,title,state,baseRefName,headRefName,mergeable,statusCheckRollup,url

gh run list \
  --repo <owner>/<repositorio> \
  --branch <branch> \
  --limit <quantidade>
```

Esses comandos consultam metadados do GitHub sem modificar o repositorio. Eles
foram usados para confirmar a branch base, o estado do PR e a execucao do check
`JavaScript quality`.

## Consultar configuracoes pela API do GitHub

```bash
gh api repos/<owner>/<repositorio>
gh api repos/<owner>/<repositorio>/rulesets
gh api repos/<owner>/<repositorio>/rulesets/<id>
```

As consultas foram usadas para verificar configuracoes de merge e os detalhes
dos Rulesets. O comando e somente leitura quando nao recebe uma opcao de metodo
ou campos para escrita.

## Alterar o URL de um remote

```bash
git remote set-url origin https://github.com/<owner>/<repositorio>.git
gh auth setup-git
```

O primeiro comando altera apenas a configuracao local do remote. O segundo
configura o Git para usar a autenticacao da conta ativa no GitHub CLI, sem
gravar o token na documentacao.

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

## Instalar dependencias de forma reproduzivel

```bash
npm ci
```

`npm ci` remove a instalacao atual de dependencias e restaura exatamente as
versoes do `package-lock.json`. Ele nao cria commits nem faz push. O comando e
usado na CI e pode ser executado localmente para reproduzir o ambiente do
workflow.

## Auditar dependencias npm

```bash
npm audit
```

O comando consulta vulnerabilidades conhecidas nas dependencias instaladas. Ele
nao corrige nem atualiza pacotes sem a opcao explicita de correcao.

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
