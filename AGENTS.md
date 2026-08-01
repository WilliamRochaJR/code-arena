# Instrucoes para agentes

Este arquivo orienta agentes de IA que trabalham no Code Arena. As instrucoes
valem para todo o repositorio. Um `AGENTS.md` mais proximo de um arquivo pode
adicionar regras especificas sem contrariar estas diretrizes.

## Contexto do projeto

O Code Arena e uma plataforma de questionarios tecnicos. O MVP permite que uma
pessoa autenticada responda questionarios de Java e acompanhe o resultado e o
historico de tentativas.

O projeto usa um monorepositorio:

```text
apps/web/                 React + TypeScript + Vite
apps/api/                 Java 21 + Spring Boot, ainda a ser criado
packages/java-quiz-sdk/   cliente REST TypeScript independente de React
packages/ui/              componentes e tokens reutilizaveis
docs/                     produto, arquitetura, contratos, decisoes e guias
```

Consulte antes de alterar comportamento ou arquitetura:

- `docs/product/mvp.md`;
- `docs/architecture/overview.md`;
- `docs/api/contracts.md`;
- `docs/roadmap.md`;
- `docs/development/definition-of-done.md`.

## Fronteiras arquiteturais

- Componentes React nao chamam endpoints diretamente; toda comunicacao com a API
  passa pelo Java Quiz SDK.
- O SDK nao depende de React, Cognito nem componentes visuais.
- A biblioteca de UI nao conhece endpoints, autenticacao ou regras do quiz.
- A API e a fonte de verdade para regras de negocio, autorizacao, pontuacao e
  respostas corretas.
- DTOs externos permanecem separados das entidades persistidas.
- Nao exponha respostas corretas ou explicacoes antes da conclusao da tentativa.
- Nao adicione microsservicos ou dependencias sem uma necessidade da entrega
  atual.

## Fluxo de Git e GitHub

- Parta de `develop` para features, correcoes de desenvolvimento, documentacao e
  chores.
- Use branches curtas e descritivas.
- Abra Pull Requests de branches curtas para `develop`.
- Use `release/*` para promover uma versao de `develop` para `main`.
- Use `hotfix/*` a partir de `main` e incorpore a correcao tambem em `develop`.
- Nunca direcione `feature/*`, `fix/*`, `docs/*`, `chore/*` ou `develop`
  diretamente para `main`; o check `Pull request policy` deve bloquear esse
  fluxo.
- Antes de fornecer um link para criar Pull Request, determine a base pela
  politica: `feature/*`, `fix/*`, `docs/*` e `chore/*` usam `develop`;
  `release/*` e `hotfix/*` usam `main`.
- Monte o link no formato
  `https://github.com/<owner>/<repositorio>/compare/<base>...<head>?expand=1&title=<titulo-codificado>&body=<descricao-codificada>`.
  Codifique a `/` da branch de origem como `%2F` e use URL encoding no titulo e
  em toda a descricao. Nunca reutilize o link generico `/pull/new/<branch>`
  retornado pelo `git push`, pois ele pode escolher `main` e nao preenche a
  descricao.
- Ao apresentar o link, informe tambem os valores esperados de `base` e
  `compare` para que o mantenedor os confirme antes de criar o Pull Request.
- Sempre que fornecer um link para criar Pull Request, inclua no proprio link um
  titulo e uma descricao completa, para que o formulario do GitHub abra com os
  dois campos preenchidos sem o mantenedor precisar copiar ou pedir o texto. A
  descricao deve seguir `.github/pull_request_template.md`, ser baseada no diff
  e nas validacoes realmente executadas e nao marcar como concluido aquilo que
  ainda estiver pendente. Apresente tambem um resumo fora do link para permitir
  a revisao antes da criacao do PR.
- Nunca faca push direto em `main` ou `develop`.
- Nunca use force push em uma branch compartilhada.
- Use Conventional Commits com uma intencao por commit.

Consulte `docs/guides/git-workflow.md` e
`docs/guides/github-repository-settings.md` antes de publicar mudancas.

## Colaboracao com o mantenedor

- Antes de executar comandos de terminal relevantes, mostre os comandos e
  explique brevemente sua finalidade. Isso e informativo, nao um pedido de
  permissao para comandos locais seguros.
- Antes de criar um commit, mostre o escopo, o diff resumido e a mensagem
  proposta; aguarde confirmacao explicita.
- Antes de qualquer push, mostre a branch e o remote de destino; aguarde
  confirmacao explicita.
- Nao abra, edite nem mescle Pull Requests sem autorizacao explicita.
- Preserve alteracoes existentes que nao pertencam a tarefa.
- Nao use comandos destrutivos para descartar trabalho sem confirmacao.

## Qualidade

Nao presuma que `git push` execute validacoes locais. Antes de propor um commit,
execute na raiz os checks aplicaveis ao escopo da alteracao.

Para mudancas em JavaScript ou TypeScript, execute o conjunto completo:

```bash
npm run format:check
npm run lint
npm run typecheck
npm test
npm run build
```

Para mudancas somente em Markdown, execute no minimo:

```bash
npm run format:check
git diff --check
```

Durante `git commit`, o Husky executa automaticamente apenas os checks rapidos:

```text
pre-commit --> lint-staged
commit-msg --> Commitlint
```

Nao existe hook de `git push` no projeto. Quando ja existe um Pull Request para
`develop` ou `main`, enviar um novo commit atualiza o PR e dispara novamente o
workflow `Pull Request`. No primeiro push de uma branch, a CI comeca somente
depois que o Pull Request e aberto.

Os hooks locais fornecem feedback rapido, e o GitHub Actions repete
`format:check`, lint, typecheck, testes e build em um ambiente limpo. Essas
camadas sao complementares: uma tarefa so esta concluida quando as validacoes
locais aplicaveis e os checks de CI estao verdes.

Quando `apps/api` existir, use o Maven Wrapper fornecido pelo projeto e execute
os checks Java definidos na propria aplicacao, incluindo `./mvnw verify` antes
do Pull Request.

- Cubra comportamentos e correcoes de bugs com testes relevantes.
- Nao desative uma validacao apenas para fazer o pipeline passar.
- Revise o diff para evitar arquivos gerados ou alteracoes acidentais.
- Mantenha o check `JavaScript quality` verde nos Pull Requests.

## Documentacao e decisoes

- Atualize README, guias e contratos quando uma mudanca alterar o uso ou o
  comportamento do projeto.
- Mantenha em `docs/guides/terminal-commands/` um catalogo de estudo organizado
  por assunto. Antes de registrar um comando reutilizavel, confirme que ele
  ainda nao existe no catalogo.
- Nao trate automaticamente uma variante como repeticao quando ela permitir um
  contexto de uso diferente, como executar pela raiz em vez de entrar em um
  subdiretorio. Documente a variante quando essa diferenca for util para estudo.
- Nao registre datas, repeticoes, a cronologia de tarefas nem comandos
  ocasionais de leitura e diagnostico. Explique objetivo, efeito e cuidados do
  comando uma unica vez no arquivo tematico adequado.
- Nunca registre valores sensiveis. Use placeholders para URLs, identificadores
  e valores que variam.
- Antes de propor qualquer commit, revise os comandos executados durante a
  tarefa e informe ao mantenedor quais comandos reutilizaveis foram adicionados
  ao catalogo. Quando nenhum comando novo precisar ser registrado, declare isso
  explicitamente e explique brevemente por que os comandos ja estavam
  catalogados ou eram apenas leituras ocasionais.
- Quando uma entrega introduzir um conceito tecnico relevante que ainda nao
  esteja explicado para estudo, atualize ou crie um guia tematico. Para
  persistencia, considere relacionamentos, constraints, migrations e
  mapeamentos JPA.
- Registre decisoes arquiteturais relevantes em um ADR dentro de
  `docs/decisions/`.
- Diferencie claramente estado implementado, decisao atual e trabalho planejado.
- Nao use a documentacao para armazenar tokens, senhas, chaves ou dados pessoais.

## Seguranca

- Nunca versione credenciais ou arquivos `.env` com valores reais.
- Nao registre tokens, authorization codes ou segredos em logs.
- Trate identificadores e autorizacao no backend; protecao de rota no frontend
  nao e controle de seguranca.
- Use apenas o access token para autorizar chamadas da API.
- Mantenha permissoes de workflows e integracoes no menor nivel necessario.
