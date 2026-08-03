# Testes E2E

Este diretorio concentra cenarios Playwright que atravessam frontend, SDK, API
e banco em um navegador real. A infraestrutura inicial usa Chromium e espera a
aplicacao em `http://127.0.0.1:3000` por padrao.

Os cenarios do fluxo principal do quiz serao adicionados na proxima unidade da
issue #35. Ate la, os comandos e a coleta de artefatos estao configurados, mas
nao existe um teste E2E executavel.

Em falhas, Playwright preserva trace, screenshot e video em `test-results/` e
gera o relatorio HTML em `playwright-report/`. Esses diretorios sao artefatos
locais e nao devem ser versionados.
