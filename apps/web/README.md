# Code Arena Web

Aplicacao web do Code Arena, responsavel pela autenticacao, configuracao do
questionario, execucao das tentativas, apresentacao dos resultados e historico
do usuario.

## Qualidade de codigo

O frontend participa da estrategia compartilhada do monorepositorio, que combina
TypeScript, ESLint, Prettier, Vitest, Husky, lint-staged e Commitlint. Essas
ferramentas atuam em momentos diferentes, desde o desenvolvimento local ate a
validacao do Pull Request.

Consulte a [estrategia de qualidade](../../docs/development/code-quality.md) para
entender o papel de cada ferramenta, quando ela roda e por que foi adotada.

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

Estas dependencias ainda nao foram instaladas. Elas serao adicionadas somente na
entrega que justificar seu uso:

- React Router para navegacao;
- TanStack Query para estado remoto;
- React Hook Form e Zod para formularios e validacao;
- React Testing Library para testes de componentes;
- Playwright para testes ponta a ponta.

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
- [Comandos de terminal](../../docs/guides/terminal-commands.md)
