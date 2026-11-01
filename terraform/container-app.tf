resource "azurerm_container_app_environment" "currency_api" {
  name                       = "cae-currency-api"
  location                   = azurerm_resource_group.currency_api.location
  resource_group_name        = azurerm_resource_group.currency_api.name
  public_network_access      = "Enabled"
  logs_destination           = "log-analytics"
  log_analytics_workspace_id = azurerm_log_analytics_workspace.currency_api.id
  tags                       = local.tags

  workload_profile {
    name                  = "Consumption"
    workload_profile_type = "Consumption"
  }

  timeouts {
    delete = "60m"
  }
}

resource "azurerm_container_app" "api" {
  name                         = "ca-currency-api"
  container_app_environment_id = azurerm_container_app_environment.currency_api.id
  resource_group_name          = azurerm_resource_group.currency_api.name
  revision_mode                = "Single"
  workload_profile_name        = "Consumption"
  tags                         = local.tags

  identity {
    type = "SystemAssigned"
  }

  # The setup stage creates the identity before the API needs its Key Vault access.
  dynamic "secret" {
    for_each = local.api_enabled ? [1] : []

    content {
      name                = "db-password"
      identity            = "System"
      key_vault_secret_id = azurerm_key_vault_secret.database_password.versionless_id
    }
  }

  dynamic "ingress" {
    for_each = local.api_enabled ? [1] : []

    content {
      external_enabled           = true
      allow_insecure_connections = false
      target_port                = 8080
      transport                  = "auto"

      traffic_weight {
        latest_revision = true
        percentage      = 100
      }
    }
  }

  template {
    min_replicas = local.api_enabled ? var.min_replicas : 1
    max_replicas = 1

    container {
      name    = "currency-api"
      image   = local.api_enabled ? var.container_image : "docker.io/library/postgres:16.15-alpine"
      command = local.api_enabled ? null : ["sleep"]
      args    = local.api_enabled ? null : ["3600"]
      cpu     = 0.5
      memory  = "1Gi"

      dynamic "env" {
        for_each = local.api_enabled ? {
          DB_URL      = "jdbc:postgresql://${azurerm_postgresql_flexible_server.database.fqdn}:5432/currency_exchange?sslmode=require&connectTimeout=5&socketTimeout=10"
          DB_USERNAME = "currency_app"
        } : {}

        content {
          name  = env.key
          value = env.value
        }
      }

      dynamic "env" {
        for_each = local.api_enabled ? [1] : []

        content {
          name        = "DB_PASSWORD"
          secret_name = "db-password"
        }
      }

      dynamic "startup_probe" {
        for_each = local.api_enabled ? [1] : []

        content {
          transport               = "HTTP"
          port                    = 8080
          path                    = "/actuator/health/liveness"
          initial_delay           = 10
          interval_seconds        = 10
          timeout                 = 3
          failure_count_threshold = 30
        }
      }

      dynamic "liveness_probe" {
        for_each = local.api_enabled ? [1] : []

        content {
          transport               = "HTTP"
          port                    = 8080
          path                    = "/actuator/health/liveness"
          initial_delay           = 10
          interval_seconds        = 10
          timeout                 = 3
          failure_count_threshold = 3
        }
      }

      dynamic "readiness_probe" {
        for_each = local.api_enabled ? [1] : []

        content {
          transport               = "HTTP"
          port                    = 8080
          path                    = "/actuator/health/readiness"
          initial_delay           = 5
          interval_seconds        = 10
          timeout                 = 5
          failure_count_threshold = 3
          success_count_threshold = 1
        }
      }
    }
  }

  depends_on = [
    azurerm_postgresql_flexible_server_database.application,
    azurerm_postgresql_flexible_server_configuration.require_tls
  ]
}
