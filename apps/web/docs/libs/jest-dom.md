# jest-dom

## Finalidade e estado

Adiciona matchers semanticos para o DOM, como `toBeInTheDocument` e
`toBeDisabled`. Esta instalada e em uso com Vitest, apesar do nome historico.

## Instalacao de referencia

```bash
npm install --save-dev @testing-library/jest-dom --workspace @code-arena/web
```

## Configuracao

```ts
import '@testing-library/jest-dom/vitest'
```

## Onde e usado

- [setup dos testes](../../src/test/setup.ts)
- [configuracao do Vitest](../../vite.config.ts)
- [assercoes](../../src/App.test.tsx)

Use matchers que expressem o comportamento percebido no DOM. Consulte a
[documentacao oficial](https://github.com/testing-library/jest-dom).
