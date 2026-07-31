# Arquivos e ambiente

## Mostrar o diretorio atual

```bash
pwd
```

Confirma em qual diretorio os proximos comandos serao executados.

## Localizar arquivos e texto

```bash
rg --files <diretorios>
rg -n -C <linhas-de-contexto> "<padrao>" <arquivos-ou-diretorios>
find <diretorio> -maxdepth <niveis> -type f -print
sed -n '<inicio>,<fim>p' <arquivo>
tail -n <quantidade> <arquivo>
```

- `rg --files` lista arquivos e respeita regras como `.gitignore`.
- `rg -n` pesquisa texto, mostra numeros de linha e adiciona contexto com `-C`.
- `find` permite filtrar por tipo e limitar a profundidade da arvore.
- `sed -n` le somente um intervalo.
- `tail` mostra as ultimas linhas, sendo util para logs.

## Contar linhas

```bash
wc -l <arquivos>
```

Mostra o numero de linhas por arquivo e o total quando recebe varios caminhos.
Ajuda a revisar o tamanho de um escopo, mas nao mede sozinho sua complexidade.

## Localizar um executavel

```bash
command -v <ferramenta>
```

Mostra o caminho encontrado pelo shell e nao instala nada.

## Conferir versoes da stack

```bash
java -version
javac -version
mvn -version
node --version
npm --version
docker --version
docker compose version
```

A API usa o Maven Wrapper, portanto nao depende da versao global do Maven.

## Identificar quem escuta em uma porta

```bash
ss -ltnp 'sport = :<porta>'
```

Ajuda a diagnosticar conflitos. O processo pode ficar oculto sem permissao
suficiente.

## Verificar variaveis sem revelar valores

```bash
compgen -e | rg '^<PREFIXO>_'
```

Lista somente nomes de variaveis. E mais seguro que imprimir todo o ambiente
quando podem existir credenciais.
