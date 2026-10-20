data "aws_caller_identity" "current" {}

resource "aws_s3_bucket" "app_bucket" {
  bucket = var.s3_bucket_name

  tags = {
    Name        = var.s3_bucket_name
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}
