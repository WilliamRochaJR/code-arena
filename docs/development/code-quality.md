# Estrategia de qualidade de codigo

Este documento explica como as ferramentas de qualidade do Code Arena se
complementam, quando cada uma e executada e qual problema ela resolve. Os
comandos de terminal usados no projeto permanecem no
[guia de terminal](../guides/terminal-commands.md).

## Objetivo

A estrategia procura detectar problemas o mais cedo possivel sem depender de
uma unica ferramenta ou apenas da disciplina individual.

```text
Durante a edicao
      |
      v
ESLint + TypeScript + Prettier
      |
      v
git commit
      |
      +-- pre-commit --> lint-staged
      |
      `-- commit-msg --> Commitlint
                            |
                            v
                      Pull Request
                            |
                            v
                  GitHub Actions / CI
```

As verificacoes locais oferecem feedback rapido. A CI repete as validacoes em
um ambiente independente e sera a fonte de verdade antes do merge.

## ESLint

ESLint realiza analise estatica de JavaScript e TypeScript. Ele identifica
padroes incorretos ou perigosos sem precisar executar a aplicacao.

Exemplos do que pode detectar:

- variaveis declaradas e nao utilizadas;
- uso incorreto de React Hooks;
- dependencias ausentes em efeitos;
- codigo incompativel com Fast Refresh;
- regras arquitetonicas que forem adicionadas ao projeto.

Execucao manual:

```bash
npm run lint
```

O ESLint roda:

- manualmente durante o desenvolvimento;
- em arquivos staged pelo lint-staged;
- na CI para validar o repositorio de forma independente.

ESLint nao substitui testes. Ele encontra padroes no codigo, mas nao confirma se
uma regra de negocio produz o resultado esperado.

## Prettier

Prettier formata codigo e documentacao de maneira deterministica. Seu objetivo e
eliminar discussoes e diffs desnecessarios sobre estilo.

Ele padroniza, por exemplo:

- indentacao;
- aspas e ponto e virgula;
- quebras de linha;
- virgulas finais;
- tabelas e blocos Markdown.

Execucao manual:

```bash
npm run format
npm run format:check
```

Prettier nao analisa regras de negocio ou problemas do React. Por isso ele nao
substitui ESLint ou TypeScript.

## TypeScript

O compilador TypeScript valida os contratos estaticos entre componentes,
funcoes, pacotes e respostas modeladas da API.

```bash
npm run typecheck
```

Exemplos de problemas detectados:

- propriedade obrigatoria ausente;
- tipo de argumento incorreto;
- acesso possivelmente indefinido;
- contrato incompativel entre o SDK e seu consumidor;
- import ou export invalido.

TypeScript nao valida dados recebidos em runtime. Respostas HTTP e formularios
ainda precisam de validacao apropriada, como Zod no frontend e Bean Validation
no backend.

## Vitest

Vitest executa testes automatizados de funcoes, componentes e integracoes do
ecossistema TypeScript.

```bash
npm test
```

Ele fornece `describe`, `it`, `expect`, mocks, spies e cobertura. React Testing
Library sera usada em conjunto para testar componentes pela forma como o usuario
interage com a interface.

Testes verificam comportamento. Eles complementam as verificacoes estaticas ao
cobrir casos de sucesso, erros, limites e regressoes.

## Husky

Husky conecta scripts aos hooks nativos do Git. Ele nao realiza lint nem valida
mensagens por conta propria; apenas dispara as ferramentas configuradas no
momento correto.

Hooks atuais:

```text
pre-commit --> npx lint-staged
commit-msg --> npx commitlint --edit "$1"
```

O Husky fica na raiz porque existe um unico repositorio Git para todo o
monorepositorio. Assim, os hooks protegem web, SDK, UI, documentacao e, no futuro,
a API.

## lint-staged

lint-staged seleciona apenas os arquivos adicionados ao proximo commit e executa
as verificacoes configuradas para suas extensoes.

```text
*.js, *.jsx, *.ts, *.tsx
  --> ESLint com correcoes seguras
  --> Prettier

*.css, *.html, *.json, *.md, *.mjs, *.yaml, *.yml
  --> Prettier
```

Executar apenas arquivos staged torna o `pre-commit` rapido e evita que uma
tarefa pequena seja bloqueada por formatacao de arquivos nao relacionados.

O lint-staged cria um backup temporario enquanto trabalha. Se uma tarefa falhar,
ele restaura o estado anterior e impede o commit.

## Commitlint

Commitlint valida a mensagem de commit. O projeto usa a configuracao Conventional
Commits:

```text
tipo(escopo): descricao
```

Exemplos:

```text
feat(web): add quiz configuration page
fix(sdk): handle request timeout
docs(api): describe validation errors
chore(workspace): configure shared tooling
```

Isso cria um historico pesquisavel e permite entender a intencao de cada mudanca
sem abrir o diff. No futuro, o padrao tambem pode apoiar changelog e versionamento
automatizados.

Commitlint nao avalia a qualidade do codigo nem confirma se o commit esta
pequeno. Ele valida apenas a estrutura da mensagem.

## GitHub Actions

Hooks locais melhoram o feedback, mas podem ser ignorados com `--no-verify` ou
falhar por diferencas de ambiente. Por isso a CI repetira as verificacoes no Pull
Request:

```text
format:check
lint
typecheck
test
build
```

O repositorio publico possui Rulesets ativos para `main` e `develop`. O check
`JavaScript quality` e obrigatorio antes do merge e executa esse conjunto de
validacoes em um ambiente independente.

## Quando cada verificacao roda

| Momento                   | Ferramenta                  | Escopo                       | Objetivo                                  |
| ------------------------- | --------------------------- | ---------------------------- | ----------------------------------------- |
| Durante o desenvolvimento | ESLint, TypeScript e testes | Escolhido pelo desenvolvedor | Feedback rapido                           |
| Antes do commit           | Husky + lint-staged         | Arquivos staged              | Impedir problemas simples no commit       |
| Mensagem do commit        | Husky + Commitlint          | Mensagem                     | Padronizar o historico                    |
| Pull Request              | GitHub Actions              | Repositorio/workspaces       | Validacao independente                    |
| Antes de release          | Pipeline completo e E2E     | Sistema                      | Confirmar que a versao pode ser publicada |

## Por que nao executar tudo no pre-commit

Um hook lento incentiva seu bypass e prejudica o ciclo de desenvolvimento. O
`pre-commit` executa apenas checks rapidos nos arquivos staged. Typecheck, suite
completa e build rodam manualmente e na CI.

Quando a API Spring Boot for adicionada, testes Maven completos tambem ficarao na
CI. O hook local deve receber apenas verificacoes Java rapidas se houver beneficio
mensuravel.

## Responsabilidade final

Ferramentas reduzem erros repetitivos, mas nao substituem:

- refinamento de requisitos;
- revisao do diff;
- bons testes;
- code review;
- validacao de QA;
- observabilidade depois do deploy.

A qualidade resulta da combinacao entre automacao, processo e julgamento
tecnico.
