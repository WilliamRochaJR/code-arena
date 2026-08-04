# Terraform

Execute os comandos a partir da raiz do repositorio.

## Conferir a versao instalada

```bash
terraform version
```

Compara a CLI local com a restricao declarada em `versions.tf`.

## Formatar e validar sem provisionar

```bash
terraform -chdir=infrastructure/terraform fmt -check
terraform -chdir=infrastructure/terraform init -backend=false
terraform -chdir=infrastructure/terraform validate
```

`-chdir` executa no diretorio da infraestrutura sem exigir `cd`. O `init` baixa
providers e cria o lock file local; `-backend=false` evita configurar estado
remoto. Nenhum desses comandos cria recursos. `apply` e `destroy` exigem revisao
da conta, custos, plano e autorizacao explicita.
