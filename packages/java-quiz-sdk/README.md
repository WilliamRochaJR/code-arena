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

| Recurso    | Operacao            | Endpoint                 | Estado |
| ---------- | ------------------- | ------------------------ | ------ |
| Categorias | `categories.list()` | `GET /api/v1/categories` | Em uso |

## Uso

```ts
import { createJavaQuizClient } from '@code-arena/java-quiz-sdk'

const client = createJavaQuizClient({
  baseUrl: 'http://localhost:8080',
  tokenProvider: () => session.accessToken,
})

const { items } = await client.categories.list()
```

O `tokenProvider` e opcional para permitir o perfil local com identidade
controlada. Em ambientes autenticados, ele deve fornecer somente o access token.

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
