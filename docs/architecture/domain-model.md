# Modelo de dominio

Este documento parte do modelo conceitual do quiz e define como ele sera
representado fisicamente no PostgreSQL. As migrations Flyway sao a fonte do
schema; o Hibernate apenas valida o mapeamento com `ddl-auto: validate`.

## Agregados principais

```text
User 1 -------- * QuizAttempt
                       |
                       | 1
                       |
                       * AttemptQuestion * -------- 1 Question
                                |                        |
                                | 0..1                   | 1
                                |                        |
                                v                        *
                         selectedAlternative       Alternative

Question * -------- * Category
```

`AttemptQuestion` representa simultaneamente a questao fixada na tentativa, sua
posicao e a resposta atual. O MVP nao usa uma tabela `answer` separada: selecionar
ou trocar uma alternativa atualiza `selected_alternative_id` e `answered_at`
nesse registro.

## Convencoes fisicas

- Tabelas e colunas usam `snake_case` e nomes de tabelas no plural.
- Identificadores internos usam `uuid`.
- Instantes usam `timestamptz` e sao gravados em UTC.
- Enums Java sao persistidos como `varchar` legivel, protegido por `check`, em
  vez de enums nativos do PostgreSQL.
- Flags usam `boolean` com default apenas quando o significado do default e
  inequivoco.
- FKs de dados historicos usam `ON DELETE RESTRICT`; exclusao em cascata fica
  restrita a tabelas de associacao sem vida propria.
- Entidades JPA nao sao reutilizadas como DTOs REST.

UUIDs e timestamps sao definidos pela aplicacao antes da persistencia. Isso
permite que o agregado conheca seus identificadores e instantes sem depender de
um round trip para defaults do banco.

## Modelo relacional

```text
app_users 1 -------- * quiz_attempts
                           |
                           +-------- * quiz_attempt_categories * --- 1 categories
                           |
                           | 1
                           *
                    attempt_questions * ------------------- 1 questions
                           |                                      |
                           | 0..1                                 | 1
                           |                                      *
                           `----------------------------- alternatives

questions * -------- * categories
           question_categories
```

### `app_users`

Representa o perfil interno associado a uma identidade autenticada. `app_users`
evita o nome reservado e ambiguo `user`.

| Coluna                      | Tipo           | Nulavel | Regra                            |
| --------------------------- | -------------- | ------- | -------------------------------- |
| `id`                        | `uuid`         | nao     | PK                               |
| `identity_provider_subject` | `varchar(255)` | nao     | unico e imutavel; claim `sub`    |
| `email`                     | `varchar(320)` | nao     | contato; nao identifica recursos |
| `display_name`              | `varchar(120)` | sim     | nome apresentado na interface    |
| `created_at`                | `timestamptz`  | nao     | instante de criacao              |
| `last_login_at`             | `timestamptz`  | nao     | ultima sincronizacao do perfil   |

Constraints e indices:

- PK em `id`;
- unique em `identity_provider_subject`;
- check para `created_at <= last_login_at`.

### `categories`

Classifica questoes por assunto, por exemplo OOP, Collections e Streams.

| Coluna   | Tipo           | Nulavel | Regra                                 |
| -------- | -------------- | ------- | ------------------------------------- |
| `id`     | `uuid`         | nao     | PK                                    |
| `slug`   | `varchar(50)`  | nao     | identificador publico unico e estavel |
| `name`   | `varchar(100)` | nao     | nome para exibicao                    |
| `active` | `boolean`      | nao     | default `true`                        |

Constraints e indices:

- PK em `id`;
- unique em `slug`;
- checks que rejeitam `slug` e `name` vazios.

Desativar uma categoria impede seu uso em novas tentativas, mas nao remove
relacionamentos historicos.

### `questions`

| Coluna        | Tipo          | Nulavel | Regra                                    |
| ------------- | ------------- | ------- | ---------------------------------------- |
| `id`          | `uuid`        | nao     | PK                                       |
| `statement`   | `text`        | nao     | enunciado                                |
| `difficulty`  | `varchar(20)` | nao     | `BEGINNER`, `INTERMEDIATE` ou `ADVANCED` |
| `explanation` | `text`        | nao     | revelada somente depois da conclusao     |
| `active`      | `boolean`     | nao     | default `true`                           |
| `created_at`  | `timestamptz` | nao     | instante de criacao                      |
| `updated_at`  | `timestamptz` | nao     | ultima alteracao permitida               |

Constraints e indices:

- PK em `id`;
- check dos valores de `difficulty`;
- checks que rejeitam `statement` e `explanation` vazios;
- check para `created_at <= updated_at`;
- indice em `(active, difficulty)` para selecionar questoes elegiveis.

Uma questao publicada e ja utilizada por uma tentativa nao tem seu enunciado,
explicacao, dificuldade ou alternativas alterados. Uma correcao cria uma nova
questao e desativa a anterior. Essa estrategia preserva o historico sem duplicar
snapshots em `attempt_questions`.

### `question_categories`

Materializa o relacionamento muitos-para-muitos.

| Coluna        | Tipo   | Nulavel | Regra                   |
| ------------- | ------ | ------- | ----------------------- |
| `question_id` | `uuid` | nao     | FK para `questions.id`  |
| `category_id` | `uuid` | nao     | FK para `categories.id` |

Constraints e indices:

- PK composta em `(question_id, category_id)`;
- ambas as FKs com `ON DELETE RESTRICT`, preservando classificacoes publicadas;
- indice em `(category_id, question_id)` para busca por categoria.

A aplicacao exige ao menos uma categoria antes de publicar uma questao.

### `alternatives`

| Coluna          | Tipo       | Nulavel | Regra                             |
| --------------- | ---------- | ------- | --------------------------------- |
| `id`            | `uuid`     | nao     | PK                                |
| `question_id`   | `uuid`     | nao     | FK para `questions.id`            |
| `text`          | `text`     | nao     | conteudo apresentado ao usuario   |
| `correct`       | `boolean`  | nao     | nunca exposto durante a tentativa |
| `display_order` | `smallint` | nao     | ordem positiva e estavel          |

Constraints e indices:

- PK em `id`;
- FK `question_id` com `ON DELETE RESTRICT`;
- unique em `(question_id, display_order)`;
- unique em `(question_id, id)`, necessario para a FK composta da resposta;
- indice unique parcial em `question_id WHERE correct`, garantindo no maximo
  uma alternativa correta;
- check para `display_order > 0` e texto nao vazio.

O banco impede duas alternativas corretas, mas nao consegue garantir sozinho que
cada questao possua ao menos duas alternativas e exatamente uma correta. A
aplicacao valida essas regras antes de publicar a questao, e os seeds sao
cobertos por testes.

### `quiz_attempts`

| Coluna            | Tipo           | Nulavel | Regra                              |
| ----------------- | -------------- | ------- | ---------------------------------- |
| `id`              | `uuid`         | nao     | PK                                 |
| `user_id`         | `uuid`         | nao     | FK para `app_users.id`             |
| `difficulty`      | `varchar(20)`  | nao     | filtro escolhido                   |
| `status`          | `varchar(20)`  | nao     | `IN_PROGRESS` ou `COMPLETED`       |
| `total_questions` | `smallint`     | nao     | dez no MVP                         |
| `correct_answers` | `smallint`     | sim     | calculado na conclusao             |
| `score`           | `numeric(5,2)` | sim     | percentual entre `0.00` e `100.00` |
| `started_at`      | `timestamptz`  | nao     | instante de criacao                |
| `completed_at`    | `timestamptz`  | sim     | preenchido uma unica vez           |

Constraints e indices:

- PK em `id`;
- FK `user_id` com `ON DELETE RESTRICT`;
- checks dos valores de `difficulty` e `status`;
- check para `total_questions = 10`;
- check para `0 <= correct_answers <= total_questions`;
- check para `0.00 <= score <= 100.00`;
- check condicional: `IN_PROGRESS` exige resultado e `completed_at` nulos;
- check condicional: `COMPLETED` exige resultado e `completed_at` preenchidos;
- check para `started_at <= completed_at` quando houver conclusao;
- indice em `(user_id, started_at DESC)` para o historico;
- indice em `(user_id, status, started_at DESC)` para historico filtrado.

O check de `total_questions` protege o valor declarado, mas a aplicacao ainda
precisa inserir exatamente dez linhas em `attempt_questions` na mesma transacao.

### `quiz_attempt_categories`

Preserva os filtros selecionados na criacao da tentativa. Esse conjunto nao e
inferido pelas categorias das questoes, pois uma questao pode pertencer tambem a
categorias que o usuario nao escolheu.

| Coluna        | Tipo   | Nulavel | Regra                      |
| ------------- | ------ | ------- | -------------------------- |
| `attempt_id`  | `uuid` | nao     | FK para `quiz_attempts.id` |
| `category_id` | `uuid` | nao     | FK para `categories.id`    |

Constraints e indices:

- PK composta em `(attempt_id, category_id)`;
- ambas as FKs com `ON DELETE RESTRICT`;
- indice em `(category_id, attempt_id)` para consultas no sentido inverso.

A aplicacao exige ao menos uma categoria e grava o conjunto na mesma transacao
que cria a tentativa.

### `attempt_questions`

| Coluna                    | Tipo          | Nulavel | Regra                           |
| ------------------------- | ------------- | ------- | ------------------------------- |
| `id`                      | `uuid`        | nao     | PK                              |
| `attempt_id`              | `uuid`        | nao     | FK para `quiz_attempts.id`      |
| `question_id`             | `uuid`        | nao     | FK para `questions.id`          |
| `position`                | `smallint`    | nao     | posicao na tentativa            |
| `selected_alternative_id` | `uuid`        | sim     | alternativa escolhida           |
| `correct`                 | `boolean`     | sim     | preenchido somente na conclusao |
| `answered_at`             | `timestamptz` | sim     | ultima alteracao da resposta    |

Constraints e indices:

- PK em `id`;
- FK `attempt_id` com `ON DELETE RESTRICT`;
- FK `question_id` com `ON DELETE RESTRICT`;
- unique em `(attempt_id, position)`;
- unique em `(attempt_id, question_id)`;
- check para `position BETWEEN 1 AND 10`;
- check que mantem `selected_alternative_id` e `answered_at` ambos nulos ou
  ambos preenchidos;
- FK composta `(question_id, selected_alternative_id)` para
  `alternatives(question_id, id)`, garantindo que a alternativa pertence a
  questao respondida;
- a unique de `(attempt_id, position)` tambem fornece o indice para carregar a
  tentativa em ordem.

O campo `correct` permanece nulo enquanto a tentativa esta em andamento. Como
uma constraint de linha nao consulta o status da tabela pai, a aplicacao garante
essa regra na transacao de resposta e conclusao.

## Estados da tentativa

```text
           criar
             |
             v
        IN_PROGRESS
             |
          concluir
             |
             v
         COMPLETED
```

No MVP nao ha expiracao nem cancelamento. Uma tentativa `COMPLETED` nao retorna
a `IN_PROGRESS`.

## Responsabilidade pelas invariantes

| Invariante                                     | PostgreSQL                                    | Aplicacao                                       |
| ---------------------------------------------- | --------------------------------------------- | ----------------------------------------------- |
| Identidade externa pertence a um unico usuario | Unique de `identity_provider_subject`         | Sincroniza o perfil pelo `sub`                  |
| Questao aparece uma vez na tentativa           | Unique `(attempt_id, question_id)`            | Seleciona sem repeticao                         |
| Posicao aparece uma vez e fica entre 1 e 10    | Unique e check em `attempt_questions`         | Cria as dez posicoes na mesma transacao         |
| Alternativa selecionada pertence a questao     | FK composta                                   | Valida antes de persistir                       |
| Questao tem categorias e alternativas validas  | FKs, unique parcial e checks locais           | Exige categoria, duas alternativas e uma certa  |
| Resposta muda apenas em `IN_PROGRESS`          | Nao cruza o status da tabela pai              | Bloqueia alteracao depois da conclusao          |
| Resultado existe apenas em `COMPLETED`         | Checks em `quiz_attempts`                     | Calcula e persiste atomicamente                 |
| Tentativa concluida e imutavel                 | FKs restringem exclusoes                      | Bloqueia updates de negocio                     |
| Conclusao e idempotente                        | Estado persistido                             | Retorna o resultado existente                   |
| Usuario acessa somente as proprias tentativas  | Relacao por `user_id`                         | Filtra e autoriza todas as consultas            |
| Conteudo historico da questao nao muda         | FKs impedem exclusao de conteudo referenciado | Versiona por nova questao e desativa a anterior |

## Limites transacionais

- Criar uma tentativa insere `quiz_attempts` e suas dez `attempt_questions` em
  uma unica transacao, junto das categorias em `quiz_attempt_categories`.
- Salvar uma resposta atualiza apenas uma `attempt_questions`, depois de validar
  propriedade, status e alternativa.
- Concluir bloqueia ou controla concorrencia sobre a tentativa, confirma dez
  respostas, calcula os resultados e atualiza tentativa e questoes na mesma
  transacao.
- Repetir a conclusao de uma tentativa `COMPLETED` apenas le o resultado
  persistido.

O mecanismo concreto de locking sera definido na Entrega 5 junto da regra de
conclusao; esta entrega precisa deixar o modelo compativel com a operacao
atomica.

## Estrategia inicial de dados

Categorias, questoes, associacoes e alternativas iniciais serao carregadas por
uma migration de seed separada da migration estrutural. Os identificadores do
seed sao estaveis para tornar testes e ambientes reproduziveis.

Um painel administrativo fica fora do MVP. Ate que exista um fluxo de
versionamento de conteudo, questoes publicadas sao tratadas como imutaveis:
correcoes criam novas linhas e desativam as anteriores.
