locals {
  api_enabled = var.deployment_stage == "api"

  tags = {
    project     = "currency-api"
    environment = "dev"
  }
}

resource "azurerm_resource_group" "currency_api" {
  name     = "rg-currency-api-dev"
  location = var.location
  tags     = local.tags
}
