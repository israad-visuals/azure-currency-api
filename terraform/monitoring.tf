resource "azurerm_log_analytics_workspace" "currency_api" {
  name                = "log-currency-api"
  location            = azurerm_resource_group.currency_api.location
  resource_group_name = azurerm_resource_group.currency_api.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = local.tags
}
