# Java e Spring Boot

Execute estes comandos dentro de `apps/api`, salvo indicacao contraria.

## Conferir o Maven Wrapper

```bash
./mvnw --version
```

O Wrapper usa a versao Maven definida pelo projeto.

## Compilar, testar e empacotar

```bash
./mvnw --batch-mode --no-transfer-progress verify
```

`verify` inclui compilacao, testes, verificacao e empacotamento.

## Executar a API local

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Ativa `application-local.yml`. `Ctrl+C` inicia o graceful shutdown.

## Sobrescrever a conexao

```bash
DB_URL=<jdbc-url> \
DB_USERNAME=<usuario> \
DB_PASSWORD=<senha> \
SPRING_PROFILES_ACTIVE=local \
./mvnw spring-boot:run
```

Use credenciais somente no ambiente e nunca registre valores reais.

## Gerar um scaffold pelo Spring Initializr

```bash
curl --fail --location \
  "https://start.spring.io/starter.zip?<parametros>" \
  --output <arquivo.zip>
unzip <arquivo.zip> -d <diretorio>
```

Consulte os metadados atuais antes de escolher versoes e dependencias.

## Localizar artefatos no cache Maven

```bash
find ~/.m2/repository/org/springframework/boot \
  -name '*.jar' \
  -type f
```

Ajuda a diagnosticar dependencias baixadas. O cache nao e versionado.
