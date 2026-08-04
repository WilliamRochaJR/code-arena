output "configuration" {
  description = "Identificacao nao sensivel da configuracao Terraform."
  value = {
    project     = var.project_name
    environment = var.environment
    region      = var.aws_region
  }
}

output "vpc_id" {
  description = "ID da VPC do Code Arena."
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "IDs das sub-redes publicas por Availability Zone."
  value       = { for availability_zone, subnet in aws_subnet.public : availability_zone => subnet.id }
}

output "private_subnet_ids" {
  description = "IDs das sub-redes privadas por Availability Zone."
  value       = { for availability_zone, subnet in aws_subnet.private : availability_zone => subnet.id }
}
