# Relacionamentos de banco e JPA no Code Arena

Este guia explica o modelo relacional do Code Arena como material de estudo.
O [modelo de dominio](../architecture/domain-model.md) continua sendo a
referencia tecnica completa do schema; aqui o foco e entender por que as tabelas
e os mapeamentos existem.

## Conceitos fundamentais

### Chave primaria

Uma chave primaria, ou `PRIMARY KEY`, identifica uma linha de forma unica.

```text
categories
+--------------------------------------+-------------+
| id                                   | name        |
+--------------------------------------+-------------+
| 69c...                               | Collections |
+--------------------------------------+-------------+
```

No Code Arena, identificadores internos usam UUID:

```sql
CONSTRAINT pk_categories PRIMARY KEY (id)
```

O Java representa esse valor com `UUID` e o JPA identifica o campo com `@Id`.

### Chave estrangeira

Uma chave estrangeira, ou `FOREIGN KEY`, aponta para uma linha de outra tabela.
Ela impede que o banco grave uma referencia inexistente.

```text
alternatives.question_id ---> questions.id
```

```sql
FOREIGN KEY (question_id) REFERENCES questions (id)
```

No JPA, essa referencia aparece como uma associacao:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "question_id", nullable = false)
private Question question;
```

`ManyToOne` significa que muitas alternativas podem apontar para uma mesma
questao.

### Restricao unique

Uma restricao `UNIQUE` impede repeticoes que nao sao a chave primaria.

Exemplos do projeto:

- duas categorias nao podem ter o mesmo `slug`;
- duas alternativas da mesma questao nao podem ter a mesma ordem;
- uma questao nao pode aparecer duas vezes na mesma tentativa.

```sql
UNIQUE (attempt_id, question_id)
```

### Check constraint

Uma `CHECK` valida uma regra usando os valores da propria linha.

```sql
CHECK (position BETWEEN 1 AND 10)
```

Ela protege o banco mesmo que um erro de aplicacao tente gravar uma posicao
invalida. Regras que precisam consultar varias linhas ou o estado de outra
tabela continuam na camada de aplicacao.

### Indice

Um indice acelera caminhos de consulta frequentes. Ele nao cria um
relacionamento.

```sql
CREATE INDEX idx_quiz_attempts_user_started
    ON quiz_attempts (user_id, started_at DESC);
```

Esse indice ajuda a listar o historico de um usuario do mais recente para o mais
antigo.

## Cardinalidades usadas

Cardinalidade descreve quantas linhas de um lado podem se relacionar com o
outro.

### Um para muitos

```text
Question 1 -------- N Alternative
```

Uma questao possui varias alternativas. Cada alternativa pertence a uma unica
questao. Fisicamente, a chave estrangeira fica no lado que pode se repetir:

```text
alternatives.question_id
```

Outros exemplos:

```text
AppUser     1 -------- N QuizAttempt
QuizAttempt 1 -------- N AttemptQuestion
Question    1 -------- N AttemptQuestion
```

### Muitos para muitos

Uma questao pode pertencer a varias categorias, e uma categoria pode classificar
varias questoes:

```text
Question N -------- N Category
```

Um banco relacional representa isso com uma tabela intermediaria:

```text
questions
    |
    | 1
    N
question_categories
    N
    | 1
    |
categories
```

`question_categories` possui uma chave primaria composta:

```sql
PRIMARY KEY (question_id, category_id)
```

O par completo identifica a associacao e impede que a mesma categoria seja
adicionada duas vezes a uma questao.

## Por que as tabelas de associacao viraram entidades

O JPA permite esconder uma tabela intermediaria usando `@ManyToMany`. O Code
Arena escolheu entidades explicitas:

- `QuestionCategory`;
- `QuizAttemptCategory`.

Essa escolha deixa a tabela e sua identidade composta visiveis no codigo,
facilita testes diretos e permite adicionar atributos no futuro sem refazer o
mapeamento.

As chaves compostas sao representadas por:

- `QuestionCategoryId`;
- `QuizAttemptCategoryId`.

Exemplo simplificado:

```java
@EmbeddedId
private QuestionCategoryId id;

@MapsId("questionId")
@ManyToOne(fetch = FetchType.LAZY)
private Question question;
```

- `@EmbeddedId` informa que a entidade possui uma chave formada por mais de uma
  coluna;
- `@MapsId` liga uma parte da chave ao identificador da entidade relacionada;
- as classes de ID implementam `equals` e `hashCode`, pois o JPA precisa comparar
  identidades compostas corretamente.

## A tentativa como registro historico

`quiz_attempts` registra o estado geral:

```text
usuario, dificuldade, status, total, acertos, nota e instantes
```

`attempt_questions` fixa as dez questoes e sua ordem:

```text
tentativa, questao, posicao, alternativa selecionada e resultado
```

Ela nao e apenas uma tabela intermediaria. Possui identidade e estado proprios,
portanto e mapeada como `AttemptQuestion`.

```text
QuizAttempt 1 ---- N AttemptQuestion N ---- 1 Question
                              |
                              | 0..1
                              v
                         Alternative
```

Uma resposta nao usa uma tabela separada no MVP. Selecionar ou trocar uma
alternativa atualiza a linha de `attempt_questions`.

Essa decisao esta detalhada no
[ADR 0005](../decisions/0005-preserve-quiz-attempt-history.md).

## A chave estrangeira composta da resposta

Nao basta verificar se `selected_alternative_id` existe. A alternativa tambem
precisa pertencer a `question_id`.

O banco protege os dois valores juntos:

```sql
FOREIGN KEY (question_id, selected_alternative_id)
    REFERENCES alternatives (question_id, id)
```

Com isso, uma tentativa nao pode associar acidentalmente a questao A com uma
alternativa da questao B.

No JPA, `question_id` ja e usado pela propriedade `question`. Para evitar duas
propriedades tentando gravar a mesma coluna:

- `selectedAlternativeId` grava o UUID escolhido;
- `selectedAlternative` oferece navegacao somente leitura;
- as colunas da associacao composta usam `insertable = false` e
  `updatable = false`.

Esse detalhe explica por que o ID e a associacao aparecem separadamente em
`AttemptQuestion`.

## Carregamento lazy

As associacoes usam `FetchType.LAZY`. Isso significa que buscar uma tentativa
nao carrega automaticamente todo o usuario, todas as questoes e alternativas.

O carregamento sob demanda:

- evita consultas e dados desnecessarios;
- exige que a camada de aplicacao defina conscientemente o que precisa;
- funciona com `spring.jpa.open-in-view=false`, mantendo acesso ao banco fora da
  camada web.

Controllers futuros nao devem serializar entidades JPA diretamente. Servicos
carregam os dados necessarios e produzem DTOs especificos para o contrato REST.

## Flyway e Hibernate possuem papeis diferentes

```text
Flyway    -> cria e evolui o schema com migrations versionadas
Hibernate -> valida se as entidades correspondem ao schema
```

O projeto usa:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

O Hibernate nao cria nem altera tabelas. Se uma anotacao divergir da migration,
a aplicacao falha ao iniciar, tornando a incompatibilidade visivel.

## Repositories

As interfaces Spring Data fornecem acesso persistente sem implementar
manualmente operacoes basicas.

```java
public interface CategoryRepository
    extends JpaRepository<Category, UUID> {

    List<Category> findAllByActiveTrueOrderByNameAsc();
}
```

O Spring interpreta o nome do metodo e cria a consulta.

Quando a selecao depende de recursos especificos do PostgreSQL, o projeto usa
SQL nativo. A consulta de questoes elegiveis usa:

- dificuldade;
- categorias ativas;
- questoes ativas;
- `EXISTS` para nao duplicar questoes com varias categorias;
- `random()` para variar a selecao;
- `LIMIT` para respeitar o tamanho da tentativa.

Todas as consultas relacionadas a tentativas recebem tambem o ID do usuario.
Esse filtro prepara o isolamento de dados, embora a autorizacao completa seja
implementada junto da autenticacao.

## Como os testes comprovam o mapeamento

Os testes usam PostgreSQL real em Testcontainers:

1. um container isolado e iniciado;
2. o Flyway aplica as migrations;
3. o Hibernate valida as entidades;
4. os testes persistem e consultam dados com JPA;
5. o container e descartado ao final.

Isso cobre comportamentos que um banco em memoria poderia representar de forma
diferente, como UUID, `timestamptz`, indices parciais, `random()` e constraints
do PostgreSQL.

## Roteiro para estudar o modelo

Ao analisar uma tabela, siga esta ordem:

1. encontre a chave primaria;
2. identifique as chaves estrangeiras;
3. determine a cardinalidade de cada relacionamento;
4. confira `UNIQUE` e `CHECK`;
5. veja quais consultas justificam os indices;
6. localize a entidade e as anotacoes JPA correspondentes;
7. localize o repository e os testes que exercitam a consulta.

Use junto deste guia:

- [Modelo de dominio](../architecture/domain-model.md);
- [Contratos REST](../api/contracts.md);
- [Comandos de PostgreSQL e Flyway](terminal-commands/postgresql-and-flyway.md);
- [Comandos de testes e diagnostico](terminal-commands/testing-and-diagnostics.md).
