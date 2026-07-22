# MVP do Code Arena

## Objetivo

O Code Arena e uma plataforma de questionarios tecnicos. A primeira versao
permite que uma pessoa autenticada avalie conhecimentos de Java e acompanhe sua
evolucao ao longo das tentativas.

O MVP deve comprovar um fluxo completo de produto e engenharia sem antecipar
funcionalidades administrativas ou de infraestrutura que nao sejam necessarias
para validar o questionario.

## Publico

- Pessoas estudando Java para entrevistas e avaliacoes tecnicas.
- Desenvolvedores que desejam identificar assuntos que precisam revisar.

## Fluxo principal

1. A pessoa entra com sua conta Google.
2. Escolhe uma dificuldade e uma ou mais categorias.
3. Inicia uma tentativa com dez questoes.
4. Responde uma questao por vez e pode revisar respostas anteriores.
5. Conclui a tentativa.
6. Recebe a pontuacao geral e o desempenho por categoria.
7. Consulta o historico de tentativas.

## Escopo funcional

### Autenticacao

- Entrar com Google por meio do Amazon Cognito.
- Restaurar uma sessao ainda valida ao abrir a aplicacao.
- Encerrar a sessao voluntariamente.
- Solicitar novo login quando a sessao nao puder ser renovada.
- Proteger no backend todos os recursos associados ao usuario.

### Configuracao do questionario

- Escolher uma dificuldade: iniciante, intermediaria ou avancada.
- Escolher uma ou mais categorias de Java.
- Criar uma tentativa com exatamente dez questoes ativas.
- Informar claramente quando nao houver questoes suficientes para os filtros.

### Execucao da tentativa

- Exibir uma questao por vez.
- Exibir o progresso atual e o total de questoes.
- Permitir selecionar apenas uma alternativa por questao.
- Permitir avancar, voltar e alterar uma resposta antes da conclusao.
- Preservar no backend as respostas ja enviadas.
- Nao revelar respostas corretas antes da conclusao.
- Nao impor limite de tempo no MVP.

### Resultado e historico

- Calcular a pontuacao exclusivamente no backend.
- Apresentar total de acertos, percentual e desempenho por categoria.
- Revelar explicacoes somente depois da conclusao.
- Listar o historico do usuario de forma paginada, do mais recente ao mais
  antigo.
- Impedir que um usuario consulte tentativas de outro usuario.

## Regras de negocio

- Cada tentativa pertence a um unico usuario.
- As dez questoes e sua ordem sao fixadas quando a tentativa e criada.
- Apenas questoes ativas podem entrar em uma nova tentativa.
- Uma alternativa selecionada precisa pertencer a questao respondida.
- Uma resposta pode ser alterada enquanto a tentativa estiver em andamento.
- Uma tentativa concluida e imutavel.
- A conclusao e idempotente: repetir a operacao devolve o mesmo resultado.
- O cliente nunca envia pontuacao, acerto ou resposta correta.
- A resposta correta e a explicacao nao aparecem nos contratos anteriores a
  conclusao.

## Criterios de qualidade

- Interface utilizavel em dispositivos moveis e desktop.
- Navegacao por teclado, foco visivel, labels e mensagens acessiveis.
- Estados de carregamento, vazio, erro e sucesso tratados explicitamente.
- Nenhuma chamada HTTP feita diretamente por componentes React; a aplicacao usa
  o SDK.
- Contratos externos tipados e validados.
- Regras relevantes cobertas por testes automatizados.
- Lint, testes e build executados no pipeline de Pull Request.

## Fora do MVP

- Cadastro e login com senha mantidos pela aplicacao.
- Painel administrativo e importacao em massa de questoes.
- Ranking global, certificados e gamificacao.
- Questionarios com limite de tempo.
- Pagamentos ou assinaturas.
- Internacionalizacao e tema escuro.
- Microsservicos e Kubernetes.
- Aplicativo mobile nativo.

## Criterio de conclusao

O MVP esta concluido quando o fluxo de login, configuracao, resposta, conclusao
e historico funcionar de ponta a ponta; os controles de seguranca impedirem
acesso cruzado; os testes, lint e builds passarem; e houver documentacao para
executar o sistema localmente.
