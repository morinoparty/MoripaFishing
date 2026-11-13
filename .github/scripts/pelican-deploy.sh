#!/usr/bin/env bash
# Pelican Panel の client API で本番サーバーのプラグインを入れ替え、再起動する。
#
# 前提の環境変数:
#   PANEL_URL      - パネルの URL (例: https://panel.example.com)
#   SERVER_UUID    - 対象サーバーの UUID
#   API_KEY        - client API キー (pacc_...)
#   PLUGIN_VERSION - デプロイするバージョン
#
# アップロード対象 (いずれも <paper-plugin.yml の name>_<version>.jar):
#   ./artifacts/MoripaFishing_<version>.jar   (本体)
#   ./artifacts/addons/*.jar                  (addon)
#   ./artifacts/integrations/*.jar            (integration)
set -euo pipefail

api() {
  local method="$1" path="$2"
  shift 2
  curl -fsS -X "$method" "$PANEL_URL/api/client/servers/$SERVER_UUID$path" \
    -H "Authorization: Bearer $API_KEY" -H "Accept: application/json" "$@"
}

jars=("./artifacts/MoripaFishing_${PLUGIN_VERSION}.jar")
for jar in ./artifacts/addons/*.jar ./artifacts/integrations/*.jar; do
  [ -e "$jar" ] || continue
  jars+=("$jar")
done

# 旧バージョンの jar を残すと二重ロードになるため、先に削除する。
# 削除パターンはアップロード対象から動的に作り、無関係なプラグインは触らない。
# jar 名から _<version>.jar を除いたプラグイン名ごとに、^<name>_.*\.jar$ を作る。
# プラグイン名は英数字と - だけなので、正規表現としてそのまま埋め込める。
patterns=()
for jar in "${jars[@]}"; do
  base=$(basename "$jar")
  patterns+=("^${base%_"${PLUGIN_VERSION}".jar}_.*\\.jar\$")
done
pattern=$(IFS='|'; echo "${patterns[*]}")

old=$(api GET "/files/list?directory=%2Fplugins" \
  | jq -r --arg re "$pattern" '.data[] | select(.attributes.is_file) | .attributes.name | select(test($re))')
if [ -n "$old" ]; then
  files=$(printf '%s\n' "$old" | jq -R . | jq -cs .)
  api POST "/files/delete" -H "Content-Type: application/json" \
    -d "{\"root\":\"/plugins\",\"files\":$files}"
  echo "Removed old JARs:"
  echo "$old"
else
  echo "No old plugin JARs found."
fi

form_args=()
for jar in "${jars[@]}"; do
  form_args+=(-F "files=@$jar")
done
upload_url=$(api GET "/files/upload" | jq -r '.attributes.url')
curl -fsS -X POST "$upload_url&directory=%2Fplugins" "${form_args[@]}"
echo "Uploaded ${#jars[@]} JAR(s): ${jars[*]}"

api POST "/power" -H "Content-Type: application/json" -d '{"signal":"restart"}'
echo "Restart signal sent."
