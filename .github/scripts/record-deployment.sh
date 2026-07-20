#!/usr/bin/env bash
# デプロイ済み jar を GitHub Linked Artifacts の deployment record として登録する。
# 環境名などのラベルのみを送り、サーバーのアドレスや endpoint は一切送らない。
#
# 前提の環境変数:
#   PLUGIN_VERSION          - デプロイしたバージョン
#   ENVIRONMENT_NAME        - 表示する環境名 (例: Development Server)
#   GH_TOKEN                - artifact-metadata: write 権限を持つトークン
#   GITHUB_REPOSITORY_OWNER - org 名 (Actions のデフォルト環境変数)
set -euo pipefail

record() {
  local jar="$1"
  local base name digest
  base=$(basename "$jar" .jar)
  name="${base%-${PLUGIN_VERSION}}"
  name="${name%_${PLUGIN_VERSION}}"
  digest="sha256:$(sha256sum "$jar" | awk '{print $1}')"
  gh api --method POST \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "/orgs/${GITHUB_REPOSITORY_OWNER}/artifacts/metadata/deployment-record" \
    -f name="$name" \
    -f digest="$digest" \
    -f version="$PLUGIN_VERSION" \
    -f status="deployed" \
    -f logical_environment="$ENVIRONMENT_NAME" \
    -f deployment_name="$name" \
    -F return_records=false
  echo "Recorded deployment: $name ($digest)"
}

record "./artifacts/MoripaFishing_${PLUGIN_VERSION}.jar"
for jar in ./artifacts/addons/*.jar ./artifacts/integrations/*.jar; do
  [ -e "$jar" ] || continue
  record "$jar"
done
