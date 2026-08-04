# Java Quiz SDK

## Finalidade e estado

Cliente REST TypeScript interno que isola componentes React de endpoints,
tokens, timeout, cancelamento e erros HTTP. Esta instalado e em uso.

## Instalacao de referencia

```bash
npm install @code-arena/java-quiz-sdk --workspace @code-arena/web
```

No monorepositorio, o npm resolve o pacote pelo workspace local.

## Uso basico

```ts
const client = createJavaQuizClient({ baseUrl, tokenProvider })
const categories = await client.categories.list()
```

## Onde e usado

- [adaptador do cliente](../../src/api/java-quiz-client.ts)
- [integracao autenticada](../../src/auth/AuthenticatedApplication.tsx)
- [paginas do quiz](../../src/quiz/QuizAttemptPage.tsx)
- [README completo do SDK](../../../../packages/java-quiz-sdk/README.md)

Componentes nao devem usar `fetch` diretamente. O SDK recebe apenas o access
token e permanece independente de React e Cognito.
