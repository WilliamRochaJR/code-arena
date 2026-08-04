# Playwright

## Finalidade e estado

Executa testes ponta a ponta em navegador real. Esta instalado na raiz do
monorepositorio e em uso para validar o fluxo completo do quiz em Chromium.

## Instalacao de referencia

```bash
npm install --save-dev @playwright/test
npm run test:e2e:install
```

## Uso basico

```ts
await page.getByRole('button', { name: /continuar/i }).click()
```

## Onde e usado

- [configuracao](../../../../playwright.config.ts)
- [fluxo do quiz](../../../../e2e/quiz-flow.spec.ts)
- [guia E2E](../../../../e2e/README.md)

Playwright complementa Vitest e jsdom; ele nao substitui testes unitarios mais
rapidos. Consulte a [documentacao oficial](https://playwright.dev/docs/intro).
