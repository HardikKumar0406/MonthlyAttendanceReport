#!/bin/bash

MAX_RETRIES=10
attempt=1

while [ $attempt -le $MAX_RETRIES ]; do
  echo "======================================================="
  echo "🔁 Running Test Suite — Attempt $attempt of $MAX_RETRIES"
  echo "======================================================="

  mvn clean test
  exit_code=$?

  if [ $exit_code -eq 0 ]; then
    echo "✅ Test suite passed successfully on attempt $attempt."
    exit 0
  else
    echo "❌ Attempt $attempt failed."

    if [ $attempt -lt $MAX_RETRIES ]; then
      echo "🔄 Retrying in 3 seconds..."
      sleep 3
    fi

    attempt=$((attempt + 1))
  fi
done

echo "❗ All $MAX_RETRIES attempts failed. Exiting."
exit 1
