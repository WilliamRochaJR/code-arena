# Comandos de terminal

Este documento registra comandos importantes usados para construir e operar o
Code Arena. Ele funciona como referencia de estudo e trilha reproduzivel; nao e
uma transcricao de toda consulta exploratoria.

## Convencoes

- Execute os comandos a partir da raiz do repositorio, salvo indicacao contraria.
- Leia a explicacao antes de executar comandos que alteram branches ou estado.
- Nunca copie credenciais reais para exemplos.
- Atualize este guia quando um novo procedimento se tornar parte do projeto.

## Verificar ferramentas locais

```bash
java -version
javac -version
mvn -version
node --version
npm --version
yarn --version
```

Versoes verificadas no inicio do projeto:

```text
Java: 21.0.11
Maven: 3.6.3
Node.js: 22.12.0
npm: 10.9.0
Yarn: 1.22.22
```

Gradle e pnpm nao estavam instalados e nao sao necessarios para a stack
escolhida.

## Inicializar o repositorio

```bash
git init -b main
git remote add origin git@github.com:william-rocha/code-arena.git
```

O primeiro comando cria o repositorio com `main` como branch inicial. O segundo
associa o checkout local ao repositorio GitHub.

## Conferir remoto e estado

```bash
git remote -v
git status --short --branch
git branch -vv
git log --oneline --decorate --graph --all --max-count=15
```

Esses comandos ajudam a confirmar remoto, branch atual, tracking e historico sem
alterar arquivos.

## Publicacao inicial das branches

```bash
git push -u origin main
git push -u origin develop
git push -u origin feature/001-project-documentation
```

`-u` configura a branch remota como upstream da branch local.

## Sincronizar referencias

```bash
git fetch --prune origin
```

O comando baixa referencias e remove localmente referencias de branches remotas
que ja foram apagadas.

## Atualizar sem merge implicito

```bash
git pull --ff-only origin develop
```

`--ff-only` falha se a atualizacao exigiria um merge, permitindo investigar a
divergencia antes de modificar o historico.

## Restauracao de develop ocorrida no projeto

A exclusao automatica de branches removeu `develop` quando ela foi usada como
origem de um Pull Request para `main`. Como `origin/main` continha todo o
historico aprovado, a restauracao foi feita com:

```bash
git switch develop
git merge --ff-only origin/main
git push -u origin develop
```

O merge em fast-forward atualizou a referencia local sem criar um commit novo; o
push recriou a branch remota. O incidente motivou a observacao sobre branches
permanentes no guia de Git.

## Validar documentacao antes do commit

```bash
git diff --check
git status --short --branch
git diff --stat
```

Essas verificacoes identificam problemas de whitespace, mostram o estado e
resumem o tamanho da alteracao.

## Comandos futuros

Comandos de npm workspaces, frontend, SDK, UI, Spring Boot, Docker, testes e
deploy serao adicionados quando as respectivas entregas forem implementadas e
validadas.
