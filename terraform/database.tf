resource "azurerm_postgresql_flexible_server" "database" {
  name                          = "pg-currency-${var.name_suffix}"
  location                      = azurerm_resource_group.currency_api.location
  resource_group_name           = azurerm_resource_group.currency_api.name
  version                       = "16"
  sku_name                      = "B_Standard_B1ms"
  storage_mb                    = 32768 # 32 GiB.
  storage_tier                  = "P4"
  auto_grow_enabled             = false
  backup_retention_days         = 7
  geo_redundant_backup_enabled  = false
  public_network_access_enabled = true
  administrator_login           = "currencyadmin"
  administrator_password        = var.database_admin_password
  tags                          = local.tags

  authentication {
    password_auth_enabled         = true
    active_directory_auth_enabled = false
  }
}

resource "azurerm_postgresql_flexible_server_database" "application" {
  name      = "currency_exchange"
  server_id = azurerm_postgresql_flexible_server.database.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_postgresql_flexible_server_configuration" "require_tls" {
  name      = "require_secure_transport"
  server_id = azurerm_postgresql_flexible_server.database.id
  value     = "on"
}
