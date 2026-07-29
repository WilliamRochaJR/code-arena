# Docker e servicos locais

Execute o Compose na raiz do repositorio.

## Validar o Compose

```bash
docker compose config --quiet
```

Valida a configuracao final sem iniciar containers.

## Iniciar o PostgreSQL

```bash
docker compose up -d --wait postgres
```

Inicia em segundo plano e aguarda o healthcheck. A porta interna `5432` e
publicada como `5433` no host.

## Conferir containers e portas

```bash
docker compose ps --all
docker ps \
  --filter publish=<porta> \
  --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
```

Inclui servicos parados e ajuda a localizar conflitos de porta.

## Ler logs

```bash
docker compose logs --tail=<quantidade> <servico>
```

Limita a saida as linhas finais.

## Encerrar preservando dados

```bash
docker compose down
```

Remove containers e rede, mas preserva volumes nomeados.

## Encerrar e apagar dados

```bash
docker compose down --volumes
```

Remove tambem volumes. Use apenas quando a perda de dados for intencional.

## Consultar health e metricas

```bash
curl --fail --silent --show-error \
  http://localhost:8080/actuator/<health-ou-prometheus>
```

Use `health` para consultar a saude e `prometheus` para ler metricas. `--fail`
faz respostas HTTP sem sucesso retornarem erro ao shell.
