# TanStack Query

## Finalidade e estado

Gerencia cache, sincronizacao, carregamento, erros e mutacoes de estado remoto.
Esta instalada e em uso no fluxo completo do quiz.

## Instalacao de referencia

```bash
npm install @tanstack/react-query --workspace @code-arena/web
```

## Uso basico

```tsx
const query = useQuery({ queryKey: ['categories'], queryFn: listCategories })
```

## Onde e usada

- QueryClientProvider - [apps/web/src/main.tsx](../../src/main.tsx)
- selecao de categorias - [apps/web/src/quiz/CategorySelectionPage.tsx](../../src/quiz/CategorySelectionPage.tsx)
- execucao e mutacoes - [apps/web/src/quiz/QuizAttemptPage.tsx](../../src/quiz/QuizAttemptPage.tsx)
- resultado - [apps/web/src/quiz/QuizResultPage.tsx](../../src/quiz/QuizResultPage.tsx)

As `queryKey` identificam o cache; mutacoes devem atualizar ou invalidar apenas
os dados afetados. Consulte a [documentacao oficial](https://tanstack.com/query/latest/docs/framework/react/installation).
