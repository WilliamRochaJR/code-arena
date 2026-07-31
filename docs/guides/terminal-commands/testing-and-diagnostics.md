# Testes e diagnostico

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
