variable "project_name" {
  description = "Nome usado para identificar os recursos do Code Arena."
  type        = string
  default     = "code-arena"
}

variable "environment" {
  description = "Ambiente temporario representado pela configuracao."
  type        = string
  default     = "portfolio"
}

variable "aws_region" {
  description = "Regiao AWS usada pelo ambiente."
  type        = string
  default     = "us-east-1"
}

variable "monthly_budget_usd" {
  description = "Limite mensal monitorado pelo AWS Budgets em USD."
  type        = number
  default     = 10

  validation {
    condition     = var.monthly_budget_usd > 0
    error_message = "monthly_budget_usd deve ser maior que zero."
  }
}

variable "budget_notification_email" {
  description = "E-mail que recebe alertas de custo da conta AWS."
  type        = string
  sensitive   = true

  validation {
    condition     = can(regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", var.budget_notification_email))
    error_message = "budget_notification_email deve ser um e-mail valido."
  }
}

variable "vpc_cidr" {
  description = "Bloco IPv4 privado usado pela VPC."
  type        = string
  default     = "10.0.0.0/16"

  validation {
    condition     = can(cidrnetmask(var.vpc_cidr))
    error_message = "vpc_cidr deve ser um CIDR IPv4 valido."
  }
}
