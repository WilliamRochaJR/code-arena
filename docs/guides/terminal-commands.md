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

## Publicar a estrategia de observabilidade

```bash
git push -u origin docs/009-observability-strategy
```

Objetivo: publicar a branch aprovada para permitir a abertura do Pull Request
direcionado a `develop`.

Resultado: a branch foi publicada no remote `origin` e passou a rastrear
`origin/docs/009-observability-strategy`. O Pull Request foi posteriormente
mesclado em `develop`.

# 2026-07-27 - Bootstrap da API Spring Boot

## Sincronizar develop e verificar o ambiente local

```bash
git fetch --prune origin
git switch develop
git pull --ff-only origin develop
git branch -d docs/009-observability-strategy

java -version
javac -version
mvn -version
docker --version
docker compose version
```

Objetivo: partir da integracao atualizada, remover a branch ja mesclada e
confirmar as ferramentas disponiveis para executar a API localmente.

Resultado: `develop` avancou ate `120c897`; a branch anterior foi removida. O
ambiente possui Java e Javac 21.0.11, Maven global 3.6.3, Docker 27.5.1 e Docker
Compose 2.30.3. O projeto usara seu proprio Maven Wrapper.

## Criar a branch da API

```bash
git switch -c feature/003-api-bootstrap
git status --short --branch
```

Objetivo: isolar o bootstrap da API em uma branch curta baseada em `develop`.

Resultado: a branch limpa `feature/003-api-bootstrap` foi criada.

## Consultar versoes e dependencias no Spring Initializr

```bash
curl -fsSL https://start.spring.io/metadata/client |
node -e '
  let input = "";
  process.stdin.on("data", chunk => input += chunk);
  process.stdin.on("end", () => {
    const metadata = JSON.parse(input);
    const wanted = new Set([
      "web", "validation", "data-jpa", "postgresql", "flyway",
      "security", "oauth2-resource-server", "actuator",
      "prometheus", "testcontainers"
    ]);
    const dependencies = metadata.dependencies.values
      .flatMap(group => group.values)
      .filter(item => wanted.has(item.id))
      .map(({ id, name }) => ({ id, name }));

    console.log(JSON.stringify({
      bootVersion: metadata.bootVersion,
      javaVersion: metadata.javaVersion,
      dependencies
    }, null, 2));
  });
'
```

Objetivo: consultar a fonte oficial antes de escolher a versao do Spring Boot e
os identificadores das dependencias.

Resultado: o Initializr informou Spring Boot 4.1.0 como release estavel padrao,
suporte a Java 21 e confirmou todos os identificadores solicitados.

## Procurar dependencias especificas de PostgreSQL e Testcontainers

```bash
curl -fsSL https://start.spring.io/metadata/client |
node -e '
  let input = "";
  process.stdin.on("data", chunk => input += chunk);
  process.stdin.on("end", () => {
    const metadata = JSON.parse(input);
    const matches = metadata.dependencies.values
      .flatMap(group => group.values)
      .filter(item =>
        /testcontainer|postgres/i.test(`${item.id} ${item.name}`)
      )
      .map(({ id, name }) => ({ id, name }));

    console.log(JSON.stringify(matches, null, 2));
  });
'
```

Objetivo: verificar se o gerador exigia um identificador adicional para testes
com PostgreSQL.

Resultado: o catalogo possui apenas `postgresql` e `testcontainers` para esse
escopo; o projeto gerado combinou ambos e incluiu o modulo PostgreSQL de
Testcontainers no `pom.xml`.

## Procurar uma convencao existente de package Java

```bash
rg -n "groupId|artifactId|base package|package Java|com\\." \
  README.md docs package.json apps packages \
  --glob '!**/node_modules/**'
```

Objetivo: evitar introduzir um group, artifact ou package em conflito com uma
convencao ja documentada.

Resultado: nenhuma convencao Java havia sido definida. Foram escolhidos o group
`com.williamrocha`, o artifact `code-arena-api` e o package
`com.williamrocha.codearena`.

## Gerar o projeto pelo Spring Initializr

```bash
test ! -e apps/api
api_archive_path="$(mktemp /tmp/code-arena-api-XXXXXX.zip)"

curl -fsSLG https://start.spring.io/starter.zip \
  --data-urlencode type=maven-project \
  --data-urlencode language=java \
  --data-urlencode bootVersion=4.1.0.RELEASE \
  --data-urlencode baseDir=code-arena-api \
  --data-urlencode groupId=com.williamrocha \
  --data-urlencode artifactId=code-arena-api \
  --data-urlencode name="Code Arena API" \
  --data-urlencode description="API REST do Code Arena" \
  --data-urlencode packageName=com.williamrocha.codearena \
  --data-urlencode packaging=jar \
  --data-urlencode javaVersion=21 \
  --data-urlencode dependencies=web,validation,data-jpa,postgresql,flyway,security,oauth2-resource-server,actuator,prometheus,testcontainers \
  --output "$api_archive_path"

unzip -l "$api_archive_path"
mkdir -p apps/api
unzip -q "$api_archive_path" -d apps/api
find apps/api -maxdepth 3 -type f | sort
```

Objetivo: baixar o scaffold oficial para um arquivo temporario, revisar o ZIP e
extrai-lo na area reservada a API.

Resultado: o Initializr gerou Maven Wrapper, aplicacao, configuracao, testes e
dependencias solicitadas. O `baseDir` do ZIP criou inicialmente o nivel extra
`apps/api/code-arena-api`.

## Corrigir o nivel do diretorio gerado e inspecionar o scaffold

```bash
test -d apps/api/code-arena-api
test ! -e apps/code-arena-api.generated
mv apps/api/code-arena-api apps/code-arena-api.generated
rmdir apps/api
mv apps/code-arena-api.generated apps/api

find apps/api -maxdepth 4 -type f | sort
sed -n '1,260p' apps/api/pom.xml
sed -n '1,160p' apps/api/src/main/resources/application.properties
git status --short
```

Objetivo: mover o conteudo gerado um nivel acima sem descartar arquivos e
revisar a estrutura, o POM e a configuracao inicial.

Resultado: a API passou a ocupar diretamente `apps/api`. O POM continha as
dependencias esperadas, incluindo Logback indiretamente pelo starter de logging,
Micrometer Prometheus e Testcontainers PostgreSQL.

## Inspecionar os fontes e executar o primeiro build

```bash
find apps/api/src -type f -print -exec sed -n '1,220p' {} \;
cd apps/api
./mvnw --version
./mvnw verify
```

Objetivo: revisar todos os fontes gerados, confirmar o Maven Wrapper e validar o
scaffold antes de personaliza-lo.

Resultado: o wrapper instalou Maven 3.9.16, mas o build falhou porque o
Initializr escreveu o parent `4.1.0.RELEASE`, coordenada ausente no Maven
Central.

## Confirmar a coordenada publicada no Maven Central

```bash
curl -fsSL \
  https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml |
sed -n '1,220p'

curl -fsSI \
  https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/4.1.0/spring-boot-starter-parent-4.1.0.pom |
sed -n '1,20p'
```

Objetivo: investigar a falha na fonte de artefatos consumida pelo Maven antes
de alterar a versao gerada.

Resultado: o Maven Central confirmou `4.1.0` como latest e release, e respondeu
HTTP 200 para o POM sem o sufixo `.RELEASE`. O parent e o roadmap foram
corrigidos para `4.1.0`.

## Validar o scaffold com a versao publicada

```bash
cd apps/api
./mvnw verify
```

Objetivo: comprovar a compilacao e a integracao inicial depois de corrigir a
coordenada do Spring Boot.

Resultado: o build passou com Java 21 e Maven Wrapper 3.9.16. O teste iniciou
PostgreSQL com Testcontainers, executou Flyway, criou o contexto JPA e carregou
a aplicacao Spring Boot 4.1.0 com sucesso.

## Validar a documentacao e o estado da primeira etapa

```bash
npx prettier --write docs/guides/terminal-commands.md docs/roadmap.md
npm run format:check
git diff --check
git status --short --branch
```

Objetivo: formatar os documentos atualizados, validar o repositorio e conferir
o escopo ao encerrar a geracao inicial da API.

Resultado: a formatacao dos dois documentos passou, mas `format:check`
identificou que o `apps/api/HELP.md` gerado pelo Initializr ainda nao seguia o
Prettier do monorepositorio. A branch continha somente o scaffold e as
atualizacoes esperadas de roadmap e diario.

## Formatar o documento gerado e repetir as validacoes

```bash
npx prettier --write apps/api/HELP.md
npm run format:check
git diff --check
git status --short --branch
```

Objetivo: adequar o Markdown gerado ao padrao do monorepositorio e repetir as
validacoes da etapa.

Resultado: Prettier, `format:check` e `git diff --check` passaram. A branch
permaneceu sem commits, contendo o scaffold em `apps/api` e as atualizacoes
esperadas de roadmap e diario.

# 2026-07-28 - Configuracao da fundacao da API

## Inspecionar scaffold, CI e documentacao

```bash
git status --short --branch
find apps/api -path '*/target' -prune -o -type f -print | sort
sed -n '1,260p' apps/api/pom.xml
sed -n '1,240p' apps/web/README.md
sed -n '1,280p' .github/workflows/pull-request.yml
rg -n "docker compose|compose.yaml|actuator|SecurityFilterChain|issuer-uri" \
  . --glob '!**/node_modules/**' --glob '!apps/api/target/**'
tail -n 100 docs/guides/terminal-commands.md
```

Objetivo: revisar a arvore gerada, os padroes de documentacao, a workflow e
configuracoes existentes antes de personalizar o bootstrap.

Resultado: nao havia Compose, configuracao explicita de seguranca nem job Java.
O scaffold continha somente as configuracoes padrao do Initializr.

## Localizar o suporte de MockMvc no Spring Boot 4

```bash
jar tf \
  ~/.m2/repository/org/springframework/boot/spring-boot-webmvc-test/4.1.0/spring-boot-webmvc-test-4.1.0.jar |
rg 'AutoConfigureMockMvc|MockMvc'
```

Objetivo: confirmar o novo package da anotacao de teste depois da reorganizacao
de modulos do Spring Boot 4.

Resultado: `AutoConfigureMockMvc` foi localizado em
`org.springframework.boot.webmvc.test.autoconfigure`.

## Formatar configuracoes, validar o Compose e testar a API

```bash
npx prettier --write \
  .github/workflows/pull-request.yml \
  apps/api/README.md \
  apps/api/src/main/resources/application.yml \
  apps/api/src/main/resources/application-local.yml \
  compose.yaml \
  docs/roadmap.md

docker compose config
cd apps/api
./mvnw --batch-mode --no-transfer-progress verify
```

Objetivo: formatar os arquivos suportados, validar a definicao do PostgreSQL
local e testar configuracoes, seguranca e health check.

Resultado: o Compose foi considerado valido e os tres testes iniciais passaram.
O relatorio do contexto revelou que o exporter Prometheus nao estava ativo no
ambiente de teste.

## Localizar propriedades de metricas no cache Maven

```bash
for metadata_jar in \
  ~/.m2/repository/org/springframework/boot/spring-boot-*-autoconfigure/4.1.0/*.jar
do
  unzip -p "$metadata_jar" META-INF/spring-configuration-metadata.json 2>/dev/null
done |
rg -C 3 '"name": "management\..*(prometheus|metrics.export)'
```

Objetivo: procurar a propriedade oficial de Prometheus nos modulos de
autoconfiguracao mais comuns do Spring Boot 4.

Resultado: o primeiro filtro encontrou apenas metadados legados de Wavefront,
exigindo uma busca em todos os modulos do Boot.

## Localizar o modulo e as propriedades de Prometheus

```bash
find ~/.m2/repository/org/springframework/boot \
  -path '*/4.1.0/*.jar' -type f |
sort |
while read -r boot_jar
do
  if unzip -p "$boot_jar" META-INF/spring-configuration-metadata.json 2>/dev/null |
    rg -qi 'prometheus'
  then
    echo "$boot_jar"
    unzip -p "$boot_jar" META-INF/spring-configuration-metadata.json |
      rg -C 4 'prometheus'
  fi
done
```

Objetivo: identificar a propriedade atual sem depender de nomes de modulos
anteriores.

Resultado: o modulo `spring-boot-micrometer-metrics` confirmou
`management.prometheus.metrics.export.enabled` e o endpoint Prometheus.

## Confirmar o controle global de exporters

```bash
find ~/.m2/repository/org/springframework/boot \
  -path '*/4.1.0/*.jar' -type f |
sort |
while read -r boot_jar
do
  unzip -p "$boot_jar" META-INF/spring-configuration-metadata.json 2>/dev/null
done |
rg -C 4 'management\.defaults\.metrics\.export\.enabled'
```

Objetivo: entender por que o exporter estava desativado apesar do default
especifico do Prometheus.

Resultado: a propriedade global tambem possui default `true`; o ambiente de
teste do Spring Boot a desativa para evitar exporters acidentais. O Prometheus
foi ativado explicitamente na aplicacao.

## Repetir os testes com o endpoint Prometheus

```bash
cd apps/api
./mvnw --batch-mode --no-transfer-progress verify
```

Objetivo: comprovar que health, Prometheus e a politica provisoria de seguranca
funcionam com PostgreSQL real em Testcontainers.

Resultado: quatro testes passaram, o contexto expos tres endpoints Actuator e
`/actuator/prometheus` respondeu com sucesso.

## Revisar documentos afetados pela API e CI

```bash
sed -n '1,260p' README.md
sed -n '1,300p' docs/development/continuous-integration.md
sed -n '1,260p' docs/development/definition-of-done.md
```

Objetivo: localizar referencias que ainda tratavam a API e o job Java como
trabalho futuro.

Resultado: o README raiz e o guia de CI precisavam refletir a API criada, o
Maven Wrapper e o terceiro job da workflow.

## Localizar os checks documentados dos Rulesets

```bash
rg -n -C 5 "JavaScript quality|required|obrigatorio|status check" \
  docs/guides/github-repository-settings.md
```

Objetivo: encontrar onde registrar a inclusao futura de `Java quality` como
check obrigatorio.

Resultado: o guia listava apenas `JavaScript quality` nos dois Rulesets e `Pull
request policy` em `main`. Foi adicionada a instrucao para configurar `Java
quality` depois de sua primeira execucao no GitHub.

## Executar a validacao final da fundacao

```bash
npx prettier --write \
  README.md \
  .github/workflows/pull-request.yml \
  apps/api/README.md \
  apps/api/src/main/resources/application.yml \
  apps/api/src/main/resources/application-local.yml \
  compose.yaml \
  docs/development/continuous-integration.md \
  docs/guides/github-repository-settings.md \
  docs/guides/terminal-commands.md \
  docs/roadmap.md

npm run format:check
git diff --check
docker compose config --quiet
cd apps/api
./mvnw --batch-mode --no-transfer-progress verify
cd ../..
git status --short --branch
git diff --stat
```

Objetivo: formatar toda a documentacao e configuracao alterada, validar
whitespace e Compose, repetir o build Java e revisar o escopo final.

Resultado: Prettier, `format:check`, `git diff --check`, Compose e Maven
passaram. Os quatro testes da API ficaram verdes e o diff permaneceu restrito ao
bootstrap, CI, execucao local e documentacao relacionada.

## Revisar o escopo antes da proposta de commit

```bash
git status --short --branch
find apps/api -path '*/target' -prune -o -type f -print | sort
git diff --check
git diff --stat
git diff --name-status
git diff -- .github/workflows/pull-request.yml README.md \
  docs/development/continuous-integration.md \
  docs/guides/github-repository-settings.md docs/roadmap.md
git check-ignore -v \
  apps/api/target \
  apps/api/target/code-arena-api-0.0.1-SNAPSHOT.jar
```

Objetivo: conferir todos os arquivos da fundacao da API, revisar as alteracoes
rastreadas e garantir que artefatos Maven nao entrariam no commit.

Resultado: o escopo correspondeu ao bootstrap planejado, `git diff --check`
passou e `apps/api/.gitignore` ignorou tanto `target` quanto o JAR gerado.

## Consultar o ponto de insercao no diario

```bash
tail -n 100 docs/guides/terminal-commands.md
git status --short --branch
```

Objetivo: localizar o final do diario para registrar a revisao pre-commit e
reconfirmar a branch e o estado do repositorio.

Resultado: o ultimo registro era a validacao da fundacao e a branch continuava
como `feature/003-api-bootstrap`, apenas com as mudancas esperadas.

## Validar a documentacao depois do registro pre-commit

```bash
npx prettier --write docs/guides/terminal-commands.md
npm run format:check
git diff --check
git status --short --branch
git diff --stat
```

Objetivo: formatar o novo registro, repetir as validacoes aplicaveis e produzir
o resumo final que sera apresentado antes de qualquer commit.

Resultado: o Prettier nao precisou alterar o diario, `format:check` e
`git diff --check` passaram, e a revisao final confirmou a branch
`feature/003-api-bootstrap` com somente o escopo esperado.

## Diagnosticar a falha ao iniciar a API localmente

```bash
docker compose ps
docker compose logs --tail=80 postgres
cd apps/api
SPRING_PROFILES_ACTIVE=local \
  ./mvnw --batch-mode --no-transfer-progress spring-boot:run
```

Objetivo: reproduzir a falha relatada e identificar se sua origem estava no
PostgreSQL, no perfil local ou na inicializacao do Spring Boot.

Resultado: a API ativou corretamente o perfil `local`, mas o Flyway nao
conseguiu autenticar o usuario `code_arena` no PostgreSQL. O Compose nao mostrou
o servico do projeto em execucao.

## Identificar o processo que ocupa a porta do PostgreSQL

```bash
docker compose ps --all
docker ps \
  --filter publish=5432 \
  --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
ss -ltnp 'sport = :5432'
compgen -e | rg '^DB_(URL|USERNAME|PASSWORD)$' || true
```

Objetivo: verificar o estado do container, descobrir quem escutava na porta
`5432` e detectar sobrescritas por variaveis de ambiente sem revelar valores.

Resultado: o container `code-arena-postgres-1` permaneceu no estado `Created`,
nenhum container publicou a porta `5432` e um PostgreSQL externo ao Compose
escutava em `127.0.0.1:5432`. Nao havia variaveis `DB_*` sobrescrevendo a
configuracao local.

## Localizar referencias a porta do PostgreSQL

```bash
rg -n -C 3 '5432|DB_URL' \
  compose.yaml \
  apps/api/src/main/resources/application-local.yml \
  apps/api/README.md \
  README.md \
  docs
git status --short --branch
```

Objetivo: identificar todos os locais que precisavam acompanhar a mudanca da
porta externa do PostgreSQL e reconfirmar o escopo pendente.

Resultado: a porta aparecia no Compose, no perfil local e no diagrama de
arquitetura. O README da API descrevia as variaveis, mas ainda nao explicitava a
porta. A configuracao foi ajustada para publicar `5433` no host e preservar
`5432` dentro do container.

## Recriar o PostgreSQL na porta externa 5433

```bash
docker compose up -d --wait postgres
docker compose ps
docker compose logs --tail=40 postgres
```

Objetivo: aplicar a nova publicacao de porta, aguardar o healthcheck e inspecionar
a inicializacao do banco.

Resultado: o Compose recriou o container, que ficou saudavel e publicou
`0.0.0.0:5433` para a porta interna `5432`. O PostgreSQL informou que estava
pronto para aceitar conexoes.

## Iniciar a API com o perfil local

```bash
cd apps/api
SPRING_PROFILES_ACTIVE=local \
  ./mvnw --batch-mode --no-transfer-progress spring-boot:run
```

Objetivo: comprovar a inicializacao completa da aplicacao contra o PostgreSQL do
Compose.

Resultado: o Flyway conectou em
`jdbc:postgresql://localhost:5433/code_arena`, o Hibernate inicializou e o Tomcat
passou a aceitar requisicoes na porta `8080`.

## Verificar os endpoints locais

```bash
curl --fail --silent --show-error \
  http://localhost:8080/actuator/health
curl --fail --silent --show-error \
  http://localhost:8080/actuator/prometheus |
sed -n '1,12p'
```

Objetivo: confirmar a saude da aplicacao e a exposicao de metricas Prometheus
durante uma execucao local real.

Resultado: o health respondeu com status `UP`, e o endpoint Prometheus retornou
metricas da aplicacao. O processo Spring Boot foi encerrado com `Ctrl+C` e
concluiu o graceful shutdown com `BUILD SUCCESS`; o PostgreSQL permaneceu ativo.

## Validar a alteracao da porta local

```bash
npx prettier --write \
  compose.yaml \
  apps/api/README.md \
  apps/api/src/main/resources/application-local.yml \
  docs/architecture/overview.md \
  docs/guides/terminal-commands.md
npm run format:check
git diff --check
docker compose config --quiet
cd apps/api
./mvnw --batch-mode --no-transfer-progress verify
cd ../..
git status --short --branch
git diff --stat
```

Objetivo: formatar os arquivos alterados, validar o Compose e repetir toda a
verificacao automatizada da API antes de propor o commit.

Resultado: os arquivos ja estavam formatados, `format:check`,
`git diff --check` e a validacao do Compose passaram. O Maven concluiu com
`BUILD SUCCESS`; os quatro testes da API passaram sem falhas.

## Revisar o escopo depois da correcao local

```bash
npm run format:check
git diff --check
git status --short --branch
git diff --stat
git diff --name-status
```

Objetivo: reconfirmar a qualidade e apresentar o escopo atualizado antes de
solicitar aprovacao para criar o commit.

Resultado: a formatacao e o whitespace passaram. A branch continuava como
`feature/003-api-bootstrap`, com a fundacao da API, o Compose, a CI e a
documentacao relacionada ainda sem commit.

## Criar o commit da fundacao da API

```bash
git add \
  .github/workflows/pull-request.yml \
  README.md \
  apps/api \
  compose.yaml \
  docs/architecture/overview.md \
  docs/development/continuous-integration.md \
  docs/guides/github-repository-settings.md \
  docs/guides/terminal-commands.md \
  docs/roadmap.md
git diff --cached --check
git diff --cached --stat
git status --short --branch
git commit -m "feat(api): bootstrap Spring Boot application"
```

Objetivo: preparar somente o escopo revisado, conferir o diff staged e criar um
commit Conventional Commits com a fundacao executavel da API.

Resultado: o mantenedor aprovou explicitamente o escopo e a mensagem. O commit
foi criado inicialmente como `ec219ae`, mas a verificacao staged avisou sobre
uma linha em branco extra no fim de `SecurityConfiguration.java`.

## Confirmar o commit e inspecionar o aviso de whitespace

```bash
git status --short --branch
git log -1 --oneline
tail -n 8 \
  apps/api/src/main/java/com/williamrocha/codearena/config/SecurityConfiguration.java
git show --check --oneline HEAD
tail -c 32 \
  apps/api/src/main/java/com/williamrocha/codearena/config/SecurityConfiguration.java |
od -An -t x1
```

Objetivo: confirmar o commit criado e determinar precisamente a origem do aviso
antes de corrigir seu conteudo.

Resultado: a branch ficou limpa no commit `ec219ae`. O `git show --check`
confirmou uma linha em branco extra no fim do arquivo, e a leitura hexadecimal
mostrou duas quebras de linha consecutivas (`0a 0a`) depois da ultima chave.

## Corrigir o whitespace no mesmo commit

```bash
set -e
npm run format:check
git diff --check
cd apps/api
./mvnw --batch-mode --no-transfer-progress verify
cd ../..
git diff --stat
git diff -- \
  apps/api/src/main/java/com/williamrocha/codearena/config/SecurityConfiguration.java \
  docs/guides/terminal-commands.md
git add \
  apps/api/src/main/java/com/williamrocha/codearena/config/SecurityConfiguration.java \
  docs/guides/terminal-commands.md
git diff --cached --check
git diff --cached --stat
git commit --amend --no-edit
git status --short --branch
git log -1 --oneline
git show --check --oneline HEAD
```

Objetivo: remover apenas a linha extra, validar novamente a entrega e incorporar
a correcao ao commit ainda nao publicado.

Resultado: o mantenedor aprovou explicitamente o amend. A formatacao,
`git diff --check`, `git diff --cached --check` e o Maven passaram; os quatro
testes ficaram verdes. O commit foi reescrito como `7bc1b08`, a branch ficou
limpa e `git show --check` nao encontrou novos avisos.

## Revisar a posicao do controle de interrupcao

```bash
rg -n -C 8 \
  'Validar e publicar a politica|Corrigir o whitespace no mesmo commit|set -e' \
  docs/guides/terminal-commands.md
git status --short --branch
```

Objetivo: investigar uma alteracao documental fora do bloco atual que apareceu
no diff do amend.

Resultado: a branch estava limpa, mas o `set -e` usado na correcao havia sido
registrado por engano em uma secao historica. A linha foi movida para o bloco
correto sem alterar os comandos historicos anteriores.

## Incorporar a correcao final do diario

```bash
set -e
npm run format:check
git diff --check
git diff --stat
git diff -- docs/guides/terminal-commands.md
git add docs/guides/terminal-commands.md
git diff --cached --check
git diff --cached --stat
git commit --amend --no-edit
git status --short --branch
git log -1 --oneline
git show --check --oneline HEAD
```

Objetivo: validar a movimentacao do comando no diario e incorpora-la ao mesmo
commit local da fundacao da API.

Resultado: o mantenedor aprovou explicitamente o ultimo amend documental antes
da execucao. O hash final sera confirmado pelo Git ao fim do bloco.
