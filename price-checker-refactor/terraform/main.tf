resource "aws_ecr_repository" "fetch_price" {
  name                 = "fetch-price"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_repository" "get_price_history" {
  name                 = "get-price-history"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_repository" "compare_price_and_notify" {
  name                 = "compare-price-and-notify"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "this" {
  for_each = {
    fetch_price              = aws_ecr_repository.fetch_price.name
    get_price_history        = aws_ecr_repository.get_price_history.name
    compare_price_and_notify = aws_ecr_repository.compare_price_and_notify.name
  }

  repository = each.value

  policy = jsonencode({
    rules = [{
      rulePriority = 1,
      description  = "Keep last 5 images",
      selection = {
        tagStatus     = "any",
        countType     = "imageCountMoreThan",
        countNumber   = 5
      },
      action = {
        type = "expire"
      }
    }]
  })
}

resource "aws_dynamodb_table" "price_history" {
  name           = "PriceHistory"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "url"
  range_key      = "date"

  attribute {
    name = "url"
    type = "S"
  }

  attribute {
    name = "date"
    type = "S"
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  tags = {
    Name = "PriceHistory"
  }
}

resource "aws_lambda_function" "fetch_price" {
  function_name = "fetch-price"
  role          = var.iam_role_arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.fetch_price.repository_url}:latest"

  environment {
    variables = {
      PRICE_HISTORY_TABLE_NAME = aws_dynamodb_table.price_history.name
      AWS_REGION                 = var.aws_region
    }
  }

  timeout = 60
}

resource "aws_lambda_function" "get_price_history" {
  function_name = "get-price-history"
  role          = var.iam_role_arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.get_price_history.repository_url}:latest"

  environment {
    variables = {
      PRICE_HISTORY_TABLE_NAME = aws_dynamodb_table.price_history.name
      AWS_REGION                 = var.aws_region
    }
  }

  timeout = 60
}

resource "aws_sfn_state_machine" "price_checker_state_machine" {
  name     = "PriceCheckerStateMachine"
  role_arn = var.iam_role_arn # Assuming the same role for simplicity

  definition = jsonencode({
    Comment = "A state machine to check Amazon prices."
    StartAt = "FetchPriceAndHistory"
    States = {
      FetchPriceAndHistory = {
        Type = "Parallel"
        Branches = [
          {
            StartAt = "FetchPrice"
            States = {
              FetchPrice = {
                Type      = "Task"
                Resource  = aws_lambda_function.fetch_price.arn
                End       = true
              }
            }
          },
          {
            StartAt = "GetPriceHistory"
            States = {
              GetPriceHistory = {
                Type      = "Task"
                Resource  = aws_lambda_function.get_price_history.arn
                End       = true
              }
            }
          }
        ]
        Next = "ComparePrice"
      },
      ComparePrice = {
        Type = "Task"
        # Placeholder for the final Lambda function
        Resource = "arn:aws:lambda:ap-northeast-1:123456789012:function:compare-price"
        End      = true
      }
    }
  })
}
