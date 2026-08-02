# ADR 0006: Usar React Router com RSC desabilitado

## Status

Aceita

## Data

2026-08-01

## Contexto

A aplicacao web precisa representar as etapas de configuracao do quiz na URL,
preservar selecoes durante a navegacao e encaminhar a pessoa para a tentativa
criada. React Router atende essa necessidade e integra-se ao React e ao Vite.

Durante a adocao, `npm audit` sinalizou o advisory
`GHSA-qwww-vcr4-c8h2` para `react-router-dom` `7.18.2`. A falha esta associada a
execucao de Actions no React Server Components (RSC). O Code Arena e uma SPA
client-side e nao usa RSC, SSR nem React Router Actions.

Versoes anteriores disponiveis possuem outros advisories de XSS, open redirect
e processamento server-side. Portanto, um downgrade apenas trocaria o alerta
por riscos mais amplos.

## Decisao

Fixar `react-router-dom` em `7.18.2` e limitar seu uso ao roteamento client-side.
RSC, SSR e React Router Actions permanecem proibidos enquanto o advisory estiver
aplicavel.

A dependencia deve ser atualizada assim que houver uma versao corrigida e
compativel. Antes de habilitar qualquer recurso server-side do React Router, uma
nova analise de seguranca e obrigatoria.

## Alternativas consideradas

### Fazer downgrade para React Router 7.11.0

Rejeitada porque o `npm audit` identifica nessa versao advisories adicionais de
XSS, open redirect, negacao de servico e processamento server-side.

### Implementar roteamento manual com History API

Evitaria a dependencia, mas adicionaria codigo proprio para matching, fallback,
navegacao e testes. Esse custo nao se justifica para ocultar um alerta ligado a
um recurso que a aplicacao nao utiliza.

### Adiar toda navegacao

Manteria a tela unica, mas impediria o fluxo de configuracao definido para o MVP
e deixaria o botao `Continuar` sem comportamento util.

## Consequencias

### Positivas

- Rotas declarativas e testaveis para as etapas do quiz.
- Navegacao do browser preservada sem estado global adicional.
- Uso restrito a um caminho que nao e afetado pelo advisory conhecido.

### Negativas

- `npm audit` continua reportando o advisory ate uma atualizacao corrigida.
- A equipe precisa monitorar a dependencia e nao pode habilitar RSC sem rever a
  decisao.
- O risco residual precisa permanecer visivel em revisoes e releases.

## Referencias

- [GitHub Advisory GHSA-qwww-vcr4-c8h2](https://github.com/advisories/GHSA-qwww-vcr4-c8h2)
- [Issue #23](https://github.com/WilliamRochaJR/code-arena/issues/23)
- [README da aplicacao web](../../apps/web/README.md)
