locals {
  api_url = "https://${azurerm_container_app.api.name}.${azurerm_container_app_environment.currency_api.default_domain}"
}

output "resource_group_name" {
  value = azurerm_resource_group.currency_api.name
}

output "container_app_outbound_ips" {
  description = "Allow these addresses through the PostgreSQL firewall before database setup."
  value       = azurerm_container_app.api.outbound_ip_addresses
}

output "container_app_name" {
  value = azurerm_container_app.api.name
}

output "key_vault_name" {
  value = azurerm_key_vault.currency_api.name
}

output "database_host" {
  value = azurerm_postgresql_flexible_server.database.fqdn
}

output "database_setup_command" {
  description = "Run in the setup container's portal console; enter the administrator password at the prompt."
  value       = "psql 'host=${azurerm_postgresql_flexible_server.database.fqdn} port=5432 dbname=currency_exchange user=currencyadmin sslmode=require' --password"
}

output "api_url" {
  value = local.api_enabled ? local.api_url : null
}

output "readiness_url" {
  value = local.api_enabled ? "${local.api_url}/actuator/health/readiness" : null
}

output "conversion_url" {
  value = local.api_enabled ? "${local.api_url}/api/convert?amount=100&from=USD&to=CAD" : null
}
