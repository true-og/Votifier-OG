#!/usr/bin/env bash

VERSION="$1"

# Ensure the target directory exists
mkdir -p build/libs

# Copy and rename the jar, using $VERSION in the final filename
cp bukkit/build/libs/nuvotifier-bukkit-*-SNAPSHOT.jar "build/libs/Votifier-OG-${VERSION}.jar"
