variable "aws_region" {
  description = "The AWS region to deploy resources in."
  type        = string
  default     = "ap-northeast-1"
}

variable "iam_role_arn" {
  description = "The ARN of the IAM role for the Lambda functions."
  type        = string
}
