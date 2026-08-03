# Java Quiz SDK

Cliente REST TypeScript do Code Arena. Ele centraliza contratos, autenticacao,
timeout, cancelamento e erros para que a aplicacao React nao conheca endpoints
nem use `fetch` diretamente.

## Responsabilidades

- expor contratos TypeScript publicos da API;
- adicionar o access token quando um `tokenProvider` estiver configurado;
- aplicar timeout e aceitar cancelamento por `AbortSignal`;
- distinguir erros HTTP, rede, timeout, cancelamento e resposta invalida;
- permanecer independente de React, Cognito e componentes visuais.

## Estado implementado

| Recurso    | Operacao                | Endpoint                                             | Estado     |
| ---------- | ----------------------- | ---------------------------------------------------- | ---------- |
| Categorias | `categories.list()`     | `GET /api/v1/categories`                             | Em uso     |
| Tentativas | `attempts.create()`     | `POST /api/v1/quiz-attempts`                         | Em uso     |
| Tentativas | `attempts.get()`        | `GET /api/v1/quiz-attempts/{attemptId}`              | Disponivel |
| Respostas  | `attempts.saveAnswer()` | `PUT /api/v1/quiz-attempts/{attemptId}/answers/{id}` | Disponivel |
| Resultado  | `attempts.complete()`   | `POST /api/v1/quiz-attempts/{attemptId}/completion`  | Disponivel |
| Historico  | `attempts.list()`       | `GET /api/v1/quiz-attempts`                          | Disponivel |

## Uso

```ts
import { createJavaQuizClient } from '@code-arena/java-quiz-sdk'

const client = createJavaQuizClient({
  baseUrl: 'http://localhost:8080',
  onUnauthorized: () => session.clear(),
  tokenProvider: () => session.accessToken,
})

const { items } = await client.categories.list()
```

Para criar uma tentativa:

```ts
const attempt = await client.attempts.create({
  difficulty: 'INTERMEDIATE',
  categories: ['OOP', 'COLLECTIONS'],
})
```

Para carregar a tentativa e salvar a resposta selecionada:

```ts
const details = await client.attempts.get(attempt.id)
const question = details.questions[0]

await client.attempts.saveAnswer(attempt.id, question.id, {
  selectedAlternativeId: question.alternatives[0].id,
})
```

Os detalhes de uma tentativa em andamento nao incluem gabarito, indicador de
acerto ou explicacao. Essas informacoes permanecem protegidas pela API ate a
conclusao.

O mesmo recurso pode carregar enunciados e alternativas de uma tentativa
concluida para compor a revisao, mas continua sem devolver o gabarito. A
correcao vem exclusivamente de `attempts.complete()`.

Para concluir uma tentativa respondida e obter o resultado corrigido:

```ts
const result = await client.attempts.complete(attempt.id)
```

A operacao de conclusao e idempotente: depois da primeira conclusao, chamadas
repetidas devolvem o resultado persistido pela API sem recalcular a tentativa.

Para consultar tentativas concluidas com paginacao:

```ts
const history = await client.attempts.list({
  page: 0,
  size: 10,
  status: 'COMPLETED',
})
```

O `tokenProvider` e opcional para permitir o perfil local com identidade
controlada. Em ambientes autenticados, ele deve fornecer somente o access token.
Quando a API responder `401`, o SDK chama `onUnauthorized` antes de propagar o
`JavaQuizHttpError`; assim a aplicacao pode descartar a sessao expirada sem
acoplar o cliente HTTP ao provedor de identidade. Uma falha nessa limpeza nao
oculta a resposta original da API.

Para cancelar uma requisicao:

```ts
const controller = new AbortController()
const request = client.categories.list({ signal: controller.signal })
controller.abort()
await request
```

## Erros publicos

| Erro                            | Significado                          |
| ------------------------------- | ------------------------------------ |
| `JavaQuizHttpError`             | A API retornou um status sem sucesso |
| `JavaQuizNetworkError`          | A API nao pode ser alcancada         |
| `JavaQuizTimeoutError`          | O tempo limite foi excedido          |
| `JavaQuizRequestCancelledError` | A chamada foi cancelada              |
| `JavaQuizInvalidResponseError`  | A resposta nao segue o contrato      |

## Comandos

Execute na raiz do repositorio:

```bash
npm run lint --workspace @code-arena/java-quiz-sdk
npm run typecheck --workspace @code-arena/java-quiz-sdk
npm run test --workspace @code-arena/java-quiz-sdk
npm run build --workspace @code-arena/java-quiz-sdk
```

O build gera JavaScript ESM, source maps e declaracoes TypeScript em `dist/`.
Esse diretorio e ignorado pelo Git. Os comandos raiz que verificam consumidores
do SDK executam `npm run build:sdk` primeiro, garantindo que as exportacoes
existam tambem depois de `npm ci` em um checkout limpo.
