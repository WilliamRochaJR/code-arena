# GitHub

## Conferir autenticacao e acesso

```bash
gh --version
gh auth status
gh auth setup-git
ssh -T git@github.com
git ls-remote <remote-ou-url>
```

Os comandos identificam a conta ativa e confirmam acesso sem documentar tokens.

## Consultar repositorio e Pull Requests

```bash
gh repo view <owner>/<repositorio> \
  --json nameWithOwner,visibility,defaultBranchRef,url,viewerPermission
gh pr status --repo <owner>/<repositorio>
gh pr list \
  --repo <owner>/<repositorio> \
  --state <estado> \
  --json number,title,baseRefName,headRefName,url
gh pr view <numero-ou-branch> \
  --repo <owner>/<repositorio> \
  --json number,title,state,baseRefName,headRefName,mergeable,statusCheckRollup,url
```

Sao consultas de metadados, base, origem, estado de merge e checks.

## Consultar Issues

```bash
gh issue view <numero> \
  --repo <owner>/<repositorio> \
  --json title,body,state,url
```

Consulta o escopo, os criterios e o estado de uma Issue sem altera-la.

Se a versao instalada do GitHub CLI falhar ao consultar campos descontinuados,
use a API REST somente leitura:

```bash
gh api repos/<owner>/<repositorio>/issues/<numero> \
  --jq '{title, state, body, html_url}'
```

A alternativa retorna apenas os campos necessarios e nao altera a Issue.

## Criar e concluir Issues

```bash
gh issue create \
  --repo <owner>/<repositorio> \
  --title "<titulo>" \
  --label <label> \
  --body-file <arquivo-ou-entrada-padrao>

gh issue close <numero> \
  --repo <owner>/<repositorio> \
  --reason completed \
  --comment "<resumo-da-entrega>"
```

O primeiro cria uma Issue a partir de uma descricao preparada. O segundo
registra a entrega e conclui manualmente uma Issue quando o fluxo por `develop`
nao aciona o fechamento automatico da branch padrao.

## Consultar Actions

```bash
gh run list \
  --repo <owner>/<repositorio> \
  --branch <branch> \
  --limit <quantidade>
```

Lista as execucoes recentes da branch.

## Consultar a API do GitHub

```bash
gh api repos/<owner>/<repositorio>
gh api repos/<owner>/<repositorio>/rulesets
gh api repos/<owner>/<repositorio>/rulesets/<id>
```

Sem metodo ou campos de escrita, apenas consulta configuracoes.

## Validar a politica de Pull Requests

```bash
bash -n .github/scripts/validate-pull-request-branch.sh
bash -n .github/scripts/validate-pull-request-branch.test.sh
bash .github/scripts/validate-pull-request-branch.test.sh
bash .github/scripts/validate-pull-request-branch.sh <base> <origem>
```

`bash -n` valida sintaxe. Os scripts testam direcoes como
`release/1.0.0 -> main`.

## Montar o link de um Pull Request

```text
https://github.com/<owner>/<repositorio>/compare/<base>...<branch-codificada>?expand=1&title=<titulo-codificado>&body=<descricao-codificada>
```

Escolha `develop` para `feature/*`, `fix/*`, `docs/*` e `chore/*`; escolha
`main` para `release/*` e `hotfix/*`. Codifique a `/` da branch como `%2F`:

```text
docs/011-web-readme-quality -> docs%2F011-web-readme-quality
```

O link generico `/pull/new/<branch>` retornado pelo push pode selecionar `main`
por ser a branch padrao. Ao abrir a comparacao, confirme visualmente os campos
`base` e `compare` antes de criar o Pull Request. Use URL encoding no titulo e
na descricao baseada em `.github/pull_request_template.md`; assim o formulario
abre com ambos preenchidos e prontos para revisao.
