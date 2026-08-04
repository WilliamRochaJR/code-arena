# jsdom

## Finalidade e estado

Implementa APIs de DOM e HTML em Node.js para testes que nao precisam abrir um
navegador real. Esta instalado e configurado como ambiente do Vitest.

## Instalacao de referencia

```bash
npm install --save-dev jsdom --workspace @code-arena/web
```

## Configuracao

```ts
test: {
  environment: 'jsdom'
}
```

## Onde e usado

- [configuracao Vite e Vitest](../../vite.config.ts)
- [setup do DOM](../../src/test/setup.ts)

jsdom nao substitui testes E2E: layout, navegacao e APIs completas do browser
devem ser validados com Playwright. Consulte o [projeto oficial](https://github.com/jsdom/jsdom).
