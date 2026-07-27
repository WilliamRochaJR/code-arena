# ADR 0004: Usar Semantic Versioning e tags anotadas

## Status

Aceita

## Data

2026-07-27

## Contexto

O Code Arena reunira aplicacao web, API Java, SDK TypeScript e biblioteca de UI
no mesmo repositorio. O projeto precisa comunicar quais estados sao releases,
quando uma mudanca quebra compatibilidade e qual commit originou uma versao.

Criar versoes e tags sem uma politica pode gerar numeros divergentes entre npm e
Maven, tags apontando para branches nao aprovadas, reutilizacao de uma tag
publicada e changelogs inconsistentes.

O projeto ainda nao possui um marco funcional para release. A estrategia precisa
ser definida agora sem antecipar arquivos, automacoes ou publicacoes que ainda
nao podem ser validados.

## Decisao

Adotar Semantic Versioning no formato `MAJOR.MINOR.PATCH` para as releases do
produto.

- Versoes `0.x.y` representam desenvolvimento anterior ao MVP estavel.
- `1.0.0` representa o MVP estavel.
- Depois de `1.0.0`, `MAJOR` indica incompatibilidade, `MINOR` funcionalidade
  compativel e `PATCH` correcao compativel.
- Antes de `1.0.0`, marcos funcionais incrementam `MINOR`, e correcoes desses
  marcos incrementam `PATCH`.

Usar tags anotadas no formato `vMAJOR.MINOR.PATCH`. A versao nao possui `v` nos
arquivos; o prefixo pertence somente a tag.

Uma tag de release:

- e criada somente depois que `release/*` ou `hotfix/*` entra em `main` por Pull
  Request;
- aponta para um commit aprovado e presente em `main`;
- nao e criada em feature, `develop` ou release ainda nao mesclada;
- nao e movida, reutilizada nem apagada para corrigir uma release; uma correcao
  gera uma nova versao.

Manter inicialmente uma versao coordenada para o produto. Versoes independentes
para SDK e UI so serao adotadas se esses pacotes passarem a ser publicados e
evoluirem separadamente.

A primeira release sera conduzida manualmente com validacoes documentadas. O
Release Please e uma candidata futura, mas nenhuma ferramenta de release sera
adotada antes que o processo manual seja executado e avaliado.

## Alternativas consideradas

### Versionamento sem uma convencao formal

Foi rejeitado porque deixa o significado dos numeros dependente de julgamento
caso a caso e dificulta comunicar compatibilidade.

### Versoes independentes desde o inicio

Permitiria evoluir web, API, SDK e UI separadamente, mas adicionaria coordenacao
e changelogs multiplos antes de existir publicacao independente dos pacotes.

### Automatizar imediatamente com Release Please

Release Please usa Conventional Commits para propor versoes, changelog, tags e
GitHub Releases. A opcao foi adiada porque exige adaptar permissoes do Actions,
politica de branches e configuracao de monorepositorio antes da primeira release
manual.

### Usar Changesets

Changesets e adequado a pacotes npm em monorepositorios, mas nao coordena
naturalmente todo o produto React e Maven como uma unica release.

### Usar apenas Maven Release Plugin

Automatizaria o componente Java, mas nao seria a fonte de release para frontend,
SDK e UI.

## Consequencias

### Positivas

- O significado de cada versao fica previsivel.
- Tags identificam commits aprovados e imutaveis em `main`.
- O monorepositorio inicia com uma unica versao de produto.
- A automacao futura podera partir de um processo ja compreendido.

### Negativas

- A primeira release exige passos e verificacoes manuais.
- Versao, changelog e arquivos npm/Maven precisarao ser mantidos alinhados.
- Um Tag Ruleset e scripts de validacao ainda precisarao ser implementados antes
  da primeira tag.
- A estrategia precisara ser reavaliada se os pacotes forem publicados
  independentemente.

## Referencias

- [Semantic Versioning](https://semver.org/)
- [Guia de versionamento e releases](../guides/versioning-and-releases.md)
- [Release Please](https://github.com/googleapis/release-please)
- [Changesets](https://github.com/changesets/changesets)
- [Maven Release Plugin](https://maven.apache.org/maven-release/maven-release-plugin/)
