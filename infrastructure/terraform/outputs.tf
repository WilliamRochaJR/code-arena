output "configuration" {
  description = "Identificacao nao sensivel da configuracao Terraform."
  value = {
    project     = var.project_name
    environment = var.environment
    region      = var.aws_region
  }
}
