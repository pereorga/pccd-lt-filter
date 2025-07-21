#!/bin/bash
set -e

echo "Testing Node.js wrapper functionality..."

echo "Testing help option via Node.js wrapper"
if ! node ../bin/lt-filter.js --help 2>&1 | grep -q "lt-filter - LanguageTool"; then
    echo "FAILED: Node.js wrapper help doesn't show expected text"
    exit 1
fi

echo "Testing version option via Node.js wrapper"
VERSION_OUTPUT=$(node ../bin/lt-filter.js -v)
if ! echo "$VERSION_OUTPUT" | grep -q "lt-filter version"; then
    echo "FAILED: Node.js wrapper version doesn't show expected format"
    echo "Output was: $VERSION_OUTPUT"
    exit 1
fi

echo "✅ Node.js wrapper tests passed!"