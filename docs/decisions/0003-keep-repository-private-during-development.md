# ADR 0003: Manter o repositorio privado durante o desenvolvimento

## Status

Aceita

## Data

2026-07-22

## Contexto

O repositorio sera publico como portfolio, mas o autor prefere concluir uma base
apresentavel antes da publicacao. No plano atual do GitHub, Rulesets podem ser
configurados, mas nao sao aplicados a este repositorio privado sem migrar para um
plano compativel.

## Decisao

Manter o repositorio privado durante as primeiras entregas e aplicar o fluxo de
branches e Pull Requests por disciplina. Antes da publicacao, realizar uma
auditoria do historico e tornar o repositorio publico, ativando o enforcement dos
Rulesets ja planejados.

## Alternativas consideradas

### Tornar publico imediatamente

Permitiria aplicar Rulesets no plano gratuito, mas nao atende a preferencia de
publicar uma base mais madura.

### Contratar um plano pago

Aplicaria protecoes no repositorio privado, mas o custo nao se justifica para o
portfolio atual.

### Trabalhar sem Rulesets planejados

Evita a configuracao antecipada, mas deixa de registrar quais protecoes serao
ativadas na publicacao.

## Consequencias

### Positivas

- O autor controla o momento da apresentacao publica.
- O fluxo profissional continua sendo praticado e documentado.
- A futura publicacao inclui uma revisao deliberada de seguranca e qualidade.

### Negativas

- O GitHub nao bloqueia tecnicamente push direto ou force push enquanto privado.
- As regras dependem de disciplina ate a mudanca de visibilidade.
- Checks de CI nao impedirao merges no plano atual, embora devam ser respeitados.

## Criterios para publicacao

- Auditar o historico em busca de segredos e dados indevidos.
- Confirmar que `.env` e credenciais nao foram versionados.
- Revisar README, licenca e instrucoes de execucao.
- Executar lint, testes e builds.
- Tornar o repositorio publico e confirmar o enforcement dos Rulesets.
