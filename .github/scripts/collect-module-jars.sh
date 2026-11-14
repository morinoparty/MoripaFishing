#!/usr/bin/env bash
# リリース用にモジュール jar を収集する。
# 新しいモジュールを追加してもワークフローを編集せずにリリースアセットへ自動的に増える。
#
# Usage: collect-module-jars.sh <src-root> <out-dir> <version>
#   e.g. collect-module-jars.sh ./integrations ./release-integrations 1.2.3
#
# 配布 jar は Gradle が <paper-plugin.yml の name>_<version>.jar として出力するため、名前は変えずに移す。
set -euo pipefail

src_root="$1"
out_dir="$2"
ver="$3"

mkdir -p "$out_dir"
for dir in "$src_root"/*/; do
  [ -d "$dir" ] || continue
  for jar in "$dir"build/libs/*_"$ver".jar; do
    [ -e "$jar" ] || continue
    mv "$jar" "$out_dir/"
  done
done
ls -1 "$out_dir"
