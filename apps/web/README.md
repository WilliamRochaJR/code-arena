# Code Arena Web

Aplicacao web do Code Arena, responsavel pela autenticacao, configuracao do
questionario, execucao das tentativas, apresentacao dos resultados e historico
do usuario.

## Qualidade de codigo

O frontend participa da estrategia compartilhada do monorepositorio. Cada
ferramenta atua em uma camada diferente:

| Ferramenta  | Responsabilidade                                                         | Quando executa                                                            | Por que usamos                                                                  |
| ----------- | ------------------------------------------------------------------------ | ------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| TypeScript  | Verificar tipos e contratos durante a compilacao                         | No editor, em `typecheck`, no build e na CI                               | Detecta incompatibilidades antes da execucao e torna refatoracoes mais seguras  |
| ESLint      | Analisar o codigo e aplicar regras de JavaScript, TypeScript e React     | Em `lint`, no pre-commit para arquivos staged e na CI                     | Identifica problemas que o sistema de tipos nao cobre e padroniza boas praticas |
| Prettier    | Aplicar uma formatacao deterministica                                    | Em `format`, no pre-commit para arquivos staged e em `format:check` na CI | Evita discussoes de estilo e diffs causados apenas por formatacao               |
| Vitest      | Executar testes unitarios e de componentes                               | Em `test`, durante o desenvolvimento e na CI                              | Valida comportamentos e permite detectar regressoes                             |
| Husky       | Conectar scripts do projeto aos hooks do Git                             | Durante `git commit`, por meio de `pre-commit` e `commit-msg`             | Automatiza verificacoes rapidas antes que o commit seja criado                  |
| lint-staged | Executar ESLint e Prettier somente nos arquivos preparados para o commit | No hook `pre-commit`                                                      | Oferece feedback rapido sem executar toda a suite local                         |
| Commitlint  | Validar a mensagem do commit com Conventional Commits                    | No hook `commit-msg`                                                      | Mantem o historico consistente e preparado para automacoes de release           |

Consulte a [estrategia de qualidade](../../docs/development/code-quality.md) para
entender como as validacoes locais, os hooks e a CI se complementam.

## Responsabilidades

- Renderizar uma interface responsiva e acessivel.
- Coordenar navegacao, formularios e estado remoto.
- Integrar a sessao do provedor de identidade.
- Consumir a API exclusivamente por `@code-arena/java-quiz-sdk`.
- Reutilizar componentes de `@code-arena/ui`.

Regras de negocio, calculo de pontuacao e respostas corretas permanecem no
backend.

## Stack instalada

| Tecnologia     | Finalidade                              |
| -------------- | --------------------------------------- |
| React 19       | Componentes e renderizacao da interface |
| TypeScript 5.9 | Tipagem estatica                        |
| Vite 8         | Servidor de desenvolvimento e build     |
| ESLint 9       | Analise estatica                        |
| Prettier 3     | Formatacao compartilhada                |
| Vitest 4       | Testes unitarios e de componentes       |

As versoes exatas resolvidas ficam em [`package-lock.json`](../../package-lock.json).

## Bibliotecas planejadas

| Biblioteca            | Finalidade                                  | Instalada | Em uso | Quando sera adicionada                           |
| --------------------- | ------------------------------------------- | --------- | ------ | ------------------------------------------------ |
| React Router          | Navegacao e protecao de rotas               | Nao       | Nao    | Quando a aplicacao possuir mais de uma pagina    |
| TanStack Query        | Cache, sincronizacao e estado remoto        | Nao       | Nao    | Na primeira integracao do frontend com o SDK     |
| React Hook Form       | Estado e submissao de formularios           | Nao       | Nao    | Na implementacao dos formularios do MVP          |
| Zod                   | Validacao de dados e schemas no frontend    | Nao       | Nao    | Junto dos formularios que exigirem validacao     |
| React Testing Library | Testes de componentes pela perspectiva real | Nao       | Nao    | No primeiro componente com comportamento testado |
| Playwright            | Testes ponta a ponta no navegador           | Nao       | Nao    | Quando existir um fluxo completo e estavel       |

As colunas `Instalada` e `Em uso` diferenciam uma dependencia presente no
projeto de uma dependencia realmente adotada pelo codigo. A tabela deve ser
atualizada na mesma entrega que instalar ou passar a usar cada biblioteca.

## Comandos

Execute a partir da raiz do monorepositorio:

```bash
npm install
npm run dev:web
npm run lint
npm run typecheck
npm test
npm run build
```

Para executar apenas este workspace:

```bash
npm run dev --workspace @code-arena/web
npm run lint --workspace @code-arena/web
npm run typecheck --workspace @code-arena/web
npm run test --workspace @code-arena/web
npm run build --workspace @code-arena/web
```

O servidor de desenvolvimento usa `http://localhost:5173` por padrao.

## Decisoes do frontend

### SDK como fronteira HTTP

Componentes React nao chamam `fetch` ou endpoints diretamente. Toda comunicacao
com a API passa pelo SDK para centralizar contratos, autenticacao, timeout,
cancelamento e erros.

### Estado local e remoto

Estado local permanece proximo ao componente. TanStack Query sera usado para
estado obtido da API. Estado global sera introduzido somente quando houver uma
necessidade compartilhada, como autenticacao.

### ESLint em vez de Oxlint

O template do Vite foi gerado com Oxlint, mas o projeto adotou ESLint porque seu
ecossistema de plugins para React, TypeScript, acessibilidade, testes e Storybook
e mais maduro. O ganho de desempenho do Oxlint nao e relevante no tamanho atual
do projeto. A escolha pode ser reavaliada se o tempo de lint se tornar um
problema mensuravel.

### Dependencias sob demanda

Bibliotecas nao sao instaladas antecipadamente apenas por estarem no roadmap.
Cada dependencia deve resolver uma necessidade da entrega que a introduz e ser
registrada neste documento.

## Documentacao relacionada

- [MVP](../../docs/product/mvp.md)
- [Visao da arquitetura](../../docs/architecture/overview.md)
- [Autenticacao](../../docs/architecture/authentication.md)
- [Contratos REST](../../docs/api/contracts.md)
- [Decisoes arquitetonicas](../../docs/decisions/README.md)
- [Catalogo de comandos](../../docs/guides/terminal-commands/README.md)
