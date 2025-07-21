#!/bin/bash
set -e

echo "Testing server functionality..."

echo "Starting server in background..."
java -jar ../bin/lt-filter.jar --port 8080 &
SERVER_PID=$!

# Function to cleanup server on exit
cleanup() {
    if kill -0 $SERVER_PID 2>/dev/null; then
        echo "Stopping server..."
        kill $SERVER_PID
        wait $SERVER_PID 2>/dev/null || true
    fi
}
trap cleanup EXIT

# Wait for server to start
sleep 5

echo "Testing server with curl..."
RESPONSE=$(curl -s -X POST -d "Això és una prova.
A acaba-set" "http://localhost:8080/?rule-names=true")

echo "Server response: $RESPONSE"

# Check if response contains expected content
if ! echo "$RESPONSE" | grep -q '"sentence": "A acaba-set"'; then
    echo "FAILED: Response missing expected flagged sentence"
    echo "Response was: $RESPONSE"
    exit 1
fi

if ! echo "$RESPONSE" | grep -q '"rules": \["PREP_VERB_CONJUGAT"\]'; then
    echo "FAILED: Response missing expected rule name"
    echo "Response was: $RESPONSE"
    exit 1
fi

if ! echo "$RESPONSE" | grep -q '"Això és una prova."'; then
    echo "FAILED: Response missing expected correct sentence"
    echo "Response was: $RESPONSE"
    exit 1
fi

echo "✅ Server test passed!"