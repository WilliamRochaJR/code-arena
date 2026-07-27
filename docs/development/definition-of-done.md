# Definition of Done

Uma tarefa do Code Arena so pode ser considerada concluida quando os itens
aplicaveis abaixo forem atendidos.

## Requisitos

- Criterios de aceite estao claros e atendidos.
- Comportamentos fora do escopo estao registrados.
- Decisoes relevantes foram documentadas em ADR.

## Implementacao

- A alteracao tem responsabilidade clara e escopo pequeno.
- Contratos publicos permanecem tipados e compativeis ou possuem migracao.
- Regras de seguranca e privacidade foram consideradas.
- Nao existem segredos, credenciais ou dados ficticios em codigo de producao.
- Acessibilidade e responsividade foram verificadas quando ha interface.

## Qualidade

- Testes relevantes cobrem sucesso, falhas e casos de borda.
- Um bug corrigido possui teste de regressao quando viavel.
- Lint, formatacao, typecheck, testes e builds aplicaveis passam.
- O diff foi revisado e nao contem alteracoes acidentais.

## Documentacao

- README, guias, contratos e ADRs aplicaveis estao atualizados.
- Todos os blocos de terminal executados na entrega foram registrados
  cronologicamente, com objetivo e resultado, sem valores sensiveis.
- A documentacao nao contem valores secretos.

## Entrega

- Commits possuem intencao unica e mensagem Conventional Commit.
- O Pull Request descreve objetivo, alteracoes, validacoes e riscos.
- Conversas de revisao foram resolvidas.
- A branch esta atualizada conforme a politica vigente.
- Validacoes locais e checks de CI aplicaveis estao verdes.

## Producao

Quando houver deploy:

- Migrations e compatibilidade de dados foram avaliadas.
- Variaveis e segredos necessarios estao configurados fora do codigo.
- Health checks e smoke tests passaram.
- Logs nao expoem tokens ou dados sensiveis.
- Existe uma estrategia de rollback ou recuperacao proporcional ao risco.
