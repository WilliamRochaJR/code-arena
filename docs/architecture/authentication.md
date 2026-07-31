# Autenticacao e autorizacao

## Responsabilidades

O Amazon Cognito autentica o usuario e federa o login com Google. O React inicia
o Authorization Code Flow com PKCE e mantem o estado da sessao por meio da
biblioteca de autenticacao escolhida. A API Spring Boot atua somente como OAuth2
Resource Server.

```text
React --> Cognito --> Google
  ^          |
  |          | authorization code / tokens
  `----------'

React --> SDK --> Spring Boot
                  Authorization: Bearer <access token>
```

Nenhum client secret e armazenado no navegador. A API nao cria uma sessao HTTP
tradicional.

## Tokens

- O access token autoriza chamadas da API.
- O ID token fornece informacoes de apresentacao sobre a identidade.
- O refresh token permite a camada de autenticacao renovar a sessao.
- O ID token nao deve ser enviado no lugar do access token.

A duracao e a politica de renovacao serao configuradas no Cognito e documentadas
na entrega de infraestrutura.

## Fronteira entre autenticacao e SDK

O SDK nao conhece Cognito. A aplicacao injeta uma funcao capaz de fornecer um
access token valido:

```typescript
const client = new JavaQuizClient({
  baseUrl,
  getAccessToken: () => authService.getAccessToken(),
})
```

O `authService` coordena a renovacao para que requisicoes concorrentes aguardem
uma unica operacao. Uma requisicao pode ser repetida no maximo uma vez depois da
renovacao; uma nova resposta `401` encerra a sessao para evitar loops.

## Validacao no backend

Antes de executar um endpoint protegido, o Spring Security valida:

- assinatura do JWT pelas chaves publicas do emissor;
- emissor esperado;
- expiracao e instante de validade;
- finalidade do token e identificador do cliente quando aplicavel;
- claims necessarias para autorizacao.

O identificador externo do usuario e a claim `sub`. E-mail pode mudar e nao deve
ser usado como chave de integracao.

## Usuario local

Na primeira chamada autenticada, a API busca o usuario por `sub`. Se ainda nao
existir, cria o perfil local com os dados permitidos. Chamadas posteriores podem
atualizar nome, e-mail e instante do ultimo acesso sem alterar o identificador.

## Autorizacao

O MVP possui o papel `USER`, com acesso apenas aos proprios recursos. O papel
`ADMIN` sera introduzido junto ao painel administrativo.

As verificacoes de propriedade acontecem no backend. Rotas protegidas no React
melhoram a navegacao, mas nao constituem controle de seguranca.

## Respostas de seguranca

- `401 Unauthorized`: token ausente, invalido ou expirado.
- `403 Forbidden`: identidade valida sem permissao para a operacao.
- `404 Not Found`: recurso inexistente ou nao visivel para o usuario, quando a
  resposta uniforme evitar enumeracao de recursos.

## Desenvolvimento local

A integracao real com Cognito sera adicionada depois que o dominio do quiz
funcionar. Nos perfis `local` e `test`, a API fornece uma identidade controlada
pela abstracao `CurrentUserProvider` e permite acesso a `/api/v1/**` sem token.
Os valores locais podem ser substituidos por `CONTROLLED_USER_SUBJECT`,
`CONTROLLED_USER_EMAIL` e `CONTROLLED_USER_DISPLAY_NAME`.

Esse mecanismo nao e carregado no perfil padrao, permanece desabilitado em
producao e nao aceita identidade enviada por cabecalhos. Na integracao com
Cognito, uma nova implementacao de `CurrentUserProvider` usara as claims
validadas do access token sem alterar os servicos de negocio.

O `CurrentUserService` resolve a identidade fornecida e cria o `AppUser` na
primeira operacao que precisa de um usuario persistido. Resolucoes seguintes
reutilizam o registro pelo `subject`, que permanece a chave externa estavel.

## Cuidados

- Nao registrar tokens, authorization codes ou credenciais.
- Nao versionar arquivos `.env` com valores reais.
- Aplicar HTTPS fora do ambiente local.
- Restringir CORS as origens conhecidas.
- Evitar armazenamento manual de tokens quando a biblioteca de autenticacao
  puder administrar a sessao com seguranca.
- Tratar logout e falha de renovacao cancelando requisicoes pendentes.
