# Policy for Spring Application
path "secret/data/spring-cloud-integration" {
  capabilities = ["read"]
}

path "secret/data/spring-cloud-integration/*" {
  capabilities = ["read"]
}

path "auth/token/lookup-self" {
  capabilities = ["read"]
}