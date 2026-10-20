variable "aws_region" {
  description = "AWS region for deployment"
  type        = string
  default     = "us-east-1"
}

variable "aws_account_id" {
  description = "Allowed AWS account ID for deployment"
  type        = string
  default     = "521024927848"
}

variable "project_name" {
  description = "Project name used for tagging and naming"
  type        = string
  default     = "skillupcloud"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "dev"
}

variable "s3_bucket_name" {
  description = "Name of the S3 bucket to provision"
  type        = string
  default     = "awsupskill206"
}
