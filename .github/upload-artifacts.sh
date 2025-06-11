#!/bin/bash

set -e
# 基本的なディレクトリ設定
PROJECT_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
UPLOAD_DIR="${PROJECT_ROOT}/upload"

# アップロード用ディレクトリを作成
mkdir -p "${UPLOAD_DIR}"

# 1. JARファイルのコピー
mkdir -p "${UPLOAD_DIR}/jars"
cp "${PROJECT_ROOT}/bukkit/build/libs"/*.jar "${UPLOAD_DIR}/jars/MoripaFishing-bukkit-${COMMIT_SHORT_SHA}.jar"
cp "${PROJECT_ROOT}/api/build/libs"/*.jar "${UPLOAD_DIR}/jars/MoripaFishing-api-${COMMIT_SHORT_SHA}.jar"

# 2. テスト結果のコピー
mkdir -p "${UPLOAD_DIR}/junit"
cp -r "${PROJECT_ROOT}/bukkit/build/reports/tests/test"/* "${UPLOAD_DIR}/junit/"

mkdir -p "${UPLOAD_DIR}/detekt"
cp -r "${PROJECT_ROOT}/build/reports/detekt"/* "${UPLOAD_DIR}/detekt/"

aws s3 cp "${UPLOAD_DIR}" --endpoint-url "${S3_ENDPOINT}" "s3://${S3_UPLOAD_BUCKET}/${COMMIT_SHORT_SHA}" --recursive