#!/bin/bash
set -e

echo "Testing --rule-names functionality..."

echo "Testing --rule-names with flagged sentences"
java -jar ../bin/lt-filter.jar --flagged --rule-names testdata/flagged.txt > rule-names-test.txt
echo "Comparing rule-names output with expected..."
if ! diff rule-names-test.txt testdata/rule-names-expected.txt; then
    echo "DIFF FAILED for --rule-names:"
    echo "=== EXPECTED ==="
    cat testdata/rule-names-expected.txt
    echo "=== ACTUAL ==="
    cat rule-names-test.txt
    echo "=== END ==="
    exit 1
fi

echo "Testing --rule-names help shows the option"
if ! java -jar ../bin/lt-filter.jar --help 2>&1 | grep -q "rule-names"; then
    echo "FAILED: --rule-names option not found in help text"
    exit 1
fi

echo "Testing --rule-names via Node.js wrapper"
node ../bin/lt-filter.js --flagged --rule-names testdata/flagged.txt > rule-names-node-test.txt
echo "Comparing Node.js wrapper rule-names output with expected..."
if ! diff rule-names-node-test.txt testdata/rule-names-expected.txt; then
    echo "DIFF FAILED for Node.js wrapper --rule-names:"
    echo "=== EXPECTED ==="
    cat testdata/rule-names-expected.txt
    echo "=== ACTUAL ==="
    cat rule-names-node-test.txt
    echo "=== END ==="
    exit 1
fi

echo "✅ All rule-names tests passed!"