#!/usr/bin/env bash
# 递归查找 L*.java 学习课，javac 后 java 运行 main（要求类含 public static void main）。
# 用法:
#   ./scripts/verify_modules.sh 00_java_basics
#   ./scripts/verify_modules.sh 00_java_basics/00_syntax
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# 要求本机 javac 主版本 >= 25（与仓库 JDK 25 教学目标一致）
JAVA_VER="$(javac -version 2>&1 | sed -n 's/.* \([0-9][0-9]*\).*/\1/p' | head -1)"
if [[ -z "$JAVA_VER" ]] || [[ "$JAVA_VER" -lt 25 ]]; then
  echo "❌ 需要 JDK 25 或更新，当前 javac: $(javac -version 2>&1)"
  exit 1
fi
echo "javac $(javac -version 2>&1) （目标 release 25+）"

if [[ $# -lt 1 ]]; then
  echo "用法: $0 <目录...>"
  exit 1
fi

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

total=0
passed=0
failed=0

run_one() {
  local file="$1"
  local dir base class
  dir="$(dirname "$file")"
  base="$(basename "$file" .java)"
  class="$base"
  total=$((total + 1))
  echo "—— 编译运行: $file"
  if ! javac -encoding UTF-8 -d "$TMP" "$file"; then
    echo "❌ 编译失败: $file"
    failed=$((failed + 1))
    return
  fi
  # 无 package 的课：直接 java L00_Xxx；有 package 时需从类文件推断（入门课默认无 package）
  if ! (cd "$TMP" && java -cp . "$class"); then
    echo "❌ 运行失败: $file"
    failed=$((failed + 1))
    return
  fi
  passed=$((passed + 1))
}

for target in "$@"; do
  abs="$ROOT/$target"
  if [[ ! -e "$abs" ]]; then
    echo "❌ 不存在: $target"
    failed=$((failed + 1))
    continue
  fi
  echo ""
  echo "===== 验证目录: $target ====="
  while IFS= read -r -d '' file; do
    run_one "$file"
  done < <(find "$abs" -type f -name 'L*.java' -print0 | sort -z)
done

echo ""
echo "全部汇总: 通过 $passed / $total ，失败 $failed"
if [[ "$failed" -gt 0 ]] || [[ "$total" -eq 0 ]]; then
  exit 1
fi
