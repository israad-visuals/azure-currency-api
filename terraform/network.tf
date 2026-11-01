resource "azurerm_postgresql_flexible_server_firewall_rule" "container_apps" {
  for_each = var.database_allowed_ips

  name             = "container-app-${replace(each.value, ".", "-")}"
  server_id        = azurerm_postgresql_flexible_server.database.id
  start_ip_address = each.value
  end_ip_address   = each.value
}
