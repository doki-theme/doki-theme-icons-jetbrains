#!/bin/bash

# Pre-Build
git fetch
git checkout main
git pull origin main
./gradlew markdownToHtml

# Build
./gradlew clean buildPlugin
