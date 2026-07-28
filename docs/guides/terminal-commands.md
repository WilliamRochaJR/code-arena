# Diario de comandos do terminal

Este documento registra os comandos executados durante o desenvolvimento do Code
Arena. O objetivo e preservar o fluxo real para estudo: o que foi executado, por
que foi necessario e qual foi o resultado observado.

Para exemplos resumidos e reutilizaveis, consulte a
[referencia de comandos](command-reference.md).

## Como este diario e mantido

- Os registros sao organizados cronologicamente e por atividade.
- Blocos repetidos podem reaparecer quando fazem parte de fluxos diferentes.
- Cada registro explica objetivo e resultado, sem copiar tokens ou saidas
  sensiveis.
- Comandos de leitura e diagnostico tambem sao registrados.
- Operacoes feitas por ferramentas que nao sao um terminal, como edicao por
  patch, nao sao apresentadas como comandos de shell.
- Artefatos pessoais criados fora do repositorio nao fazem parte deste diario.

## Limite da reconstrucao historica

A obrigacao de manter um diario integral foi definida depois que o projeto ja
estava em andamento. Os registros de 22 a 27 de julho de 2026 foram reconstruidos
a partir da conversa disponivel, historico Git, documentacao, Pull Requests e
execucoes do Actions.

Esses blocos preservam os comandos confirmados, mas podem nao conter cada
consulta exploratoria antiga na ordem literal. A partir da entrega
`docs/008-terminal-command-journal`, todos os blocos executados no terminal sao
registrados durante a propria tarefa.

# 2026-07-22 - Inicio do projeto

## Verificar as ferramentas locais

```bash
java -version
javac -version
mvn -version
node --version
npm --version
yarn --version
```

Objetivo: descobrir quais versoes poderiam executar React e Spring Boot
localmente.

Resultado registrado:

```text
Java: 21.0.11
Maven: 3.6.3
Node.js: 22.12.0
npm: 10.9.0
Yarn: 1.22.22
```

## Inicializar o repositorio

```bash
git init -b main
git remote add origin git@github.com:william-rocha/code-arena.git
git remote -v
git status --short --branch
```

Objetivo: criar o repositorio local com `main`, associar o primeiro GitHub e
conferir a configuracao.

Resultado: o repositorio passou a usar
`git@github.com:william-rocha/code-arena.git` como `origin`.

## Publicar as branches iniciais

```bash
git push -u origin main
git push -u origin develop
git push -u origin feature/001-project-documentation
```

Objetivo: publicar as branches permanentes e a primeira entrega. `-u` configurou
o tracking remoto.

## Restaurar develop depois de uma exclusao acidental

```bash
git fetch --prune origin
git switch develop
git merge --ff-only origin/main
git push -u origin develop
```

Objetivo: restaurar `develop` depois que a exclusao automatica removeu a branch
usada como origem de um PR para `main`.

Resultado: `develop` foi recriada sem reescrever o historico. O incidente
motivou a regra de nunca usar `develop` como head descartavel.

## Criar a aplicacao React

```bash
npm create vite@latest apps/web -- --template react-ts --no-interactive
npm install
```

Objetivo: gerar o React com TypeScript e conectar os npm workspaces.

Resultado: `apps/web`, `packages/java-quiz-sdk` e `packages/ui` passaram a ser
administrados pelo `package-lock.json` da raiz.

## Validar os workspaces

```bash
npm run format:check
npm run lint
npm run typecheck
npm test
npm run build
npm audit
```

Objetivo: validar formatacao, analise estatica, tipos, testes, build e
vulnerabilidades conhecidas.

Resultado: os checks passaram. Os testes ainda usam `--passWithNoTests` enquanto
nao existem casos implementados, e a auditoria registrou zero vulnerabilidades.

## Instalar hooks e padrao de commits

```bash
npm install --save-dev \
  husky@^9.1.7 \
  lint-staged@16.4.0 \
  @commitlint/cli@^21.2.1 \
  @commitlint/config-conventional@^21.2.0

npx husky init
```

Objetivo: instalar hooks locais, formatacao/lint de arquivos staged e validacao
de Conventional Commits.

Resultado:

```text
pre-commit --> npx lint-staged
commit-msg --> npx commitlint --edit "$1"
```

## Reproduzir a instalacao da CI

```bash
npm ci
```

Objetivo: instalar exatamente o `package-lock.json` e reproduzir o ambiente do
GitHub Actions.

Resultado: dependencias restauradas sem criar commit ou push.

## Inspecionar e validar documentacao

```bash
git status --short --branch
git diff
git diff --check
git diff --stat
```

Objetivo: revisar alteracoes, encontrar whitespace invalido e conferir o escopo
antes dos commits.

# 2026-07-25 - Migracao para outro repositorio

## Conferir GitHub CLI e identidade SSH

```bash
gh --version
gh auth status
ssh -T git@github.com
```

Objetivo: identificar qual conta seria usada pelo GitHub CLI e pela chave SSH.

Resultado: o CLI estava autenticado como `WilliamRochaJR`, mas o SSH identificava
`william-rocha`.

## Atualizar o repositorio antigo e testar o destino

```bash
git fetch --prune origin
git branch -avv
git tag --list
git ls-remote git@github.com:WilliamRochaJR/code-arena.git
```

Objetivo: baixar o estado final do repositorio antigo, conferir branches/tags e
testar o acesso ao novo repositorio vazio.

## Preservar o repositorio antigo

```bash
git remote rename origin old-origin
git remote add origin git@github.com:WilliamRochaJR/code-arena.git
git remote -v
git ls-remote origin
```

Objetivo: manter o repositorio anterior como `old-origin` e tornar o novo
repositorio o destino principal.

Resultado: a configuracao local preservou os dois remotes.

## Diagnosticar a primeira tentativa de push

```bash
git push origin \
  refs/remotes/old-origin/main:refs/heads/main \
  refs/remotes/old-origin/develop:refs/heads/develop

ssh -T git@github.com

gh repo view WilliamRochaJR/code-arena \
  --json nameWithOwner,viewerPermission,defaultBranchRef
```

Objetivo: enviar `main` e `develop` e diagnosticar a negacao de permissao.

Resultado: o push SSH falhou porque a chave pertencia a conta antiga; o GitHub
CLI confirmou permissao `ADMIN` para `WilliamRochaJR`.

## Trocar o novo remote para HTTPS

```bash
git remote set-url origin https://github.com/WilliamRochaJR/code-arena.git
gh auth setup-git
git remote -v
```

Objetivo: usar no Git a autenticacao da conta ativa no GitHub CLI, sem expor o
token.

## Transferir main e develop

```bash
git push origin \
  refs/remotes/old-origin/main:refs/heads/main \
  refs/remotes/old-origin/develop:refs/heads/develop

git ls-remote --heads origin
```

Objetivo: enviar somente as branches permanentes atualizadas e confirmar seus
commits no destino.

Resultado:

```text
develop: fe44dae194594f37883996de69834165fe9c98fa
main:    d6b9c54daedc8d514a9f936d7b30561babc82edb
```

## Alinhar branches locais ao novo origin

```bash
git fetch origin
git branch -f main origin/main
git branch -f develop origin/develop
git branch --set-upstream-to=origin/main main
git branch --set-upstream-to=origin/develop develop
git switch develop
git status --short --branch
git branch -vv

gh repo view WilliamRochaJR/code-arena \
  --json url,defaultBranchRef,viewerPermission
```

Objetivo: atualizar as branches locais, seus upstreams e confirmar `main` como
branch padrao.

## Consultar configuracoes do novo repositorio

```bash
gh repo view WilliamRochaJR/code-arena \
  --json nameWithOwner,visibility,defaultBranchRef,url

gh api repos/WilliamRochaJR/code-arena \
  --jq '{private, default_branch, delete_branch_on_merge, allow_merge_commit, allow_squash_merge, allow_rebase_merge}'
```

Objetivo: verificar visibilidade, branch padrao, exclusao automatica e metodos de
merge.

Resultado: repositorio publico, `main` padrao e exclusao automatica inicialmente
desativada.

## Consultar Rulesets

```bash
gh api repos/WilliamRochaJR/code-arena/rulesets \
  --jq '.[] | {id, name, enforcement, target}'

gh api repos/WilliamRochaJR/code-arena/rulesets/19745468 \
  --jq '{name, enforcement, conditions, bypass_actors, rules}'

gh api repos/WilliamRochaJR/code-arena/rulesets/19745515 \
  --jq '{name, enforcement, conditions, bypass_actors, rules}'
```

Objetivo: confirmar os Rulesets `Protect main` e `Protect develop`, targets,
bypass e regras.

Resultado: ambos ativos; a leitura identificou e permitiu corrigir a resolucao de
conversas inicialmente ausente em `Protect main`.

## Criar e publicar a documentacao dos Rulesets

```bash
git switch develop
git pull --ff-only origin develop
git switch -c docs/003-repository-rulesets

npm run format:check
git diff --check
git status --short --branch
git diff --stat

git add \
  docs/README.md \
  docs/guides/git-workflow.md \
  docs/guides/github-repository-settings.md \
  docs/guides/terminal-commands.md

git diff --cached --check
git diff --cached --stat
git commit -m "docs(workflow): document GitHub rulesets and repository migration"

git push -u origin docs/003-repository-rulesets
```

Objetivo: documentar a migracao e as protecoes do GitHub em uma branch curta.

Resultado: commit `239a0aa` publicado para PR com base `develop`.

## Atualizar o mesmo Pull Request

```bash
git add \
  docs/guides/git-workflow.md \
  docs/guides/github-repository-settings.md

git commit -m "docs(workflow): explain develop base and required checks"
git push origin docs/003-repository-rulesets

gh run list \
  --repo WilliamRochaJR/code-arena \
  --branch docs/003-repository-rulesets \
  --limit 3
```

Objetivo: explicar por que `develop` e a base e confirmar a nova execucao do
Actions.

Resultado: commit `7199c80` enviado; `JavaScript quality` entrou na fila e passou.

# 2026-07-26 - Instrucoes de agentes e politica do diario

## Criar as instrucoes para agentes

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git switch -c chore/004-agent-instructions

npm run format:check
git diff --check

git add \
  AGENTS.md \
  docs/README.md \
  docs/development/code-quality.md \
  docs/development/continuous-integration.md

git diff --cached --check
git diff --cached --stat
git commit -m "chore(ai): add repository agent instructions"
git push -u origin chore/004-agent-instructions
```

Objetivo: orientar agentes sobre arquitetura, qualidade, Git, confirmacao de
commits/pushes e documentacao.

Resultado: commit `36e444e`, posteriormente mesclado no PR #3.

## Formalizar o registro de comandos

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git branch -d chore/004-agent-instructions
git switch -c docs/005-terminal-command-policy

rg -n "transcricao|exploratorio|comando|terminal" \
  AGENTS.md docs/README.md docs/guides/terminal-commands.md

sed -n '1,220p' docs/guides/terminal-commands.md
sed -n '1,190p' AGENTS.md
```

Objetivo: localizar regras contraditorias e transformar o registro de comandos
em obrigacao do projeto.

## Validar e publicar a politica

```bash
npm run format:check
git diff --check
git status --short --branch
git diff --stat

git add \
  AGENTS.md \
  docs/README.md \
  docs/development/code-quality.md \
  docs/development/definition-of-done.md \
  docs/guides/terminal-commands.md

git diff --cached --check
git diff --cached --stat
git commit -m "docs(workflow): require cataloging terminal commands"
git push -u origin docs/005-terminal-command-policy
```

Objetivo: publicar a politica inicial. Resultado: commit `9ea8c74`, mesclado no
PR #4.

# 2026-07-27 - Politica de Pull Requests

## Conferir a diferenca entre main e develop

```bash
git fetch --prune origin
git log --oneline origin/main..origin/develop
git diff --stat origin/main...origin/develop

gh pr list \
  --repo WilliamRochaJR/code-arena \
  --state open \
  --json number,title,baseRefName,headRefName,url
```

Objetivo: entender o que seria promovido e confirmar que nao havia PR aberto.

Resultado: `develop` continha apenas entregas de processo ainda nao publicadas em
`main`; nenhuma release funcional justificava promocao.

## Criar a branch da politica

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git branch -d docs/005-terminal-command-policy
git switch -c chore/006-pull-request-policy
```

Objetivo: iniciar a protecao automatica a partir da integracao atualizada.

## Testar a politica de branches

```bash
bash .github/scripts/validate-pull-request-branch.test.sh

bash .github/scripts/validate-pull-request-branch.sh \
  develop chore/006-pull-request-policy

if bash .github/scripts/validate-pull-request-branch.sh \
  main feature/003-api-bootstrap
then
  echo "Unexpected policy success"
  exit 1
else
  echo "Expected policy rejection confirmed"
fi
```

Objetivo: confirmar cenarios permitidos, aceitar a entrega atual em `develop` e
rejeitar de proposito uma feature destinada a `main`.

Resultado: testes passaram; o caso incorreto retornou erro como esperado.

## Executar a validacao completa

```bash
bash -n .github/scripts/validate-pull-request-branch.sh
bash -n .github/scripts/validate-pull-request-branch.test.sh
npm run format:check
npm run lint
npm run typecheck
npm test
npm run build
git diff --check
command -v actionlint
```

Objetivo: validar sintaxe Bash, qualidade do monorepositorio e disponibilidade de
um validador adicional para workflows.

Resultado: scripts e suite completa passaram. `actionlint` nao estava instalado e
nao foi adicionado apenas para a entrega.

## Publicar a politica

```bash
git add \
  .github/scripts/validate-pull-request-branch.sh \
  .github/scripts/validate-pull-request-branch.test.sh \
  .github/workflows/pull-request.yml \
  AGENTS.md \
  docs/development/continuous-integration.md \
  docs/guides/git-workflow.md \
  docs/guides/github-repository-settings.md \
  docs/guides/terminal-commands.md

git diff --cached --check
git diff --cached --stat
git commit -m "ci(github): enforce pull request branch policy"
git push -u origin chore/006-pull-request-policy
```

Objetivo: publicar o job `Pull request policy` e sua documentacao.

Resultado: commit `fe13391`, mesclado no PR #5 depois que os dois checks passaram.

## Confirmar o Ruleset de main

```bash
gh api repos/WilliamRochaJR/code-arena/rulesets/19745468 \
  --jq '{name, enforcement, required_status_checks: [.rules[] | select(.type == "required_status_checks") | .parameters.required_status_checks[] | .context]}'
```

Objetivo: verificar se `Protect main` exige os checks corretos.

Resultado:

```text
JavaScript quality
Pull request policy
```

# 2026-07-27 - Estrategia de versionamento

## Criar a branch de documentacao

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git branch -d chore/006-pull-request-policy
git switch -c docs/007-versioning-strategy
```

Objetivo: iniciar a documentacao de SemVer, releases e tags.

## Ler o modelo de decisoes e os guias relacionados

```bash
sed -n '1,220p' docs/decisions/template.md
sed -n '1,200p' docs/decisions/README.md
sed -n '1,210p' docs/guides/git-workflow.md
sed -n '1,120p' docs/README.md
sed -n '100,160p' docs/roadmap.md
```

Objetivo: seguir o formato de ADR e evitar contradicoes com Git Flow e roadmap.

## Formatar e validar

```bash
npx prettier --write \
  docs/decisions/0004-use-semantic-versioning.md \
  docs/guides/versioning-and-releases.md \
  docs/README.md \
  docs/guides/git-workflow.md \
  docs/guides/terminal-commands.md

npm run format:check
git diff --check
git tag --list
rg -n "0004-use-semantic-versioning|versioning-and-releases" docs
git status --short --branch
git diff --stat
```

Objetivo: formatar os documentos, confirmar links e verificar que nenhuma tag
real havia sido criada.

Resultado: validacoes passaram e a lista de tags permaneceu vazia.

## Publicar a estrategia

```bash
git add \
  docs/decisions/0004-use-semantic-versioning.md \
  docs/guides/versioning-and-releases.md \
  docs/README.md \
  docs/guides/git-workflow.md \
  docs/guides/terminal-commands.md

git diff --cached --check
git diff --cached --stat
git commit -m "docs(release): define versioning and tag strategy"
git push -u origin docs/007-versioning-strategy
```

Objetivo: publicar a decisao e o guia de versionamento.

Resultado: commit `63a1afe`, mesclado no PR #6 com os dois checks aprovados.

# 2026-07-27 - Transformacao em diario cronologico

## Sincronizar develop e criar a entrega

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git branch -d docs/007-versioning-strategy
git switch -c docs/008-terminal-command-journal
```

Objetivo: partir da integracao atualizada e criar uma branch apenas para a
reestruturacao da documentacao.

Resultado: `develop` avancou ate `07c8e04`, a branch mesclada foi removida
localmente e `docs/008-terminal-command-journal` foi criada.

## Reconstruir a linha do tempo

```bash
git log --reverse \
  --date=iso-strict \
  --format="%h|%ad|%s" \
  --all

gh pr list \
  --repo WilliamRochaJR/code-arena \
  --state all \
  --limit 100 \
  --json number,title,state,baseRefName,headRefName,mergedAt,url

gh run list \
  --repo WilliamRochaJR/code-arena \
  --limit 30

git remote -v
git status --short --branch
```

Objetivo: usar commits, PRs, Actions, remotes e estado local como fontes da
reconstrucao.

Resultado: a cronologia confirmou entregas entre 22 e 27 de julho de 2026, seis
PRs mesclados no novo repositorio e os remotes `origin` e `old-origin`.

## Localizar referencias aos documentos de comandos

```bash
rg -n "terminal-commands|command-reference|Comandos de terminal|guia de terminal" \
  AGENTS.md README.md docs apps packages
```

Objetivo: encontrar links e rotulos que ainda tratavam o diario como uma
referencia resumida.

Resultado: foram encontrados dois rotulos a corrigir no guia do GitHub e no
README da web; os demais links estavam coerentes com a nova separacao.

## Formatar e validar a transformacao do diario

```bash
npx prettier --write \
  AGENTS.md \
  apps/web/README.md \
  docs/README.md \
  docs/development/code-quality.md \
  docs/development/definition-of-done.md \
  docs/guides/command-reference.md \
  docs/guides/github-repository-settings.md \
  docs/guides/terminal-commands.md

npm run format:check
git diff --check
git status --short --branch
git diff --stat
```

Objetivo: formatar todos os documentos alterados, validar o repositorio e
conferir o escopo da entrega.

Resultado: Prettier e `git diff --check` passaram; a arvore continha somente os
arquivos esperados da reestruturacao.

## Criar o commit do diario

```bash
git add \
  AGENTS.md \
  apps/web/README.md \
  docs/README.md \
  docs/development/code-quality.md \
  docs/development/definition-of-done.md \
  docs/guides/github-repository-settings.md \
  docs/guides/terminal-commands.md \
  docs/guides/command-reference.md

git diff --cached --check
git diff --cached --stat
git commit -m "docs(commands): create chronological terminal journal"

git status --short --branch
git log -1 --oneline --decorate
```

Objetivo: selecionar somente os arquivos da reestruturacao, revisar o stage,
criar o commit aprovado e confirmar o estado final.

Resultado: hooks de documentacao e Commitlint passaram; a arvore ficou limpa na
branch `docs/008-terminal-command-journal`.

## Publicar e confirmar a entrega do diario

```bash
git push -u origin docs/008-terminal-command-journal
git status --short --branch
gh pr view docs/008-terminal-command-journal \
  --repo WilliamRochaJR/code-arena \
  --json number,title,state,baseRefName,mergeable,statusCheckRollup,url
```

Objetivo: publicar a branch aprovada, conferir seu rastreamento e verificar o
Pull Request criado para `develop`.

Resultado: a branch foi publicada e o PR #7 foi mesclado em `develop` com os
checks `Pull request policy` e `JavaScript quality` aprovados.

# 2026-07-27 - Documentacao da estrategia de observabilidade

## Inspecionar as regras e a documentacao existente

```bash
pwd
sed -n '1,240p' AGENTS.md
find docs -maxdepth 2 -type f | sort
sed -n '1,240p' docs/README.md
tail -n 120 docs/guides/terminal-commands.md
git status --short --branch
```

Objetivo: confirmar o repositorio, as regras de colaboracao, a estrutura da
documentacao, o final do diario e o estado da branch antes da alteracao.

Resultado: o repositorio estava limpo na branch ja mesclada
`docs/008-terminal-command-journal`; nao havia um documento especifico de
observabilidade.

## Sincronizar develop e criar a entrega

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git branch -d docs/008-terminal-command-journal
git switch -c docs/009-observability-strategy

sed -n '1,260p' docs/architecture/overview.md
sed -n '1,260p' docs/roadmap.md
```

Objetivo: partir da integracao atualizada, remover a branch local ja mesclada,
criar uma branch curta e consultar a arquitetura e o roadmap.

Resultado: `develop` avancou ate `4fa222c`, a branch anterior foi removida e
`docs/009-observability-strategy` foi criada. A leitura confirmou os pontos em
que a estrategia deveria ser referenciada.

## Formatar e validar a documentacao de observabilidade

```bash
npx prettier --write \
  docs/README.md \
  docs/architecture/overview.md \
  docs/architecture/observability.md \
  docs/roadmap.md \
  docs/guides/terminal-commands.md

npm run format:check
git diff --check
git status --short --branch
git diff --stat
git diff -- docs/architecture/observability.md docs/roadmap.md
```

Objetivo: aplicar a formatacao do repositorio, validar os arquivos Markdown e
revisar o escopo e o conteudo principal da entrega.

Resultado: Prettier, `format:check` e `git diff --check` passaram. O diff ficou
restrito ao novo guia e aos documentos de arquitetura, roadmap, indice e diario
que precisam referencia-lo.

## Preparar a revisao anterior ao commit

```bash
git status --short --branch
git diff --check
npm run format:check
git diff --stat
git diff --name-status
```

Objetivo: confirmar a branch e os arquivos alterados, repetir as validacoes
obrigatorias e preparar o resumo que sera apresentado antes do commit.

Resultado: as validacoes passaram e o status confirmou cinco arquivos no
escopo. Como o novo documento ainda nao estava rastreado, os dois ultimos
comandos mostraram apenas os quatro arquivos modificados.

## Incluir o novo arquivo na revisao e validar o diario

```bash
git diff --no-index --stat /dev/null docs/architecture/observability.md || true
npx prettier --write docs/guides/terminal-commands.md
npm run format:check
git diff --check
git status --short --branch
```

Objetivo: revisar separadamente o tamanho do arquivo ainda nao rastreado,
formatar o diario atualizado e executar a validacao final antes de propor o
commit.

Resultado: o novo documento foi incluido na revisao de escopo; Prettier,
`format:check` e `git diff --check` passaram, e os cinco arquivos esperados
permaneceram na arvore de trabalho.

## Criar o commit da estrategia de observabilidade

```bash
git add \
  docs/README.md \
  docs/architecture/overview.md \
  docs/architecture/observability.md \
  docs/guides/terminal-commands.md \
  docs/roadmap.md

git diff --cached --check
git diff --cached --stat
git commit -m "docs(observability): define monitoring strategy"

git status --short --branch
git log -1 --oneline --decorate
```

Objetivo: selecionar somente a documentacao da estrategia, revisar o stage,
criar o commit aprovado e confirmar o estado final da branch.

Resultado: as validacoes do stage e os hooks passaram; o commit foi criado e a
arvore de trabalho ficou limpa.
