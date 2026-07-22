# ADR 0001: Usar um monorepositorio

## Status

Aceita

## Data

2026-07-22

## Contexto

O Code Arena tera uma aplicacao React, uma API Spring Boot, um SDK TypeScript e
uma biblioteca de componentes. Contratos e funcionalidades atravessam esses
componentes e precisam evoluir de forma coordenada.

## Decisao

Manter aplicacoes, pacotes reutilizaveis, infraestrutura e documentacao em um
unico repositorio. Os projetos JavaScript usarao npm workspaces; a API Java
permanecera em `apps/api` com seu proprio Maven Wrapper.

## Alternativas consideradas

### Um repositorio por componente

Oferece ciclos independentes, mas aumenta a administracao, dificulta mudancas de
contrato atomicas e adiciona complexidade desnecessaria ao projeto atual.

### SDK e UI dentro da aplicacao React

Simplifica a estrutura inicial, mas nao demonstra pacotes independentes e cria
acoplamento com um unico consumidor.

## Consequencias

### Positivas

- Uma mudanca coordenada pode ser revisada em um Pull Request.
- Configuracoes, documentacao e execucao local ficam centralizadas.
- SDK e UI preservam fronteiras explicitas sem exigir repositorios separados.

### Negativas

- O pipeline precisa executar apenas trabalhos relevantes a cada mudanca.
- Workspaces e ferramentas compartilhadas exigem configuracao inicial.
- Versoes e dependencias entre pacotes precisam ser administradas com cuidado.
