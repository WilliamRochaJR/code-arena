# Arquivos e ambiente

## Localizar arquivos e texto

```bash
rg --files <diretorios>
rg -n -C <linhas-de-contexto> "<padrao>" <arquivos-ou-diretorios>
sed -n '<inicio>,<fim>p' <arquivo>
tail -n <quantidade> <arquivo>
```

- `rg --files` lista arquivos e respeita regras como `.gitignore`.
- `rg -n` pesquisa texto, mostra numeros de linha e adiciona contexto com `-C`.
- `sed -n` le somente um intervalo.
- `tail` mostra as ultimas linhas, sendo util para logs.

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
