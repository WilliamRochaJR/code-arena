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

## Montar o link de um Pull Request para develop

```text
https://github.com/<owner>/<repositorio>/compare/develop...<branch>?expand=1
```

O link generico do push pode usar `main`. Features do Code Arena usam `develop`
como base.
