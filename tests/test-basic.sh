#!/bin/bash
set -e

echo "Testing basic functionality..."

echo "Running basic JAR test with sample input"
java -jar ../bin/lt-filter.jar testdata/input.txt > correct.txt 2> flagged.txt
if ! diff correct.txt testdata/correct.txt; then
    echo "DIFF FAILED for basic correct sentences:"
    echo "=== EXPECTED ==="
    cat testdata/correct.txt
    echo "=== ACTUAL ==="
    cat correct.txt
    echo "=== END ==="
    exit 1
fi

if ! diff flagged.txt testdata/flagged.txt; then
    echo "DIFF FAILED for basic flagged sentences:"
    echo "=== EXPECTED ==="
    cat testdata/flagged.txt
    echo "=== ACTUAL ==="
    cat flagged.txt
    echo "=== END ==="
    exit 1
fi

echo "Testing separate options (--correct and --flagged)"
java -jar ../bin/lt-filter.jar --correct testdata/input.txt > correct-option.txt
java -jar ../bin/lt-filter.jar --flagged testdata/input.txt > flagged-option.txt

if ! diff correct-option.txt testdata/correct.txt; then
    echo "DIFF FAILED for --correct option:"
    echo "=== EXPECTED ==="
    cat testdata/correct.txt
    echo "=== ACTUAL ==="
    cat correct-option.txt
    echo "=== END ==="
    exit 1
fi

if ! diff flagged-option.txt testdata/flagged.txt; then
    echo "DIFF FAILED for --flagged option:"
    echo "=== EXPECTED ==="
    cat testdata/flagged.txt
    echo "=== ACTUAL ==="
    cat flagged-option.txt
    echo "=== END ==="
    exit 1
fi

echo "✅ All basic tests passed!"