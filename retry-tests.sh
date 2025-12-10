#!/bin/bash

MAX_RETRIES=8
attempt=1

while [ $attempt -le $MAX_RETRIES ]; do
  echo "🔁 Running test suite - Attempt $attempt of $MAX_RETRIES..."
  mvn clean test
  exit_code=$?

  if [ $exit_code -eq 0 ]; then
    echo "✅ Test suite passed on attempt $attempt."
    exit 0  # ✅ If success, exit immediately — no retry.
  else
    echo "❌ Test suite failed on attempt $attempt."
    ((attempt++))
  fi
done

echo "❗ Maximum attempts ($MAX_RETRIES) reached. Suite did not pass."
exit 1
