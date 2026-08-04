# oidc-client-ts

## Finalidade e estado

Implementa Authorization Code com PKCE, tokens, renovacao e armazenamento de
sessao no navegador. Esta instalada e usada por `react-oidc-context` e pela
adaptacao do Code Arena.

## Instalacao de referencia

```bash
npm install oidc-client-ts --workspace @code-arena/web
```

## Uso basico

```ts
const store = new WebStorageStateStore({ store: window.sessionStorage })
```

## Onde e usada

- [armazenamento OIDC - apps/web/src/auth/AuthenticationProvider.tsx](../../src/auth/AuthenticationProvider.tsx)
- [leitura segura do access token - apps/web/src/auth/access-token.ts](../../src/auth/access-token.ts)
- [tipos de autenticacao - apps/web/src/auth/types.ts](../../src/auth/types.ts)

Nunca configure client secret no frontend e nunca envie ID token à API no lugar
do access token. Consulte a [documentacao oficial](https://authts.github.io/oidc-client-ts/modules.html).
