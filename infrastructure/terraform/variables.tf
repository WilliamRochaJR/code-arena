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
