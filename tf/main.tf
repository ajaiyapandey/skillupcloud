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

resource "aws_s3_bucket_versioning" "app_bucket_versioning" {
  bucket = aws_s3_bucket.app_bucket.id

  versioning_configuration {
    status = "Enabled"
  }

}

resource "aws_s3_bucket_policy" "app_bucket_policy" {
  bucket = aws_s3_bucket.app_bucket.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowIAMRoleToListBucket"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::521024927848:role/s3_manager_role"
        }
        Action   = "s3:ListBucket"
        Resource = "arn:aws:s3:::${var.s3_bucket_name}"
      },
      {
        Sid    = "AllowIAMRoleObjectAccess"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::521024927848:role/s3_manager_role"
        }
        Action = [
          "s3:GetObject",
          "s3:DeleteObject"
        ]
        Resource = "arn:aws:s3:::${var.s3_bucket_name}/*"
      }
    ]
  })
}

locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}