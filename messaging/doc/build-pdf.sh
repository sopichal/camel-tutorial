#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIAGRAMS_DIR="$SCRIPT_DIR/diagrams"
SLIDES="$SCRIPT_DIR/messaging-patterns-marp.md"
OUTPUT="$SCRIPT_DIR/messaging-patterns.pdf"

# ---------------------------------------------------------------------------
# 1. Generate PNG images from Mermaid source files
# ---------------------------------------------------------------------------
echo "→ Generating Mermaid diagrams..."

npx --yes @mermaid-js/mermaid-cli \
  -i "$DIAGRAMS_DIR/point-to-point.mmd" \
  -o "$DIAGRAMS_DIR/point-to-point.png" \
  --width 1400 --height 600 \
  --backgroundColor white

npx @mermaid-js/mermaid-cli \
  -i "$DIAGRAMS_DIR/pub-sub.mmd" \
  -o "$DIAGRAMS_DIR/pub-sub.png" \
  --width 1400 --height 600 \
  --backgroundColor white

npx @mermaid-js/mermaid-cli \
  -i "$DIAGRAMS_DIR/request-reply.mmd" \
  -o "$DIAGRAMS_DIR/request-reply.png" \
  --width 1400 --height 700 \
  --backgroundColor white

echo "✓ Diagrams generated in $DIAGRAMS_DIR"

# ---------------------------------------------------------------------------
# 2. Build PDF slide deck from Marp Markdown
# ---------------------------------------------------------------------------
echo "→ Building PDF with Marp..."

npx --yes @marp-team/marp-cli \
  "$SLIDES" \
  --pdf \
  --allow-local-files \
  -o "$OUTPUT"

echo "✓ Slide deck: $OUTPUT"
