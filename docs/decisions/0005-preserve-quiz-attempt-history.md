# ADR 0005: Preservar o historico das tentativas sem snapshots

## Status

Aceita

## Data

2026-07-30

## Contexto

Uma tentativa precisa preservar as dez questoes selecionadas, sua ordem, a
resposta atual e o resultado calculado. O historico nao pode mudar quando o
catalogo de questoes evoluir, e uma alternativa selecionada precisa pertencer a
questao daquela posicao.

Ha diferentes formas de representar esse estado:

- criar uma tabela separada para respostas;
- armazenar a resposta no registro que fixa a questao na tentativa;
- copiar enunciados, explicacoes e alternativas para cada tentativa;
- manter referencias ao catalogo e impedir alteracoes destrutivas.

O MVP nao possui painel administrativo nem edicao livre do catalogo. Duplicar
todo o conteudo em cada tentativa aumentaria o schema, o volume de dados e a
quantidade de mapeamentos antes de existir uma necessidade comprovada.

## Decisao

Usar `attempt_questions` como entidade associativa com estado proprio. Cada linha
fixa uma `question` em uma `quiz_attempt`, preserva sua `position` e armazena
`selected_alternative_id`, `answered_at` e o campo `correct` calculado na
conclusao.

Nao criar uma tabela `answers` separada no MVP. Uma questao possui no maximo uma
resposta atual, que pode ser substituida enquanto a tentativa estiver
`IN_PROGRESS`.

Manter referencias ao catalogo em vez de copiar snapshots textuais. Depois que
uma questao for publicada e usada:

- enunciado, dificuldade, explicacao e alternativas sao imutaveis;
- uma correcao cria uma nova questao e desativa a anterior;
- FKs usam `ON DELETE RESTRICT` para conteudo historico;
- categorias escolhidas pelo usuario sao preservadas separadamente em
  `quiz_attempt_categories`.

Garantir por FK composta que `selected_alternative_id` pertence a
`question_id`. Regras que cruzam o estado da tentativa, como impedir respostas
depois da conclusao, permanecem na camada de aplicacao e na transacao de negocio.

## Alternativas consideradas

### Criar uma tabela `answers`

Separaria resposta e questao da tentativa e facilitaria manter um historico de
cada alteracao. Foi rejeitada porque o MVP precisa apenas da resposta atual:
adicionaria uma entidade, join e ciclo de vida sem um requisito de auditoria de
mudancas.

### Copiar snapshots completos para cada tentativa

Garantiria independencia total do catalogo, mesmo com edicao e exclusao. Foi
adiada porque duplica enunciados, explicacoes e alternativas em todas as
tentativas e exige definir como consultar e corrigir snapshots. A imutabilidade
do conteudo publicado atende ao MVP com menos complexidade.

### Permitir edicao das questoes referenciadas

Simplificaria um futuro CRUD administrativo, mas alteraria retroativamente o
historico exibido e poderia tornar respostas antigas incoerentes. Foi rejeitada.

### Usar triggers para todas as invariantes

Triggers poderiam contar dez questoes e impedir updates com base no status da
tentativa. Foram adiadas porque espalhariam regras de negocio entre banco e
aplicacao. Constraints declarativas protegem a integridade local; transacoes e
servicos protegem o ciclo de vida.

## Consequencias

### Positivas

- A tentativa e sua resposta atual usam um unico registro por posicao.
- Posicao, questao e alternativa podem ser protegidas por constraints e FKs.
- O historico permanece estavel sem duplicar textos em cada tentativa.
- A troca de resposta e um update simples e idempotente.
- O modelo permanece compativel com conclusao atomica e calculo de resultado.

### Negativas

- Conteudo publicado nao pode ser corrigido no mesmo registro.
- Seeds e um futuro painel administrativo precisam criar novas versoes de
  questoes em vez de editar conteudo usado.
- O banco nao garante sozinho regras que dependem do status da tentativa.
- Se surgir auditoria de cada troca de resposta, uma tabela de eventos ou
  historico precisara ser adicionada.
- Se o catalogo passar a aceitar edicao livre, a estrategia de snapshots devera
  ser reavaliada em um novo ADR.

## Referencias

- [Modelo de dominio](../architecture/domain-model.md)
- [MVP](../product/mvp.md)
- [Contratos REST](../api/contracts.md)
- [Issue 13](https://github.com/WilliamRochaJR/code-arena/issues/13)
