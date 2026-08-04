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

- [QueryClientProvider](../../src/main.tsx)
- [selecao de categorias](../../src/quiz/CategorySelectionPage.tsx)
- [execucao e mutacoes](../../src/quiz/QuizAttemptPage.tsx)
- [resultado](../../src/quiz/QuizResultPage.tsx)

As `queryKey` identificam o cache; mutacoes devem atualizar ou invalidar apenas
os dados afetados. Consulte a [documentacao oficial](https://tanstack.com/query/latest/docs/framework/react/installation).
