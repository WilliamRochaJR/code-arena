# Contratos REST do MVP

## Convencoes

- Base path: `/api/v1`.
- Conteudo: `application/json`.
- Datas: ISO 8601 em UTC.
- Identificadores: UUID representado como string.
- Enumeracoes: valores em `UPPER_SNAKE_CASE`.
- Endpoints protegidos recebem `Authorization: Bearer <access token>`.
- Erros seguem Problem Details (`application/problem+json`).

## Perfil autenticado

### `GET /api/v1/me`

Retorna o perfil do usuario e cria ou sincroniza o registro local quando
necessario.

```json
{
  "id": "357209e1-44d6-4904-843f-35df2fd09418",
  "email": "developer@example.com",
  "displayName": "Developer",
  "roles": ["USER"]
}
```

## Categorias

### `GET /api/v1/categories`

Retorna categorias ativas que podem ser usadas para configurar uma tentativa.

```json
{
  "items": [
    { "slug": "OOP", "name": "Orientacao a objetos" },
    { "slug": "COLLECTIONS", "name": "Collections" }
  ]
}
```

## Tentativas

### `POST /api/v1/quiz-attempts`

Cria uma tentativa e seleciona dez questoes sem revelar as respostas corretas.

```json
{
  "difficulty": "INTERMEDIATE",
  "categories": ["OOP", "COLLECTIONS"]
}
```

Resposta `201 Created`, com header `Location` apontando para a tentativa:

```json
{
  "id": "df34978a-2024-4234-9ba8-1af6cf7af994",
  "status": "IN_PROGRESS",
  "difficulty": "INTERMEDIATE",
  "categories": ["OOP", "COLLECTIONS"],
  "totalQuestions": 10,
  "answeredQuestions": 0,
  "startedAt": "2026-07-22T18:30:00Z"
}
```

Retorna `422 Unprocessable Entity` quando nao houver dez questoes compativeis.

### `GET /api/v1/quiz-attempts/{attemptId}`

Retorna a tentativa do usuario. Enquanto estiver em andamento, inclui as
questoes, alternativas e respostas ja selecionadas, sem incluir acerto ou
explicacao.

```json
{
  "id": "df34978a-2024-4234-9ba8-1af6cf7af994",
  "status": "IN_PROGRESS",
  "difficulty": "INTERMEDIATE",
  "totalQuestions": 10,
  "answeredQuestions": 1,
  "startedAt": "2026-07-22T18:30:00Z",
  "questions": [
    {
      "id": "38cce009-e4c6-4ea9-be9a-49929b6bce31",
      "position": 1,
      "statement": "Qual interface nao permite elementos duplicados?",
      "categories": ["COLLECTIONS"],
      "alternatives": [
        {
          "id": "c36507e9-2865-4689-af8f-87a1f8e54649",
          "text": "List"
        },
        {
          "id": "cc187215-c0f2-44a6-803f-392fbd21a2df",
          "text": "Set"
        }
      ],
      "selectedAlternativeId": "cc187215-c0f2-44a6-803f-392fbd21a2df"
    }
  ]
}
```

Depois da conclusao, esse recurso pode incluir o resultado descrito abaixo.

### `PUT /api/v1/quiz-attempts/{attemptId}/answers/{questionId}`

Cria ou substitui a resposta de uma questao. A operacao e idempotente.

```json
{
  "selectedAlternativeId": "cc187215-c0f2-44a6-803f-392fbd21a2df"
}
```

Resposta `200 OK`:

```json
{
  "questionId": "38cce009-e4c6-4ea9-be9a-49929b6bce31",
  "selectedAlternativeId": "cc187215-c0f2-44a6-803f-392fbd21a2df",
  "answeredAt": "2026-07-22T18:32:14Z"
}
```

Retorna `409 Conflict` se a tentativa ja estiver concluida e `422
Unprocessable Entity` se a questao ou alternativa nao pertencer ao contexto da
tentativa.

### `POST /api/v1/quiz-attempts/{attemptId}/completion`

Conclui a tentativa, corrige as respostas e persiste o resultado em uma unica
transacao. Todas as questoes precisam estar respondidas. Repetir a requisicao
devolve o resultado ja persistido sem recalcular a tentativa.

Resposta `200 OK`:

```json
{
  "attemptId": "df34978a-2024-4234-9ba8-1af6cf7af994",
  "status": "COMPLETED",
  "totalQuestions": 10,
  "correctAnswers": 8,
  "score": 80.0,
  "startedAt": "2026-07-22T18:30:00Z",
  "completedAt": "2026-07-22T18:40:00Z",
  "performanceByCategory": [
    { "category": "COLLECTIONS", "correct": 3, "total": 4 },
    { "category": "OOP", "correct": 5, "total": 6 }
  ],
  "questions": [
    {
      "id": "38cce009-e4c6-4ea9-be9a-49929b6bce31",
      "position": 1,
      "selectedAlternativeId": "cc187215-c0f2-44a6-803f-392fbd21a2df",
      "correctAlternativeId": "cc187215-c0f2-44a6-803f-392fbd21a2df",
      "correct": true,
      "explanation": "Set representa uma colecao sem elementos duplicados."
    }
  ]
}
```

Retorna `409 Conflict` se ainda houver questoes sem resposta.

### `GET /api/v1/quiz-attempts`

Lista somente as tentativas do usuario autenticado.

Query parameters:

| Parametro | Padrao | Regra |
|---|---:|---|
| `page` | `0` | Inteiro maior ou igual a zero |
| `size` | `10` | Entre 1 e 50 |
| `status` | todos | `IN_PROGRESS` ou `COMPLETED` |
| `sort` | `startedAt,desc` | Ordenacao permitida pelo contrato |

```json
{
  "items": [
    {
      "id": "df34978a-2024-4234-9ba8-1af6cf7af994",
      "status": "COMPLETED",
      "difficulty": "INTERMEDIATE",
      "categories": ["OOP", "COLLECTIONS"],
      "score": 80.0,
      "startedAt": "2026-07-22T18:30:00Z",
      "completedAt": "2026-07-22T18:40:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalItems": 1,
  "totalPages": 1
}
```

## Erros

Exemplo de erro de validacao:

```json
{
  "type": "https://code-arena.dev/problems/validation-error",
  "title": "Dados invalidos",
  "status": 400,
  "detail": "Um ou mais campos possuem valores invalidos.",
  "instance": "/api/v1/quiz-attempts",
  "errors": [
    {
      "field": "categories",
      "message": "selecione ao menos uma categoria"
    }
  ]
}
```

Codigos relevantes:

| Codigo | Uso |
|---:|---|
| `400` | JSON ou campos invalidos |
| `401` | autenticacao ausente ou invalida |
| `403` | operacao nao autorizada |
| `404` | recurso inexistente ou nao visivel |
| `409` | estado atual impede a operacao |
| `422` | dados validos, mas incompativeis com a regra de negocio |
| `500` | falha interna sem exposicao de detalhes sensiveis |

## Evolucao do contrato

O OpenAPI publicado pela API sera a fonte executavel do contrato. Os tipos do
SDK devem refletir esse contrato, mas a geracao automatica so sera adotada se
reduzir manutencao sem expor detalhes internos da API.
