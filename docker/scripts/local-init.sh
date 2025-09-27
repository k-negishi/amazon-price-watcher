#!/usr/bin/env bash
set -euo pipefail

ENDPOINT_URL=${ENDPOINT_URL:-http://localhost:4566}
TABLE_NAME=${PRICE_TABLE:-PriceHistory}

aws dynamodb describe-table \
  --endpoint-url "${ENDPOINT_URL}" \
  --table-name "${TABLE_NAME}" >/dev/null 2>&1 && {
  echo "Table ${TABLE_NAME} already exists"
  exit 0
}

echo "Creating table ${TABLE_NAME}" 
aws dynamodb create-table \
  --endpoint-url "${ENDPOINT_URL}" \
  --table-name "${TABLE_NAME}" \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

echo "Enabling TTL on ${TABLE_NAME}" 
aws dynamodb update-time-to-live \
  --endpoint-url "${ENDPOINT_URL}" \
  --table-name "${TABLE_NAME}" \
  --time-to-live-specification Enabled=true,AttributeName=expireAt
