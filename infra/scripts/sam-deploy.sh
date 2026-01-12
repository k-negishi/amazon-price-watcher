#!/usr/bin/env bash

set -euo pipefail

REPO_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
SAM_CONFIG_FILE=${SAM_CONFIG_FILE:-}
SAM_CONFIG_ENV=${SAM_CONFIG_ENV:-default}
BUILD_ROOT=${BUILD_ROOT:-"$(mktemp -d "${TMPDIR:-/tmp}/sam-build-XXXXXX")"}

AWS_REGION=${AWS_REGION:-ap-northeast-1}
STACK_NAME=${STACK_NAME:-amazon-price-watcher}
SSM_UPLOAD_SCRIPT="${REPO_ROOT}/infra/scripts/ssm-upload.sh"

if [[ -x "${SSM_UPLOAD_SCRIPT}" ]]; then
  "${SSM_UPLOAD_SCRIPT}"
else
  echo "ERROR: ${SSM_UPLOAD_SCRIPT} not found or not executable"
  exit 1
fi

CONFIG_ARGS=()
if [[ -n "${SAM_CONFIG_FILE}" && -f "${SAM_CONFIG_FILE}" ]]; then
  CONFIG_ARGS+=(--config-file "${SAM_CONFIG_FILE}" --config-env "${SAM_CONFIG_ENV}")
else
  echo ">>> samconfig.toml not found. Using CLI arguments only."
fi

echo ">>> sam build"
sam build \
  --build-dir "${BUILD_ROOT}/.aws-sam/build" \
  --cache-dir "${BUILD_ROOT}/.aws-sam/cache" \
  "${CONFIG_ARGS[@]}" \
  --use-container \
  --template-file "${REPO_ROOT}/infra/sam/template.yaml"

echo ">>> copying build artifacts into repo .aws-sam directory"
rm -rf "${REPO_ROOT}/.aws-sam"
mkdir -p "${REPO_ROOT}/.aws-sam"
cp -R "${BUILD_ROOT}/.aws-sam/." "${REPO_ROOT}/.aws-sam/"

echo ">>> sam deploy"
sam deploy \
  "${CONFIG_ARGS[@]}" \
  --region "${AWS_REGION}" \
  --stack-name "${STACK_NAME}" \
  --template-file "${REPO_ROOT}/infra/sam/template.yaml"
