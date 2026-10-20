# AWS S3 Setup Commands

This file documents the commands used to configure AWS credentials, validate the Terraform setup, and provision the S3 bucket.

## 1. Check Terraform installation

```bash
terraform
terraform -version
```

## 2. Check AWS identity and account

```bash
aws sts get-caller-identity
```

## 3. Configure AWS CLI credentials

```bash
aws configure
aws --version
```

## 4. Change to the Terraform directory

```bash
cd tf
```

## 5. Initialize Terraform

```bash
terraform init
```

## 6. Format Terraform files

```bash
terraform fmt
```

## 7. Terraform validation and planning

```bash
terraform validate
terraform plan
```

## 8. Apply Terraform

```bash
terraform apply
```

## 9. After creating the S3 bucket

```bash
terraform plan
terraform apply
```

## 10. Useful verification commands

```bash
aws s3 ls --region us-east-1 | findstr awsupskill206
aws s3api head-bucket --bucket awsupskill206 --region us-east-1
```

## Bucket details used

- Region: `us-east-1`
- Account ID: `521024927848`
- Bucket name: `awsupskill206`

