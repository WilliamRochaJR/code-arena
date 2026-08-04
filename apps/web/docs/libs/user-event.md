# user-event

## Finalidade e estado

Simula interacoes completas, como clique e digitacao, com eventos mais proximos
do navegador que chamadas isoladas de `fireEvent`. Esta instalada e em uso.

## Instalacao de referencia

```bash
npm install --save-dev @testing-library/user-event --workspace @code-arena/web
```

## Uso basico

```tsx
const user = userEvent.setup()
await user.click(screen.getByRole('button', { name: /continuar/i }))
```

## Onde e usada

- [fluxo principal](../../src/App.test.tsx)
- [login e logout](../../src/auth/AuthenticatedApplication.test.tsx)

Crie uma sessao com `setup()` dentro do teste e aguarde as interacoes assincronas.
Consulte a [documentacao oficial](https://testing-library.com/docs/user-event/intro/).
