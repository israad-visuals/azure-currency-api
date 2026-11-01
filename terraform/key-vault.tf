resource "azurerm_key_vault" "currency_api" {
  name                          = "kv-currency-${var.name_suffix}"
  location                      = azurerm_resource_group.currency_api.location
  resource_group_name           = azurerm_resource_group.currency_api.name
  tenant_id                     = var.tenant_id
  sku_name                      = "standard"
  rbac_authorization_enabled    = true
  public_network_access_enabled = true
  soft_delete_retention_days    = 90
  purge_protection_enabled      = false
  tags                          = local.tags

  network_acls {
    default_action = "Allow"
    bypass         = "None"
  }
}

resource "azurerm_role_assignment" "terraform_secrets" {
  scope                = azurerm_key_vault.currency_api.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}

resource "azurerm_key_vault_secret" "database_password" {
  name         = "currency-db-password"
  value        = var.database_app_password
  key_vault_id = azurerm_key_vault.currency_api.id

  depends_on = [azurerm_role_assignment.terraform_secrets]
}

resource "azurerm_role_assignment" "app_secrets" {
  scope                            = azurerm_key_vault.currency_api.id
  role_definition_name             = "Key Vault Secrets User"
  principal_id                     = azurerm_container_app.api.identity[0].principal_id
  skip_service_principal_aad_check = true
}
