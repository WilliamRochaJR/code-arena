# Configuracao do repositorio no GitHub

Este guia registra as configuracoes aplicadas ao repositorio hospedado. Elas nao
fazem parte do historico Git e precisam ser recriadas ao migrar o projeto para
outro repositorio.

Git controla commits, branches e tags. O GitHub adiciona Pull Requests,
Rulesets, Actions, Issues, secrets e configuracoes de colaboracao.

## Estado esperado

```text
Visibility: Public
Default branch: main
Permanent branches: main, develop
Short-lived branches: feature/*, release/*, hotfix/*
```

`main` representa releases estaveis. `develop` integra a proxima versao.

## Rulesets

O repositorio publico usa dois Rulesets, um para cada branch permanente. A
separacao permite que requisitos de release e integracao evoluam de forma
independente.

### Proteger main

No GitHub, acesse `Settings > Rules > Rulesets > New ruleset > New branch
ruleset` e configure:

```text
Ruleset name: Protect main
Enforcement status: Active
Bypass list: vazia
Target branches: Include default branch
```

Ative:

- `Restrict deletions`;
- `Require a pull request before merging`;
- `Require conversation resolution before merging`;
- `Block force pushes`.

Em `Require a pull request before merging`, mantenha zero aprovacoes
obrigatorias enquanto o repositorio tiver um unico mantenedor. O autor nao pode
aprovar o proprio Pull Request; exigir uma aprovacao impediria o fluxo sem
adicionar uma revisao independente.

### Proteger develop

Crie outro branch ruleset com:

```text
Ruleset name: Protect develop
Enforcement status: Active
Bypass list: vazia
Target branches: Include by pattern: develop
```

Ative as mesmas regras usadas em `Protect main`, inclusive Pull Request
obrigatorio com zero aprovacoes.

### Por que nao permitir bypass

Uma bypass list vazia faz as regras valerem tambem para o mantenedor. Isso evita
que um push direto acidental ignore o processo de Pull Request.

Um bypass temporario so deve ser considerado para recuperacao de incidente, com
escopo e motivo registrados.

## Status checks

Depois da primeira execucao bem-sucedida do workflow no repositorio, a regra
`Require status checks to pass` foi ativada nos dois Rulesets com o check:

```text
JavaScript quality
```

O check obrigatorio garante:

```text
format:check
lint
typecheck
test
build
```

O Ruleset `Protect main` tambem deve exigir:

```text
Pull request policy
```

Esse check permite em `main` somente Pull Requests originados de `release/*` ou
`hotfix/*`. Ele precisa executar com sucesso ao menos uma vez antes de aparecer
na lista de status checks do Ruleset.

O GitHub precisa reconhecer um check em uma execucao anterior para oferece-lo na
configuracao do Ruleset. Por isso a regra so foi ativada depois que o workflow
passou no novo repositorio, evitando bloquear os merges com um requisito ainda
indisponivel.

`Require branches to be up to date before merging` permanece desativado. Essa
opcao pode ser reavaliada quando houver mais contribuidores ou concorrencia
frequente de Pull Requests.

Nao exigir inicialmente:

- commits assinados, ate que a assinatura seja configurada;
- historico linear, pois o projeto preserva merge commits de Pull Requests;
- deployments, ate que exista um ambiente de entrega;
- code scanning, ate que a ferramenta correspondente seja adotada.

## Pull Requests e merge

Em `Settings > General > Pull Requests`, preserve a estrategia de merge definida
para o projeto e ative `Automatically delete head branches`.

A automacao deve apagar somente branches curtas depois do merge. `main` e
`develop` permanecem protegidas contra exclusao pelos Rulesets.

Antes do merge, confirme explicitamente:

```text
feature/* --> develop
release/* --> main
hotfix/*  --> main
```

`develop` e a base das features porque funciona como a branch de integracao da
proxima versao. Ela permite combinar e validar entregas antes de promover uma
release para `main`. Abrir uma feature diretamente contra `main` pula essa
integracao e mistura trabalho em desenvolvimento com o estado estavel do
produto.

O link generico criado depois de um push seleciona a branch padrao `main`. Para
abrir o formulario com a base correta, use:

```text
https://github.com/WilliamRochaJR/code-arena/compare/develop...<branch>?quick_pull=1
```

Se um PR ainda for aberto contra `main` com uma origem inadequada, o check `Pull
request policy` falha e o Ruleset impede o merge.

Correcoes de release e hotfix tambem precisam ser incorporadas em `develop`.
Nunca use `develop` como head descartavel de um Pull Request para `main`.

## GitHub Actions

Em `Settings > Actions > General`, permita a execucao das actions oficiais usadas
pelo workflow:

```text
actions/checkout
actions/setup-node
```

O workflow atual declara somente permissao de leitura do conteudo:

```yaml
permissions:
  contents: read
```

Permissoes de escrita devem ser adicionadas apenas quando uma entrega justificar
seu uso. Uma restricao de billing pode impedir o inicio dos jobs mesmo quando o
arquivo do workflow esta correto; nesse caso, os mesmos comandos devem ser
executados localmente e a restricao tratada separadamente.

## Itens nao transferidos pelo Git

Ao migrar para outro repositorio, recrie e valide:

- Rulesets e protecoes de branches;
- configuracoes de merge e exclusao automatica;
- Actions e permissoes do workflow;
- secrets e variables;
- environments;
- Issues, Pull Requests e labels, quando precisarem ser preservados;
- webhooks, GitHub Apps e integracoes externas.

Os comandos usados na migracao do Code Arena estao no
[diario de comandos do terminal](terminal-commands.md).

## Checklist

- `main` e a branch padrao;
- `Protect main` esta ativo;
- `Protect develop` esta ativo;
- Pull Request e obrigatorio nas branches permanentes;
- force push e exclusao estao bloqueados;
- conversas precisam ser resolvidas;
- bypass list permanece vazia;
- branches curtas sao apagadas depois do merge;
- `JavaScript quality` e obrigatorio nos dois Rulesets;
- `Pull request policy` e obrigatorio em `Protect main`;
- atualizacao da branch antes do merge nao e obrigatoria nesta etapa;
- secrets e permissoes nao sao expostos na documentacao.
