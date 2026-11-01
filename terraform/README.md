# Currency API on Azure

This is the lower-cost portfolio deployment: public HTTPS ingress, a Java Container App, PostgreSQL, Key Vault and Log Analytics. It uses one Terraform folder and local state.

The portal exercise used a custom VNet and private PostgreSQL. This version uses default Container Apps networking and a public PostgreSQL endpoint with individual outbound IP firewall rules. This removes the custom-network load balancer, public IP and private DNS costs while retaining TLS, a restricted database login and managed-identity access to Key Vault. The original private-network diagram is a separate lab design.

The deployment has two stages. `setup` runs a temporary PostgreSQL client so you can create the restricted database login. `api` replaces that client with the Java image in the same Container App. Start every fresh deployment with `setup`.

## Read the code in this order

| File | Purpose |
| --- | --- |
| `versions.tf`, `providers.tf` | Terraform/provider versions, Azure account selection and permanent lab cleanup settings. |
| `variables.tf`, `terraform.tfvars.example` | Inputs and your local values. |
| `main.tf` | Resource group and shared tags. |
| `network.tf` | PostgreSQL firewall rules for the app's outbound IP addresses. |
| `database.tf` | PostgreSQL 16, B1ms, 32 GiB, TLS and the `currency_exchange` database. |
| `monitoring.tf` | Log Analytics workspace. |
| `key-vault.tf` | Application password, Terraform secret-management access and app read access. |
| `container-app.tf` | Environment, container, identity, ingress, environment variables and health probes. |
| `outputs.tf` | Resource names, database connection command and API addresses. |

`local.api_enabled` is true when the stage is `api`. The `dynamic` blocks in `container-app.tf` add the API settings only in that stage: `[1]` includes a block once; `[]` leaves it out. The environment-variable map adds the named settings. No modules or separate environment folders are needed.

## Before deploying

- Use Terraform 1.11 or later, below 2.0, and Azure CLI for sign-in. Keep `.terraform.lock.hcl`; it records the tested AzureRM 4.81.0 provider.
- Confirm the permitted Azure for Students subscription, remaining credit, current prices, regional availability and B1ms quota. PostgreSQL compute/storage still cost money when the API has zero replicas. Logs, requests and data transfer can add usage charges.
- Your account needs resource creation, role-assignment creation, and permission to purge the lab vault during destroy. A subscription Owner normally has these permissions; Contributor alone cannot create role assignments.
- In the subscription's **Resource providers** page, check that `Microsoft.App`, `Microsoft.DBforPostgreSQL`, `Microsoft.KeyVault`, `Microsoft.OperationalInsights` and `Microsoft.ManagedIdentity` are registered. Register any missing ones and wait for **Registered**. The provider is configured not to register unrelated services automatically.
- The resource group `rg-currency-api-dev` must be available. Use a different `name_suffix` if the server or vault name is already taken. Do not import another project's resources to resolve a name conflict.

From the repository root:

```sh
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars`: fill your subscription ID, tenant ID, unique name suffix and two different passwords. The blank values intentionally fail validation. Use 16–128 characters for both passwords; the administrator password needs uppercase, lowercase, a number and a symbol, and must not contain `currencyadmin`. Keep `deployment_stage = "setup"`, `min_replicas = 0` and `database_allowed_ips = []` initially. The setup client stays at one replica independently of the API setting.

`terraform.tfvars` is ignored by Git. Terraform state and saved plans can contain the passwords even though terminal output marks them sensitive. Keep those files private and retain state until cleanup finishes. There is no remote-state storage account in this first version.

Sign in to the same tenant and subscription you put in the file, replacing the placeholders:

```sh
az login --tenant "YOUR_TENANT_ID"
az account set --subscription "YOUR_SUBSCRIPTION_ID"
az account show --query '{name:name, subscription:id, tenant:tenantId}'
terraform init
terraform fmt -check
terraform validate
```

## 1. Create the setup environment

```sh
terraform plan -out=setup.tfplan
```

Read the plan before applying. Expect one resource group, a public-endpoint B1ms PostgreSQL server with **32768 MB (32 GiB)** storage, the database/TLS setting, Key Vault/secret, two vault-scoped role assignments, Log Analytics, the Container Apps environment and one temporary client app. The application has no ingress yet.

There is no custom VNet, subnet, private DNS zone, NAT Gateway, dedicated workload profile, or separately billed custom-VNet load balancer/public IP in this configuration. The PostgreSQL firewall initially has no allowed IPs.

After reviewing the scope and cost:

```sh
terraform apply setup.tfplan
terraform output container_app_outbound_ips
```

Wait for a successful apply, including the app's **Key Vault Secrets User** assignment. The Terraform account receives **Key Vault Secrets Officer** only on this vault so it can create and delete the secret.

## 2. Allow the setup app to reach PostgreSQL

Copy every address from `terraform output container_app_outbound_ips` into `database_allowed_ips` in your private values file. For example, the following documentation-only addresses show the format; replace them with your actual output:

```hcl
database_allowed_ips = ["192.0.2.10", "192.0.2.11"]
```

Keep `deployment_stage = "setup"`, then review and apply again:

```sh
terraform plan -out=access.tfplan
terraform apply access.tfplan
terraform output -raw database_setup_command
```

This adds one exact-address firewall rule per IP; it does not allow all Azure services or the whole internet. Azure can take up to five minutes to enforce firewall changes. These IP addresses are network access rules, not proof of the application's identity: database authentication is still required. Azure can change the app's outbound addresses; compare this output after changing the app or recreating its environment and update the allowlist if necessary.

## 3. Create the application database login

Run SQL from the temporary client through the allowed outbound addresses. Terraform has already created `currency_exchange`; SQL creates its login and grants. Flyway will create the cache tables when Java starts.

1. Open **ca-currency-api → Monitoring → Console** in the Azure portal. Choose the active setup revision and `/bin/sh`.
2. Paste the command printed by `database_setup_command`. At the password prompt, enter `database_admin_password` from your private values file.
3. Open [`scripts/init-azure-db.sql`](../scripts/init-azure-db.sql) locally and paste its contents into the `psql` session. Run this file once on the new database. The final row should show `currency_exchange` and three `t` values.
4. Enter the following command by itself, then wait for its prompts. Enter the exact `database_app_password` from your values file twice; the password is not echoed.

```text
\password currency_app
```

5. Enter `\q`, then connect again using the same host, database `currency_exchange`, user `currency_app`, TLS and `--password`. Enter the application password and verify:

```sql
SELECT current_user, current_database(),
       has_schema_privilege(current_user, 'public', 'USAGE') AS can_use_schema,
       has_schema_privilege(current_user, 'public', 'CREATE') AS can_create_tables;
```

Expect `currency_app`, `currency_exchange`, `t`, `t`. Enter `\q` when done. The temporary container sleeps for one hour and is restarted by Container Apps afterward; reconnect to the current replica if its console closes.

If interrupted, inspect which SQL statements succeeded before repeating them. An existing-role error does not mean the password and grants are complete. Never use the administrator login for the Java application.

## 4. Switch to the Java API

Change only this setting in `terraform.tfvars`:

```hcl
deployment_stage = "api"
```

Then review a new plan:

```sh
terraform plan -out=api.tfplan
terraform apply api.tfplan
```

This updates the existing Container App. It removes `sleep`, uses the Java image's entrypoint, adds the Key Vault reference, sets `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`, and enables HTTPS ingress and the three probes. The API gets the application password, not the administrator password. The setup stage must have completed first because a system-assigned identity does not exist before its app is created.

The default image is the exact public Linux AMD64 image behind `ghcr.io/israad-visuals/currency-api:portal-v1`, pinned by digest in `variables.tf`. Terraform does not build or upload it: Azure pulls that reference. To release different code later, build and push a new AMD64 image, set `container_image` in your local values file to its new tag or digest, and review another plan.

| Probe | Path | Delay | Interval | Timeout | Failures |
| --- | --- | --- | --- | --- | --- |
| Startup | `/actuator/health/liveness` | 10s | 10s | 3s | 30 |
| Liveness | `/actuator/health/liveness` | 10s | 10s | 3s | 3 |
| Readiness | `/actuator/health/readiness` | 5s | 10s | 5s | 3 |

All use HTTP on port 8080. Readiness requires one success and checks the database as well as the application.

## Verify the result

```sh
terraform output
curl --fail --silent --show-error "$(terraform output -raw readiness_url)"
curl --fail --silent --show-error "$(terraform output -raw conversion_url)"
terraform plan
```

Readiness should return `{"status":"UP"}`. Conversion should return a USD-to-CAD result with its rate and dates; do not expect a fixed exchange rate. The last plan should show no unexpected changes. Output URLs are addresses to test, not evidence that the API is healthy. The root `/` can return 404 because there is no homepage.

The API defaults to zero-to-one replicas. After idle it can scale to zero; the next request starts Java again and can take longer. The first HTTP request also performs the database readiness check. Keep the PostgreSQL server running if you want the demo available to recruiters. Do not configure a frequent external uptime ping: those requests can keep the app awake and consume its free compute allowance.

If startup fails, temporarily set `min_replicas = 1`, apply a reviewed plan and inspect the logs. Return it to zero after troubleshooting.

## Cost and portfolio scope

At the researched North Central US retail rates (October 31, 2026), PostgreSQL B1ms is US$0.017/hour and 32 GiB storage is US$3.68/month: about **US$16.09 per 730-hour month** before student benefits. Budget roughly **US$16–20/month** for very light demo usage with the Container Apps monthly free grant available. This is an estimate, not a spending limit; request volume, time awake, logs and transfer can increase it.

Container Apps includes 180,000 vCPU-seconds, 360,000 GiB-seconds and two million requests per subscription per month. At 0.5 vCPU / 1 GiB, the compute grant covers approximately 100 replica-hours per month if none is used elsewhere. Repeated visits, restarts and uptime checks use that allowance. Student PostgreSQL benefits may lower the database charge, but verify eligibility and remaining credit rather than assuming they apply. An Azure budget alert sends notifications; it does not stop resources automatically.

For evidence, record the reviewed plan, successful apply, database readiness, a conversion response, the managed identity's vault role, firewall rules, and the app scaling to zero. Explain the public-endpoint tradeoff and cold start honestly. Local validation and this source code do not prove an Azure deployment.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Resource-provider registration error | Register the named provider in this subscription, wait, then make a fresh plan. |
| Server or vault name unavailable | Choose a new suffix before the first deployment. Changing it after deployment can replace resources. |
| Key Vault 403 shortly after assignment | RBAC can take time to propagate. Confirm the vault-scoped role, wait, and run a fresh plan/apply. Do not broaden permissions or add a second identity. |
| API cannot retrieve its Key Vault secret | Confirm setup completed, the app identity is unchanged and its role assignment succeeded. Start fresh deployments in `setup`. |
| Database authentication failure | Compare the SQL role password with the Key Vault application password. Changing the Terraform password does not change an existing SQL role; update both deliberately. |
| Java exits or readiness fails | Check system/container logs, current outbound IPs/firewall rules, database grants and the three DB variables. Confirm no `sleep` command remains in the API revision. |
| Database connection times out | Compare `container_app_outbound_ips` with `database_allowed_ips`, apply any changes, and allow up to five minutes for firewall propagation. Do not enable access for all Azure services. |
| Public access is blocked | The environment must have public network access enabled, and the app must be in `api` stage with external ingress. |

When configuration changes, create a new saved plan. An older saved plan still contains its original inputs.

## Cleanup

This is a disposable learning environment. The provider deliberately purges its Key Vault, secrets and Log Analytics workspace on destroy. These deletions are permanent.

```sh
terraform plan -destroy -out=destroy.tfplan
terraform apply destroy.tfplan
```

Wait for completion. The environment has a 60-minute delete timeout to allow Azure to finish its managed-service cleanup. If destroy fails, keep the state, inspect the error and retry a reviewed destroy plan. Verify that the project group is gone and that the vault/workspace recycle bins have no project remnants. Keep the GHCR image and local learning evidence separately.

## Validation status

Locally checked with Terraform 1.16.6 and AzureRM 4.81.0: formatting, configuration validation and eight offline mocked-provider checks for setup, exact-IP firewall access, API transition, default scale-to-zero, an always-on override and invalid inputs. An isolated PostgreSQL 16.15 container also verified the SQL grants, application login, existing Flyway migration and cache read/write; application attempts to create roles or databases were denied. That test container was removed. The public image manifest was checked for Linux AMD64. These checks do not establish Azure quota, current permissions or a successful Terraform deployment. This configuration has not been applied to Azure.

References: [Container App resource](https://registry.terraform.io/providers/hashicorp/azurerm/4.81.0/docs/resources/container_app), [Container Apps Key Vault references](https://learn.microsoft.com/en-us/azure/container-apps/manage-secrets#reference-secret-from-key-vault), [PostgreSQL firewall rules](https://learn.microsoft.com/en-us/azure/postgresql/security/security-firewall-rules), [Container Apps billing](https://learn.microsoft.com/en-us/azure/container-apps/billing), [Container Apps networking](https://learn.microsoft.com/en-us/azure/container-apps/networking), [student benefits](https://azure.microsoft.com/en-us/free/students/), [Azure retail prices](https://learn.microsoft.com/en-us/rest/api/cost-management/retail-prices/azure-retail-prices), [Terraform sensitive data](https://developer.hashicorp.com/terraform/language/manage-sensitive-data).
