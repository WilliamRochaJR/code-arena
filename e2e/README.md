# Testes E2E

Este diretorio concentra cenarios Playwright que atravessam frontend, SDK, API
e banco em um navegador real. A infraestrutura inicial usa Chromium e espera a
aplicacao em `http://127.0.0.1:3000` por padrao.

O cenario `quiz-flow.spec.ts` percorre a selecao de categoria e dificuldade,
responde todas as perguntas, conclui o quiz, valida o resultado e confirma a
tentativa no historico. Cada execucao cria uma nova tentativa e identifica seu
ID pela navegacao, sem depender dos registros deixados por execucoes anteriores.

Em falhas, Playwright preserva trace, screenshot e video em `test-results/` e
gera o relatorio HTML em `playwright-report/`. Esses diretorios sao artefatos
locais e nao devem ser versionados.
