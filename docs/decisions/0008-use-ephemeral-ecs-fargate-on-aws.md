# ADR 0008: Usar ECS Fargate em ambiente AWS temporario

## Status

Aceita

## Data

2026-08-03

## Contexto

O Code Arena precisa demonstrar deploy de uma API Spring Boot containerizada,
frontend React, autenticacao e PostgreSQL na AWS. O ambiente de portfolio deve
ser reproduzivel por Terraform e removido depois das demonstracoes para limitar
custos. App Runner era o destino inicialmente planejado, mas deixou de aceitar
novos clientes em 31 de marco de 2026.

Free Tier e creditos variam conforme a data e a conta. Fargate, Application Load
Balancer, RDS, IPv4, ECR, CloudWatch e Secrets Manager podem gerar cobranca
enquanto seus recursos existirem.

## Decisao

O frontend usara Amplify Hosting, a identidade usara Cognito e a API sera
publicada como imagem no ECR e executada no ECS Fargate atras de um Application
Load Balancer. O PostgreSQL usara RDS Single-AZ; logs e metricas iniciais irao
para CloudWatch, e segredos operacionais ficarao no Secrets Manager.

O Terraform sera dividido em entregas pequenas. O primeiro bootstrap nao cria
recursos. Quando o provisionamento for implementado, o ambiente sera temporario:
`apply` para validacao controlada e `destroy` depois da demonstracao. Orcamento,
alertas e revisao do plano antecedem qualquer `apply`.

## Alternativas consideradas

### App Runner

Simplificaria a execucao do container e permitiria pausar compute, mas nao esta
disponivel para novos clientes desde 31 de marco de 2026.

### Lightsail Containers

Oferece endpoint HTTPS e preco previsivel, mas cobra ate que o servico seja
apagado e demonstra menos componentes da arquitetura AWS buscada no portfolio.

### Lambda

Escala a zero e pode reduzir custo ocioso, mas exigiria adaptar o modelo de
execucao da API Spring Boot e ampliar o escopo desta entrega.

### Elastic Beanstalk com instancia unica

Reduz a operacao de EC2 e evita load balancer, mas mantem uma instancia minima,
tem menor tolerancia a falhas e muda o modelo de empacotamento planejado.

## Consequencias

### Positivas

- Demonstra containers, registry, rede, balanceamento e IaC.
- Preserva a imagem Docker existente da API.
- Permite recriar e destruir o ambiente de forma auditavel.
- Mantem autenticacao, aplicacao e banco em servicos com responsabilidades claras.

### Negativas

- Nao existe garantia de custo zero, mesmo com Free Tier.
- ALB e RDS possuem custos enquanto existirem e impedem um scale-to-zero completo.
- O ambiente exige destruicao intencional depois de cada demonstracao.
- A arquitetura possui mais recursos e configuracao de rede que Lightsail.

## Referencias

- Issue #48.
- [AWS Fargate pricing](https://aws.amazon.com/fargate/pricing/).
- [Amazon RDS Free Tier](https://aws.amazon.com/rds/free/).
- [AWS App Runner PauseService](https://docs.aws.amazon.com/apprunner/latest/api/API_PauseService.html).
- [AWS Amplify pricing](https://aws.amazon.com/amplify/pricing/).
