# ADR 0007: Usar cliente OIDC generico no React

## Status

Aceita

## Data

2026-08-03

## Contexto

O frontend precisa iniciar Authorization Code com PKCE, restaurar e renovar a
sessao, tratar callback e fornecer somente o access token ao Java Quiz SDK. O
Amazon Cognito sera o provedor inicial, mas o SDK e as paginas do quiz nao devem
depender de APIs especificas da AWS.

## Decisao

Usar `react-oidc-context` como integracao React e `oidc-client-ts` como cliente
de protocolo. A aplicacao mantem uma abstracao propria de sessao e injeta o
access token no SDK por `tokenProvider`.

O modo `local` fornece uma sessao controlada sem token e sem conexao com AWS. O
modo `oidc` recebe issuer, dominio do Cognito, App Client e URLs de retorno por
variaveis publicas do Vite. Segredos nao fazem parte do bundle.

## Alternativas consideradas

### AWS Amplify Auth

Oferece integracao direta com Cognito e outros recursos Amplify. Para o MVP,
adicionaria uma camada maior e mais especifica da AWS, embora a aplicacao use
somente um User Pool como provedor OIDC.

### Implementar OAuth e PKCE manualmente

Reduz dependencias, mas transfere para o projeto responsabilidades sensiveis de
protocolo, armazenamento, callback, renovacao e validacao de estado.

### Acoplar o SDK ao Cognito

Simplificaria o primeiro consumidor, mas impediria reutilizacao do SDK e
contrariaria a fronteira arquitetural existente.

## Consequencias

### Positivas

- O fluxo usa padroes OIDC e OAuth em vez de APIs proprietarias nas paginas.
- O SDK permanece independente de React e Cognito.
- O modo local continua reproduzivel sem recursos AWS.
- Callback, estado, PKCE e renovacao ficam sob uma biblioteca especializada.

### Negativas

- Duas dependencias passam a fazer parte do bundle web.
- A integracao precisa adaptar o logout especifico do dominio Cognito.
- Tokens persistidos pela biblioteca exigem defesa continua contra XSS.
- A compatibilidade com Cognito real so sera comprovada depois do
  provisionamento da infraestrutura.
