#!/usr/bin/env bash
# .agent/rules のルールを結合して AGENTS.md と CLAUDE.md を生成するスクリプト
# 生成物は .gitignore に含まれており、リポジトリには載せない
set -euo pipefail

cd "$(dirname "$0")/.."

RULES_DIR=".agent/rules"
OUTPUT_FILES=(AGENTS.md CLAUDE.md)

for output_file in "${OUTPUT_FILES[@]}"; do
  : > "$output_file"
  for file in "$RULES_DIR"/*.md; do
    cat "$file" >> "$output_file"
    printf '\n\n' >> "$output_file"
  done
done

echo "Generated ${OUTPUT_FILES[*]} from $(ls -1 "$RULES_DIR"/*.md | wc -l) rule files"
