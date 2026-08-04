# Zod

## Finalidade e estado

Define schemas TypeScript-first para validar dados em runtime. Esta planejada,
mas ainda nao esta instalada nem em uso.

## Instalacao planejada

```bash
npm install zod --workspace @code-arena/web
```

## Exemplo de referencia

```ts
const schema = z.object({ categories: z.array(z.string()).min(1) })
```

## Adocao planejada

Pode validar formularios e dados externos quando houver uma fronteira que exija
checagem em runtime. Nao substitui a validacao da API nem deve duplicar regras de
negocio do backend. Hoje nao existem arquivos de uso para linkar.

Consulte a [documentacao oficial](https://zod.dev/).
