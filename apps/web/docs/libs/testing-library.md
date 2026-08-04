# React Testing Library

## Finalidade e estado

Renderiza componentes e consulta o DOM pela perspectiva da pessoa usuaria. Esta
instalada e em uso com Vitest.

## Instalacao de referencia

```bash
npm install --save-dev @testing-library/react --workspace @code-arena/web
```

## Uso basico

```tsx
render(<Page />)
expect(screen.getByRole('heading')).toBeInTheDocument()
```

## Onde e usada

- [testes da aplicacao - apps/web/src/App.test.tsx](../../src/App.test.tsx)
- [testes de autenticacao - apps/web/src/auth/AuthenticatedApplication.test.tsx](../../src/auth/AuthenticatedApplication.test.tsx)
- [cleanup compartilhado - apps/web/src/test/setup.ts](../../src/test/setup.ts)

Prefira roles, nomes acessiveis e texto visivel a detalhes internos. A biblioteca
nao e um test runner. Consulte a [documentacao oficial](https://testing-library.com/docs/react-testing-library/intro/).
