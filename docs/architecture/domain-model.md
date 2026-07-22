# Modelo de dominio

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

`AttemptQuestion` e um registro da questao escolhida para uma tentativa. Ele
preserva a ordem, a resposta selecionada e o resultado avaliado, impedindo que
uma alteracao posterior no cadastro mude o historico da tentativa.

## Entidades

### User

Representa o perfil interno associado a uma identidade autenticada.

| Campo                     | Tipo            | Regra                                      |
| ------------------------- | --------------- | ------------------------------------------ |
| `id`                      | UUID            | Identificador interno                      |
| `identityProviderSubject` | string          | Claim `sub`, unica e imutavel              |
| `email`                   | string          | Dado de contato, nao e chave de identidade |
| `displayName`             | string opcional | Nome apresentado na interface              |
| `createdAt`               | instante        | Definido no servidor                       |
| `lastLoginAt`             | instante        | Atualizado ao sincronizar o perfil         |

### Category

Classifica questoes por assunto, por exemplo OOP, Collections e Streams.

| Campo    | Tipo    | Regra                                 |
| -------- | ------- | ------------------------------------- |
| `id`     | UUID    | Identificador interno                 |
| `slug`   | string  | Identificador publico unico e estavel |
| `name`   | string  | Nome para exibicao                    |
| `active` | boolean | Controla uso em novas tentativas      |

### Question

| Campo         | Tipo     | Regra                                       |
| ------------- | -------- | ------------------------------------------- |
| `id`          | UUID     | Identificador interno                       |
| `statement`   | texto    | Enunciado obrigatorio                       |
| `difficulty`  | enum     | `BEGINNER`, `INTERMEDIATE` ou `ADVANCED`    |
| `explanation` | texto    | Disponivel somente depois da conclusao      |
| `active`      | boolean  | Questao inativa nao entra em nova tentativa |
| `createdAt`   | instante | Definido no servidor                        |
| `updatedAt`   | instante | Definido no servidor                        |

Uma questao pertence a uma ou mais categorias e possui pelo menos duas
alternativas, exatamente uma delas correta.

### Alternative

| Campo          | Tipo    | Regra                             |
| -------------- | ------- | --------------------------------- |
| `id`           | UUID    | Identificador interno             |
| `questionId`   | UUID    | Questao proprietaria              |
| `text`         | texto   | Conteudo apresentado ao usuario   |
| `correct`      | boolean | Nunca exposto durante a tentativa |
| `displayOrder` | inteiro | Ordem estavel das alternativas    |

### QuizAttempt

| Campo            | Tipo              | Regra                            |
| ---------------- | ----------------- | -------------------------------- |
| `id`             | UUID              | Identificador interno            |
| `userId`         | UUID              | Proprietario da tentativa        |
| `difficulty`     | enum              | Filtro escolhido                 |
| `status`         | enum              | Estado do ciclo de vida          |
| `totalQuestions` | inteiro           | Dez no MVP                       |
| `correctAnswers` | inteiro opcional  | Calculado na conclusao           |
| `score`          | decimal opcional  | Percentual calculado no servidor |
| `startedAt`      | instante          | Definido na criacao              |
| `completedAt`    | instante opcional | Definido uma unica vez           |

### AttemptQuestion

| Campo                   | Tipo              | Regra                             |
| ----------------------- | ----------------- | --------------------------------- |
| `id`                    | UUID              | Identificador interno             |
| `attemptId`             | UUID              | Tentativa proprietaria            |
| `questionId`            | UUID              | Questao selecionada               |
| `position`              | inteiro           | Posicao unica dentro da tentativa |
| `selectedAlternativeId` | UUID opcional     | Alternativa escolhida             |
| `correct`               | boolean opcional  | Preenchido na conclusao           |
| `answeredAt`            | instante opcional | Ultima alteracao da resposta      |

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

## Invariantes

- O usuario autenticado precisa ser proprietario da tentativa consultada.
- Uma tentativa possui dez `AttemptQuestion` com posicoes unicas.
- A mesma questao nao aparece duas vezes na tentativa.
- A alternativa selecionada pertence a questao daquela posicao.
- Respostas so podem mudar em `IN_PROGRESS`.
- A pontuacao e os acertos so existem em `COMPLETED`.
- A conclusao calcula e persiste o resultado em uma unica transacao.
- Repetir a conclusao devolve o resultado persistido.

## Estrategia inicial de dados

Categorias, questoes e alternativas iniciais serao carregadas por migrations ou
seed controlado. Um painel administrativo sera considerado somente depois do
MVP.
