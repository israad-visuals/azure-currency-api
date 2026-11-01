variable "subscription_id" {
  description = "Azure for Students subscription to deploy into."
  type        = string

  validation {
    condition     = length(var.subscription_id) == 36
    error_message = "Enter the 36-character subscription ID from Azure."
  }
}

variable "tenant_id" {
  description = "Microsoft Entra tenant that owns the subscription."
  type        = string

  validation {
    condition     = length(var.tenant_id) == 36
    error_message = "Enter the 36-character tenant ID from Azure."
  }
}

variable "location" {
  description = "Azure region used for the portal deployment."
  type        = string
  default     = "northcentralus"
}

variable "name_suffix" {
  description = "Unique suffix for the PostgreSQL server and Key Vault names."
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9]{3,8}$", var.name_suffix))
    error_message = "Use 3 to 8 lowercase letters or numbers."
  }
}

variable "deployment_stage" {
  description = "Use setup first, then api after creating the database login."
  type        = string
  default     = "setup"

  validation {
    condition     = contains(["setup", "api"], var.deployment_stage)
    error_message = "deployment_stage must be setup or api."
  }
}

variable "container_image" {
  description = "Public GHCR image reference for the Java API."
  type        = string
  default     = "ghcr.io/israad-visuals/currency-api@sha256:b95c50f44d02f0309467fc258ea06892006333c1e049143538d7c49dd127de5d"
}

variable "min_replicas" {
  description = "Use 0 so the API can sleep between visits; 1 keeps it running."
  type        = number
  default     = 0

  validation {
    condition     = contains([0, 1], var.min_replicas)
    error_message = "min_replicas must be 0 or 1."
  }
}

variable "database_allowed_ips" {
  description = "Copy the Container App's outbound IP addresses here after the first setup apply."
  type        = set(string)
  default     = []

  validation {
    condition     = !contains(var.database_allowed_ips, "0.0.0.0")
    error_message = "Use individual outbound IPv4 addresses; 0.0.0.0 allows all Azure customers."
  }

  validation {
    condition     = var.deployment_stage != "api" || length(var.database_allowed_ips) > 0
    error_message = "Before switching to api, copy the setup app's outbound IP addresses into database_allowed_ips and apply the setup plan again."
  }
}

variable "database_admin_password" {
  description = "Password for currencyadmin. Keep it separate from the application password."
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.database_admin_password) >= 16 && length(var.database_admin_password) <= 128
    error_message = "Choose an administrator password between 16 and 128 characters."
  }
}

variable "database_app_password" {
  description = "Password stored in Key Vault; use this same value in the SQL password prompt."
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.database_app_password) >= 16 && length(var.database_app_password) <= 128
    error_message = "Choose an application password between 16 and 128 characters."
  }

  validation {
    condition     = var.database_app_password != var.database_admin_password
    error_message = "The application and administrator passwords must be different."
  }
}
