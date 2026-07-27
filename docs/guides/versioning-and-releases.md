# Versionamento, releases e tags

Este guia aplica a decisao registrada no
[ADR 0004](../decisions/0004-use-semantic-versioning.md). Ele diferencia a
politica vigente das protecoes e automacoes que ainda serao implementadas.

## Estado atual

```text
Politica SemVer: definida
Primeira release: ainda nao criada
Tags de release: nenhuma
VERSION: planejado antes da primeira release
CHANGELOG.md: planejado antes da primeira release
Tag Ruleset: planejado antes da primeira tag
Automacao de release: nao adotada
```

O valor `0.0.0` dos arquivos npm e um placeholder de desenvolvimento, nao uma
release publicada.

## Para lembrar: versao nao e tag

```text
Versao         numero que comunica compatibilidade: 0.1.0
Tag Git        referencia para um commit: v0.1.0
GitHub Release pagina e notas publicadas a partir da tag
```

Versao e uma convencao do produto. Ela pode aparecer em `VERSION`,
`package.json`, `pom.xml`, changelog e interface. Git nao interpreta SemVer nem
mantem esses arquivos sincronizados automaticamente.

Tag e um recurso nativo do Git. Ela associa um nome estavel a um ponto do
historico:

```text
refs/tags/v0.1.0
        |
        v
objeto de tag anotada
        |
        v
commit aprovado em main
```

Criar a tag nao altera o commit, nao cria outro snapshot e nao atualiza arquivos
de versao. Por isso os arquivos precisam estar corretos antes do merge e da tag.

Uma GitHub Release tambem nao e a tag. Ela e uma publicacao do GitHub ligada a
uma tag, com titulo, notas e possiveis artefatos.

## Semantic Versioning

As releases usam:

```text
MAJOR.MINOR.PATCH
```

| Alteracao                        | Exemplo         | Incremento |
| -------------------------------- | --------------- | ---------- |
| Marco funcional anterior ao MVP  | `0.1.0 → 0.2.0` | `MINOR`    |
| Correcao de um marco `0.x`       | `0.2.0 → 0.2.1` | `PATCH`    |
| Primeiro MVP estavel             | `0.x.y → 1.0.0` | `MAJOR`    |
| Correcao compativel apos `1.0.0` | `1.2.0 → 1.2.1` | `PATCH`    |
| Funcionalidade compativel        | `1.2.1 → 1.3.0` | `MINOR`    |
| Mudanca publica incompativel     | `1.3.0 → 2.0.0` | `MAJOR`    |

Antes de `1.0.0`, uma mudanca incompativel em um marco publicado incrementa
`MINOR` e volta `PATCH` para zero.

## Versao do produto e tag

A versao nos arquivos nao possui prefixo:

```text
0.1.0
```

A tag correspondente usa `v`:

```text
v0.1.0
```

Tags de release sao anotadas:

```bash
git tag -a v0.1.0 -m "Code Arena 0.1.0"
```

Uma tag anotada registra objeto proprio, autor, data e mensagem. Ela deve apontar
para o commit aprovado em `main`.

### Como o comando funciona

```text
git tag   comando de tags
-a        cria uma tag anotada
v0.1.0    nome da tag
-m        fornece a mensagem sem abrir um editor
```

Sem outro commit informado, o Git aponta a tag para `HEAD`, ou seja, o commit
atualmente selecionado. Esse e o motivo para trocar para `main`, atualiza-la e
confirmar o commit antes de executar o comando.

A tag nasce apenas no repositorio local. Ela so chega ao GitHub com:

```bash
git push origin v0.1.0
```

Um push comum da branch nao envia automaticamente todas as tags. O envio da tag
e uma acao separada e continua exigindo confirmacao.

### Tag leve e tag anotada

Uma tag leve armazena apenas uma referencia direta:

```bash
git tag v0.1.0
```

Uma tag anotada cria um objeto com autor, data e mensagem:

```bash
git tag -a v0.1.0 -m "Code Arena 0.1.0"
```

O Code Arena usa a segunda forma porque uma release precisa de contexto
auditavel. A forma leve nao deve ser usada para releases.

### Como consultar

```bash
git tag --list
git show v0.1.0
git cat-file -t v0.1.0
git branch --contains v0.1.0
```

- `git tag --list` lista as tags locais.
- `git show` apresenta os dados da tag e do commit associado.
- `git cat-file -t` retorna `tag` quando ela e anotada; uma tag leve resolve
  diretamente para `commit`.
- `git branch --contains` ajuda a confirmar quais branches contem o commit
  marcado.

## Resumo operacional

```text
1. Escolher a versao:          0.1.0
2. Atualizar arquivos:         VERSION, package.json, pom.xml, CHANGELOG
3. Mesclar release/* em main:  somente apos checks
4. Atualizar main local:       git pull --ff-only origin main
5. Criar tag anotada:          git tag -a v0.1.0 -m "Code Arena 0.1.0"
6. Enviar a tag:               git push origin v0.1.0
7. Publicar GitHub Release:     notas associadas a v0.1.0
```

Frase curta para lembrar:

> A versao descreve a release; a tag marca seu commit; o GitHub Release publica
> suas notas.

## Imutabilidade

Depois de publicada, uma tag nao deve ser movida, reutilizada ou apagada. Se
`v0.1.0` possuir um defeito, a correcao gera `v0.1.1`.

Nao criar tags em:

- `feature/*`, `fix/*`, `docs/*` ou `chore/*`;
- `develop`;
- `release/*` antes do merge;
- commits locais ainda nao presentes em `origin/main`.

## Fluxo de release

### 1. Criar a branch

Parta de `develop` validada:

```bash
git switch develop
git pull --ff-only origin develop
git switch -c release/0.1.0
```

### 2. Preparar a versao

Quando esses artefatos existirem, a release deve:

- atualizar `VERSION`;
- alinhar versões aplicaveis em `package.json` e `apps/api/pom.xml`;
- atualizar `CHANGELOG.md`;
- revisar documentacao e criterios do marco;
- executar todas as validacoes.

O commit de preparacao segue:

```text
chore(release): prepare 0.1.0
```

### 3. Abrir o Pull Request

```text
base: main
compare: release/0.1.0
```

O check `Pull request policy` aceita `release/*` para `main`. Os demais checks
obrigatorios precisam passar antes do merge.

### 4. Criar a tag

Somente depois do merge:

```bash
git fetch --prune origin
git switch main
git pull --ff-only origin main
git tag --list "v0.1.0"
git tag -a v0.1.0 -m "Code Arena 0.1.0"
git push origin v0.1.0
```

Antes do `git tag`, confirme que a tag nao existe e que `HEAD` corresponde ao
commit aprovado em `origin/main`. O push da tag continua exigindo confirmacao do
mantenedor.

### 5. Sincronizar correcoes

Correcoes realizadas exclusivamente durante a release precisam voltar para
`develop` por um Pull Request controlado. Nao use `develop` como head de um PR
para `main`.

### 6. Publicar as notas

Uma GitHub Release associada a tag deve resumir:

- funcionalidades;
- correcoes;
- mudancas incompativeis;
- instrucoes de migracao, quando existirem;
- artefatos e verificacoes da entrega.

Na primeira release, essa etapa sera manual.

## Versao coordenada no monorepositorio

Inicialmente, uma release identifica o Code Arena como produto. Web, API, SDK e
UI permanecem coordenados pela mesma versao quando seus arquivos declararem uma
versao de release.

Se SDK ou UI forem publicados para consumidores independentes, uma nova decisao
deve avaliar versionamento e changelog por pacote.

## Protecoes planejadas

Antes da primeira tag, implementar:

- `VERSION` como fonte principal da versao do produto;
- `CHANGELOG.md`;
- script que valide SemVer, branch, arvore limpa, sincronizacao com
  `origin/main`, alinhamento dos arquivos e existencia local/remota da tag;
- Tag Ruleset direcionado a `v*`, bloqueando exclusao e atualizacao;
- check de politica de release para comparar branch, versao e changelog.

Nao ativar `Restrict creations` no Tag Ruleset ate existir um ator autorizado
para criar tags sem bloquear o proprio processo.

## Automacao futura

Release Please e a candidata preferencial depois que a primeira release manual
for avaliada. Se adotada, a ferramenta deve ser a unica responsavel por calcular
versao, atualizar changelog, criar tag e publicar a GitHub Release.

A adocao exige uma nova decisao porque altera:

- o fluxo de `release/*`;
- as origens permitidas pelo `Pull request policy`;
- permissoes de escrita do GitHub Actions;
- arquivos de configuracao e fonte da versao.

## Checklist

- a versao segue `MAJOR.MINOR.PATCH`;
- a branch se chama `release/MAJOR.MINOR.PATCH` ou `hotfix/MAJOR.MINOR.PATCH-*`;
- os arquivos de versao aplicaveis estao alinhados;
- o changelog possui a versao;
- o PR aponta para `main`;
- os checks obrigatorios passaram;
- a tag usa `vMAJOR.MINOR.PATCH`;
- a tag e anotada e aponta para `main`;
- a tag ainda nao existe local ou remotamente;
- o push da tag foi confirmado;
- correcoes da release voltaram para `develop`.
