# React Router

## Finalidade e estado

Declara rotas, parametros e navegacao da SPA. Esta instalado e em uso nas etapas
de configuracao, tentativa, resultado e historico.

## Instalacao de referencia

```bash
npm install react-router-dom --workspace @code-arena/web
```

## Uso basico

```tsx
<Route path="/quiz-attempts/:id/result" element={<QuizResultPage />} />
```

## Onde e usado

- BrowserRouter - [apps/web/src/main.tsx](../../src/main.tsx)
- mapa de rotas - [apps/web/src/App.tsx](../../src/App.tsx)
- URL como estado - [apps/web/src/quiz/setupSearchParams.ts](../../src/quiz/setupSearchParams.ts)

O projeto usa modo declarativo client-side, sem RSC, SSR ou Actions. Consulte a
[documentacao oficial](https://reactrouter.com/start/declarative/installation)
e o ADR de seguranca -
[docs/decisions/0006-use-react-router-with-rsc-disabled.md](../../../../docs/decisions/0006-use-react-router-with-rsc-disabled.md).
