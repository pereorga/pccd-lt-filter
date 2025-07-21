#!/bin/bash
set -e

echo "Testing stdin functionality..."

echo "Testing basic stdin input"
cat testdata/input.txt | java -jar ../bin/lt-filter.jar > stdin-correct.txt 2> stdin-flagged.txt
if ! diff stdin-correct.txt testdata/correct.txt; then
    echo "DIFF FAILED for stdin correct sentences:"
    echo "=== EXPECTED ==="
    cat testdata/correct.txt
    echo "=== ACTUAL ==="
    cat stdin-correct.txt
    echo "=== END ==="
    exit 1
fi

if ! diff stdin-flagged.txt testdata/flagged.txt; then
    echo "DIFF FAILED for stdin flagged sentences:"
    echo "=== EXPECTED ==="
    cat testdata/flagged.txt
    echo "=== ACTUAL ==="
    cat stdin-flagged.txt
    echo "=== END ==="
    exit 1
fi

echo "Testing stdin with separate options"
cat testdata/input.txt | java -jar ../bin/lt-filter.jar --correct > stdin-correct-option.txt
cat testdata/input.txt | java -jar ../bin/lt-filter.jar --flagged > stdin-flagged-option.txt

if ! diff stdin-correct-option.txt testdata/correct.txt; then
    echo "DIFF FAILED for stdin --correct option:"
    echo "=== EXPECTED ==="
    cat testdata/correct.txt
    echo "=== ACTUAL ==="
    cat stdin-correct-option.txt
    echo "=== END ==="
    exit 1
fi

if ! diff stdin-flagged-option.txt testdata/flagged.txt; then
    echo "DIFF FAILED for stdin --flagged option:"
    echo "=== EXPECTED ==="
    cat testdata/flagged.txt
    echo "=== ACTUAL ==="
    cat stdin-flagged-option.txt
    echo "=== END ==="
    exit 1
fi

echo "✅ All stdin tests passed!"