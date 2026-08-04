# react-oidc-context

## Finalidade e estado

Integra o ciclo de sessao OIDC ao React por provider e hooks. Esta instalada e
em uso no modo de autenticacao `oidc`.

## Instalacao de referencia

```bash
npm install react-oidc-context --workspace @code-arena/web
```

## Uso basico

```tsx
<AuthProvider authority={issuer} client_id={clientId} redirect_uri={callback} />
```

## Onde e usada

- [provider e logout](../../src/auth/AuthenticationProvider.tsx)
- [configuracao externa](../../src/auth/config.ts)
- [contexto da aplicacao](../../src/auth/AuthContext.ts)

O SDK nao depende desta biblioteca. Tokens permanecem sob responsabilidade da
camada de autenticacao. Consulte o [projeto oficial](https://github.com/authts/react-oidc-context).
