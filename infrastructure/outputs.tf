output "vaultUri" {
  value = "${module.definition-store-vault.key_vault_uri}"
}

output "vaultName" {
  value = "${local.vaultName}"
}

output "idam_url" {
  value = "${var.idam_api_url}"
}

output "s2s_url" {
  value = "${local.s2s_url}"
}
