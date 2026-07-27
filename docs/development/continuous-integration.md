# Integracao continua

O GitHub Actions valida cada Pull Request destinado a `develop` ou `main`. O
objetivo e repetir os checks em um ambiente independente antes do merge.

Workflow:

```text
.github/workflows/pull-request.yml
```

## Quando executa

```text
Pull Request --> develop
Pull Request --> main
```

Novos commits no mesmo PR cancelam a execucao anterior. Isso evita gastar tempo
com uma revisao que ja foi substituida.

## Permissoes

O workflow declara apenas:

```yaml
permissions:
  contents: read
```

Ele precisa ler o repositorio, mas nao cria commits, comentarios, releases ou
deploys. Pipelines futuros devem receber somente as permissoes necessarias.

## Ambiente

- Runner hospedado pelo GitHub com Ubuntu.
- Node obtido de `.nvmrc`.
- Cache de dependencias npm administrado por `actions/setup-node`.
- Instalacao reproduzivel com `npm ci` e `package-lock.json`.
- Timeout de dez minutos para evitar jobs presos indefinidamente.

O workflow usa actions oficiais nas linhas atuais:

```text
actions/checkout@v6
actions/setup-node@v6
```

## Etapas

O workflow possui dois jobs independentes:

```text
Pull request policy              JavaScript quality
        |                                |
testar politica                         checkout
        |                                |
validar base e origem                setup Node
                                         |
                                      npm ci
                                         |
                          format, lint, types, tests, build
```

Separar os jobs permite distinguir uma violacao do fluxo de branches de uma
falha de qualidade JavaScript.

### Politica do Pull Request

O job `Pull request policy` executa:

```bash
bash .github/scripts/validate-pull-request-branch.test.sh
bash .github/scripts/validate-pull-request-branch.sh "$BASE_REF" "$HEAD_REF"
```

Pull Requests destinados a `main` so podem partir de `release/*` ou `hotfix/*`.
Uma feature, documentacao, chore, fix ou a propria `develop` direcionada a
`main` faz o job falhar.

Para `develop`, o job aceita branches curtas sem restringir seus prefixos nesta
primeira versao. O fluxo esperado continua documentado e pode ser endurecido
quando houver uma necessidade observada.

### Qualidade JavaScript

```text
checkout
   |
setup Node + cache
   |
npm ci
   |
format:check
   |
lint
   |
typecheck
   |
test
   |
build
```

### Formatacao

```bash
npm run format:check
```

Confirma que arquivos versionados seguem o Prettier sem modificar o checkout.

### Analise estatica

```bash
npm run lint
```

Executa ESLint no monorepositorio.

### Tipos

```bash
npm run typecheck
```

Valida os contratos TypeScript de todos os workspaces que declaram o script.

### Testes

```bash
npm test
```

Executa Vitest nos workspaces. A tolerancia temporaria a ausencia de casos sera
removida quando o primeiro comportamento testavel for implementado.

### Build

```bash
npm run build
```

Confirma que web, SDK e UI podem gerar seus artefatos.

## Backend futuro

Quando `apps/api` for criado, o workflow recebera um job Java independente com
JDK 21, cache Maven e `./mvnw verify`. Separar os jobs permite identificar se uma
falha pertence ao ecossistema JavaScript ou Java.

## Relacao com hooks locais

Husky e lint-staged oferecem feedback rapido antes do commit, mas podem ser
ignorados ou variar conforme a maquina. A CI executa o conjunto completo em um
ambiente limpo. As duas camadas sao complementares.

O repositorio publico possui os Rulesets `Protect main` e `Protect develop`. Os
dois exigem o job `JavaScript quality` como status check antes do merge.
`Protect main` tambem exige `Pull request policy`, depois que o GitHub reconhece
o novo check em sua primeira execucao. As validacoes locais continuam
necessarias para oferecer feedback antes do Pull Request e facilitar o
diagnostico de falhas.

## Diagnostico

Se o workflow falhar:

1. identifique a primeira etapa com erro;
2. reproduza localmente o mesmo comando;
3. corrija a causa, sem desabilitar um teste valido;
4. execute o conjunto completo;
5. envie um novo commit para o mesmo PR.
