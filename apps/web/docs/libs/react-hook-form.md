# React Hook Form

## Finalidade e estado

Gerencia registro, validacao e submissao de formularios React com poucas
renderizacoes. Esta planejada, mas ainda nao esta instalada nem em uso.

## Instalacao planejada

```bash
npm install react-hook-form --workspace @code-arena/web
```

## Exemplo de referencia

```tsx
const { register, handleSubmit } = useForm<FormValues>()
```

## Adocao planejada

So deve ser introduzida quando um formulario do MVP possuir complexidade que
justifique a dependencia. Hoje nao existem arquivos de uso para linkar.

Evite adicionar estado global para dados exclusivos de um formulario. Consulte
a [documentacao oficial](https://react-hook-form.com/).
