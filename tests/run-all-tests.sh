#!/bin/bash
set -e

echo "🚀 Running all lt-filter tests..."

# Ensure JAR is built
if [ ! -f "../bin/lt-filter.jar" ]; then
    echo "Building JAR first..."
    cd .. && mvn package && cd tests
fi

# Run individual test scripts
echo ""
./test-basic.sh
echo ""
./test-stdin.sh
echo ""
./test-disable-rules.sh
echo ""
./test-rule-names.sh
echo ""
./test-node-wrapper.sh
echo ""
./test-server.sh

# Clean up test files
echo ""
echo "Cleaning up test files..."
rm -f correct.txt flagged.txt correct-option.txt flagged-option.txt
rm -f stdin-*.txt disable-rules-*.txt rule-names-*.txt

echo ""
echo "🎉 All tests passed successfully!"