# Testes e diagnostico

## Instalar o Chromium do Playwright

Na raiz do repositorio:

```bash
npm run test:e2e:install
```

Baixa a versao do Chromium compativel com o Playwright instalado. A dependencia
Node e o binario do navegador possuem ciclos de instalacao separados.

## Executar testes E2E

Com a aplicacao local disponivel:

```bash
npm run test:e2e
PLAYWRIGHT_BASE_URL=<url> npm run test:e2e
npm run test:e2e:ui
```

O primeiro comando usa a URL padrao da configuracao. A variavel permite testar
outra origem, e o modo `ui` abre a interface interativa para desenvolvimento e
diagnostico. Traces, screenshots, videos e o relatorio HTML sao artefatos
gerados e nao devem ser versionados.

## Executar uma classe de teste Java

Dentro de `apps/api`:

```bash
./mvnw --batch-mode --no-transfer-progress \
  -Dtest=<ClasseDeTeste> test
```

Executa uma classe durante o desenvolvimento. E mais rapido para investigar um
cenario, mas nao substitui o `verify` completo antes do commit.

## Executar um metodo de teste Java

```bash
./mvnw --batch-mode --no-transfer-progress \
  -Dtest=<ClasseDeTeste>#<metodoDeTeste> test
```

Isola um unico comportamento. Depois da correcao, execute a classe e por fim a
suite completa.

## Consultar relatorios do Maven Surefire

Dentro de `apps/api`:

```bash
rg -n -C <linhas> \
  "FAILURE|ERROR|Tests run" \
  target/surefire-reports
```

Os relatorios ajudam quando a saida do Maven e extensa ou truncada. O diretorio
`target` e gerado localmente e nao deve ser versionado.

## Verificar whitespace

Na raiz:

```bash
git diff --check
rg -n '[[:blank:]]+$' <arquivos-novos>
```

`git diff --check` cobre alteracoes rastreadas. A busca com `rg` tambem permite
verificar arquivos novos que ainda nao foram adicionados ao stage.

## Acompanhar um processo iniciado no terminal

Quando um build ou servidor continua em execucao, acompanhe a mesma sessao ate
ela terminar. Nao inicie outra copia do comando apenas porque ainda nao houve
saida final.

No terminal interativo, `Ctrl+C` solicita a interrupcao do processo atual.
