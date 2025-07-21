#!/bin/bash
set -e

echo "Testing --disable-rules functionality..."

echo "Testing --disable-rules (append mode)"
java -jar ../bin/lt-filter.jar --disable-rules COMMA_PARENTHESIS_WHITESPACE testdata/input.txt > disable-rules-test.txt 2>&1
echo "Comparing disable-rules output with expected..."
if ! diff disable-rules-test.txt testdata/disable-rules-expected.txt; then
    echo "DIFF FAILED for --disable-rules:"
    echo "=== EXPECTED ==="
    cat testdata/disable-rules-expected.txt
    echo "=== ACTUAL ==="
    cat disable-rules-test.txt
    echo "=== END ==="
    exit 1
fi

echo "Testing --disable-rules-replace (replace mode)"
java -jar ../bin/lt-filter.jar --disable-rules-replace SER_ESSER testdata/input.txt > disable-rules-replace-test.txt 2>&1
echo "Comparing disable-rules-replace output with expected..."
if ! diff disable-rules-replace-test.txt testdata/disable-rules-replace-expected.txt; then
    echo "DIFF FAILED for --disable-rules-replace:"
    echo "=== EXPECTED ==="
    cat testdata/disable-rules-replace-expected.txt
    echo "=== ACTUAL ==="
    cat disable-rules-replace-test.txt
    echo "=== END ==="
    exit 1
fi

echo "✅ All disable-rules tests passed!"