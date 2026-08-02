# JavaScript e TypeScript

## Criar a aplicacao React

```bash
npm create vite@latest apps/web -- --template react-ts --no-interactive
```

Gera o workspace React com TypeScript usando Vite.

## Instalar dependencias

```bash
npm install
npm ci
npm install <pacote> --workspace <workspace>
```

- `npm install` instala e pode atualizar o lockfile.
- `npm ci` reproduz exatamente o lockfile; nao cria commit nem faz push.
- `--workspace` registra a dependencia no pacote correto.

## Auditar dependencias

```bash
npm audit
```

Consulta vulnerabilidades. Sem `--fix`, nao atualiza pacotes.

## Executar a aplicacao web

```bash
npm run dev:web
```

Inicia o Vite do workspace `@code-arena/web`.

## Executar a suite de qualidade

```bash
npm run format:check
npm run lint
npm run typecheck
npm test
npm run build
```

Verifica Prettier, ESLint, tipos, testes e build dos workspaces aplicaveis.

Para listar os scripts disponiveis ou executar um check isolado de um
workspace:

```bash
npm run --workspace <workspace>
npm run <script> --workspace <workspace>
```

No Code Arena, `format:check` existe somente na raiz; lint, typecheck, testes e
build tambem podem ser executados no workspace durante o desenvolvimento.

## Formatar arquivos

```bash
npx prettier --write <arquivos>
```

Altera somente os caminhos informados. Depois, repita `format:check`.

## Instalar hooks e validadores de commit

```bash
npm install --save-dev \
  husky@^9.1.7 \
  lint-staged@16.4.0 \
  @commitlint/cli@^21.2.1 \
  @commitlint/config-conventional@^21.2.0
npx husky init
```

No Code Arena, o pre-commit executa `lint-staged` e o commit-msg executa
Commitlint.
