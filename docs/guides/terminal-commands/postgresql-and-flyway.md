# PostgreSQL e Flyway

Os comandos usam o servico `postgres` definido no Compose. Inicie o servico
conforme o catalogo de [Docker e servicos locais](docker-and-services.md).

## Abrir o terminal do PostgreSQL

```bash
docker compose exec postgres \
  psql --username <usuario> --dbname <banco>
```

Abre o cliente `psql` dentro do container, sem exigir instalacao local. Use as
credenciais do ambiente de desenvolvimento e nunca registre senhas reais.

Para sair:

```text
\q
```

## Listar e descrever tabelas

Dentro do `psql`:

```text
\dt
\d <tabela>
\di
```

- `\dt` lista tabelas;
- `\d` mostra colunas, tipos, chaves, indices e constraints;
- `\di` lista indices.

Esses comandos sao metacomandos do `psql`, nao instrucoes SQL.

## Executar uma consulta sem abrir sessao interativa

```bash
docker compose exec postgres \
  psql --username <usuario> --dbname <banco> \
  --command "<consulta-sql>"
```

E util para verificacoes pontuais e scripts. Prefira consultas somente leitura
durante diagnosticos.

## Conferir migrations aplicadas

Dentro do `psql`:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

`flyway_schema_history` e mantida pelo Flyway. Nao altere suas linhas
manualmente.

## Consultar constraints de uma tabela

```sql
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'public'
  AND table_name = '<tabela>'
ORDER BY constraint_type, constraint_name;
```

Ajuda a estudar chaves primarias, estrangeiras, `UNIQUE` e `CHECK` registradas no
schema.

## Localizar migrations

Execute na raiz:

```bash
rg --files apps/api/src/main/resources/db/migration
```

Os nomes seguem `V<versao>__<descricao>.sql`. Uma migration aplicada nao deve ser
reescrita; uma alteracao posterior recebe uma nova versao.
