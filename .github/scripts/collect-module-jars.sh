#!/usr/bin/env bash
# リリース用にモジュール jar を収集する。
# 新しいモジュールを追加してもワークフローを編集せずにリリースアセットへ自動的に増える。
# -api の SPI 専用モジュールは配布物ではないため除外する。
#
# Usage: collect-module-jars.sh <src-root> <out-dir> <version>
#   e.g. collect-module-jars.sh ./integrations ./release-integrations 1.2.3
set -euo pipefail

src_root="$1"
out_dir="$2"
ver="$3"

mkdir -p "$out_dir"
for dir in "$src_root"/*/; do
  [ -d "$dir" ] || continue
  base="$(basename "$dir")"
  case "$base" in
    *-api) continue ;;
  esac
  for jar in "$dir"build/libs/*-"$ver".jar; do
    [ -e "$jar" ] || continue
    mv "$jar" "$out_dir/MoripaFishing-$(basename "$jar")"
  done
done
ls -1 "$out_dir"
