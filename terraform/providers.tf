provider "azurerm" {
  subscription_id                 = var.subscription_id
  tenant_id                       = var.tenant_id
  resource_provider_registrations = "none"

  features {
    key_vault {
      purge_soft_delete_on_destroy          = true
      purge_soft_deleted_secrets_on_destroy = true
    }

    log_analytics_workspace {
      permanently_delete_on_destroy = true
    }
  }
}

data "azurerm_client_config" "current" {}
