output "ecr_repository_urls" {
  description = "The URLs of the ECR repositories"
  value = {
    fetch_price              = aws_ecr_repository.fetch_price.repository_url
    get_price_history        = aws_ecr_repository.get_price_history.repository_url
    compare_price_and_notify = aws_ecr_repository.compare_price_and_notify.repository_url
  }
}

output "dynamodb_table_arn" {
  description = "The ARN of the DynamoDB table"
  value       = aws_dynamodb_table.price_history.arn
}

output "step_functions_state_machine_arn" {
  description = "The ARN of the Step Functions state machine"
  value       = aws_sfn_state_machine.price_checker_state_machine.id
}
