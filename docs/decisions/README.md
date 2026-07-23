# Architecture Decision Records

Um Architecture Decision Record (ADR) registra uma decisao tecnica relevante,
seu contexto, as alternativas avaliadas e suas consequencias.

## Quando criar um ADR

Crie um ADR quando a decisao:

- afeta mais de um componente;
- estabelece uma restricao duradoura;
- possui alternativas razoaveis;
- influencia seguranca, dados, deploy ou operacao;
- seria dificil de compreender apenas lendo o codigo.

Decisoes locais e facilmente reversiveis, como nomes de variaveis ou detalhes
visuais, nao precisam de ADR.

## Numeracao e nome

```text
NNNN-titulo-curto-em-kebab-case.md
```

Exemplo:

```text
0004-use-postgresql.md
```

Os numeros nao sao reutilizados, mesmo quando uma proposta e rejeitada.

## Status

- `Proposta`: em discussao.
- `Aceita`: decisao vigente.
- `Rejeitada`: avaliada e nao adotada.
- `Substituida`: trocada por outro ADR, que deve ser referenciado.
- `Obsoleta`: nao se aplica mais ao sistema.

Um ADR aceito nao deve ser reescrito para esconder a historia. Uma mudanca de
decisao gera um novo registro que substitui o anterior.

Use [template.md](template.md) como ponto de partida.
