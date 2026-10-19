#!/bin/sh

GRADLE_VERSION=8.8
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
GRADLE_APP_HOME="$(cd "$(dirname "$0")" && pwd)"

if [ ! -f "$GRADLE_APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Gradle wrapper not found. Please run: gradle wrapper"
    exit 1
fi

exec java -jar "$GRADLE_APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
