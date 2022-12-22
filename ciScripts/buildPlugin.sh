#!/bin/bash

# Pre-Build
git fetch
git checkout main
git pull origin main

# Build
./gradlew clean buildPlugin
