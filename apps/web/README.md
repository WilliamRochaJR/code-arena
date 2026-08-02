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

## Bibliotecas instaladas e planejadas

| Biblioteca            | Finalidade                                      | Instalada | Em uso | Estado ou momento de adocao                |
| --------------------- | ----------------------------------------------- | --------- | ------ | ------------------------------------------ |
| Java Quiz SDK         | Cliente REST e contratos TypeScript             | Sim       | Sim    | Quiz completo e resultados                 |
| TanStack Query        | Cache, sincronizacao e estado remoto            | Sim       | Sim    | Quiz, conclusao e resultado                |
| React Testing Library | Testes pela perspectiva de quem usa a interface | Sim       | Sim    | Configuracao e execucao da tentativa       |
| user-event            | Simulacao de interacoes reais nos testes        | Sim       | Sim    | Selecao, retry, salvamento e navegacao     |
| jest-dom              | Matchers semanticos para elementos do DOM       | Sim       | Sim    | Assercoes dos testes de componentes        |
| jsdom                 | Ambiente de navegador para testes no Vitest     | Sim       | Sim    | Execucao dos testes React                  |
| React Router          | Navegacao declarativa entre etapas              | Sim       | Sim    | Configuracao e perguntas por posicao       |
| React Hook Form       | Estado e submissao de formularios               | Nao       | Nao    | Nos formularios do MVP                     |
| Zod                   | Validacao de dados e schemas no frontend        | Nao       | Nao    | Nos formularios que exigirem validacao     |
| Playwright            | Testes ponta a ponta no navegador               | Nao       | Nao    | Quando existir um fluxo completo e estavel |

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

O servidor de desenvolvimento usa `http://localhost:5173` por padrao. Durante o
desenvolvimento, o Vite encaminha `/api` para `http://localhost:8080`; assim o
navegador usa a mesma origem e a API nao precisa liberar CORS para a porta do
Vite. Para apontar diretamente para outra origem, defina `VITE_API_URL` antes de
iniciar o frontend:

```bash
VITE_API_URL=http://localhost:8080 npm run dev:web
```

O fluxo implementado configura, cria e executa uma tentativa pela cadeia:

```text
Categorias -> Dificuldade -> POST da tentativa -> GET das perguntas
           -> selecao da alternativa -> PUT da resposta -> proxima pergunta
           -> POST da conclusao -> resultado e revisao
```

A aplicacao possui rotas para categorias, dificuldade e cada posicao da
tentativa. As escolhas de configuracao ficam nos parametros da URL, preservando
o estado ao avancar, voltar ou usar a navegacao do browser. Depois da criacao, a
tentativa e carregada pela API, permitindo abrir ou atualizar diretamente a URL
de uma pergunta. O frontend restaura respostas salvas, bloqueia envios
duplicados, apresenta erros com retry e salva a alternativa antes de avancar. A
conclusao exige confirmacao e leva a um resultado recarregavel com pontuacao,
desempenho por categoria, respostas corretas e explicacoes.

## Decisoes do frontend

### SDK como fronteira HTTP

Componentes React nao chamam `fetch` ou endpoints diretamente. Toda comunicacao
com a API passa pelo SDK para centralizar contratos, autenticacao, timeout,
cancelamento e erros.

### Estado local e remoto

Estado local permanece proximo ao componente. TanStack Query gerencia cache,
cancelamento e estados das consultas obtidas da API. Estado global sera
introduzido somente quando houver uma necessidade compartilhada, como
autenticacao.

### URL como estado da configuracao

Categorias e dificuldade usam query parameters enquanto a pessoa configura o
quiz. Isso torna as etapas navegaveis, preserva as escolhas no historico do
browser e evita um store global para um estado curto. Depois da criacao, o ID da
tentativa e a posicao passam a fazer parte do caminho
`/quiz-attempts/{id}/questions/{position}`. O resultado usa
`/quiz-attempts/{id}/result`.

### Risco residual do React Router

O projeto fixa `react-router-dom` em `7.18.2`. O `npm audit` sinaliza o advisory
`GHSA-qwww-vcr4-c8h2`, relacionado a execucao de Actions no modo React Server
Components (RSC). O Code Arena usa uma SPA client-side com Vite e nao habilita
RSC, SSR nem React Router Actions, portanto o caminho vulneravel nao faz parte da
arquitetura atual.

Versoes anteriores possuem outros advisories de XSS e open redirect, por isso o
projeto nao fez downgrade para ocultar o alerta. RSC nao deve ser habilitado sem
nova avaliacao, e a dependencia deve ser atualizada quando uma versao corrigida
e compativel estiver disponivel. Consulte o
[ADR 0006](../../docs/decisions/0006-use-react-router-with-rsc-disabled.md).

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
