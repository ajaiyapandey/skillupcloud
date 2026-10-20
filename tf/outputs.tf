output "account_id" {
  description = "Current AWS account ID"
  value       = data.aws_caller_identity.current.account_id
}

output "arn" {
  description = "Current AWS caller ARN"
  value       = data.aws_caller_identity.current.arn
}

output "user_id" {
  description = "Current AWS user ID"
  value       = data.aws_caller_identity.current.user_id
}

output "region" {
  description = "Configured AWS region"
  value       = var.aws_region
}
