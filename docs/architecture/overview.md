# Visao geral da arquitetura

## Contexto

O Code Arena usa um monorepositorio para manter a aplicacao web, a API e os
pacotes reutilizaveis com contratos e evolucao coordenados.

```text
code-arena/
|-- apps/
|   |-- web/                 React + TypeScript + Vite
|   `-- api/                 Java 21 + Spring Boot
|-- packages/
|   |-- java-quiz-sdk/       cliente REST independente de React
|   `-- ui/                  componentes e tokens reutilizaveis
|-- infrastructure/
|   `-- terraform/           infraestrutura AWS (etapa posterior)
`-- docs/
```

## Componentes e responsabilidades

### Aplicacao web

- Renderizar as paginas e os estados da interface.
- Coordenar navegacao, formularios e estado remoto.
- Integrar a sessao do Cognito por uma camada de autenticacao.
- Consumir a API exclusivamente pelo Java Quiz SDK.
- Reutilizar os componentes publicados pelo pacote de UI.

### Java Quiz SDK

- Expor uma API TypeScript estavel para os consumidores.
- Serializar requisicoes e respostas.
- Obter um access token por uma funcao injetada pelo consumidor.
- Aplicar timeout e aceitar `AbortSignal`.
- Padronizar erros HTTP e erros de transporte.
- Nao depender de React, Cognito ou componentes visuais.

### Biblioteca de UI

- Concentrar tokens e componentes reutilizaveis.
- Oferecer variantes previsiveis e contratos de propriedades tipados.
- Tratar acessibilidade e estados interativos como parte do componente.
- Nao conhecer endpoints, autenticacao ou regras do questionario.

### API

- Validar access tokens e autorizacao sobre os recursos.
- Manter as regras de negocio e a fonte de verdade do questionario.
- Selecionar questoes, persistir respostas e calcular resultados.
- Publicar contratos REST com DTOs e validacao de entrada.
- Persistir dados no PostgreSQL com schema versionado pelo Flyway.
- Expor health checks, metricas e logs conforme a
  [estrategia de observabilidade](observability.md).

## Comunicacao

```text
Usuario
  |
  v
React ----> Cognito/Google
  |
  v
Java Quiz SDK
  |  Authorization: Bearer <access token>
  v
Spring Boot API
  |
  v
PostgreSQL
```

O frontend recebe apenas dados necessarios para apresentar a questao. A API nao
envia a alternativa correta ou a explicacao enquanto a tentativa estiver em
andamento.

## Execucao local

Durante o desenvolvimento, React e Spring Boot podem executar diretamente na
maquina para facilitar hot reload e depuracao, enquanto o PostgreSQL executa no
Compose. Para validar o ambiente reproduzivel, o Compose executa toda a stack:

```text
Nginx/React :3000 --> Spring Boot :8080 --> PostgreSQL :5432 (rede interna)
                          |
                          `--> /actuator/prometheus <-- Prometheus :9090
```

O Compose publica o PostgreSQL como `5433` no host para evitar conflito com
instalacoes locais. Prometheus coleta a API pelo nome do servico na rede
interna. Grafana consulta essa fonte e carrega um dashboard local versionado.

## Destino de producao

```text
Amplify Hosting ou S3/CloudFront --> React
Cognito + Google                 --> identidade
App Runner + ECR                 --> API Spring Boot
RDS PostgreSQL                   --> dados
CloudWatch                       --> logs e metricas
Secrets Manager                  --> segredos do banco
```

Terraform e deploy AWS entram depois que o fluxo completo estiver validado
localmente.

## Principios

- Monolito modular no backend antes de considerar microsservicos.
- API stateless e autenticacao baseada em access token.
- Contratos externos separados das entidades persistidas.
- Dependencias apontando para abstracoes nas fronteiras externas.
- Mudancas pequenas, testaveis e entregues por Pull Request.
- Complexidade adicionada somente quando resolve uma necessidade observavel.
