#!/bin/bash
# Usage: ./scripts/bump-version.sh patch|minor|major
# Updates build.gradle.kts and update.json

set -e

TYPE=${1:-patch}

CURRENT_VERSION=$(grep "versionName =" app/build.gradle.kts | head -1 | awk '{print $3}' | tr -d '"')
CURRENT_CODE=$(grep "versionCode =" app/build.gradle.kts | head -1 | awk '{print $3}')

IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

case $TYPE in
  major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
  minor) MINOR=$((MINOR + 1)); PATCH=0 ;;
  patch) PATCH=$((PATCH + 1)) ;;
  *) echo "Invalid bump type: patch|minor|major"; exit 1 ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
NEW_CODE=$((CURRENT_CODE + 1))

echo "Bumping QuickDash version: $CURRENT_VERSION ($CURRENT_CODE) -> $NEW_VERSION ($NEW_CODE)"

sed -i.bak "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" app/build.gradle.kts && rm -f app/build.gradle.kts.bak
sed -i.bak "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts && rm -f app/build.gradle.kts.bak

if [ -f update.json ]; then
  sed -i.bak "s/\"version_code\": $CURRENT_CODE/\"version_code\": $NEW_CODE/" update.json && rm -f update.json.bak
  sed -i.bak "s/\"latest_version\": \"$CURRENT_VERSION\"/\"latest_version\": \"$NEW_VERSION\"/" update.json && rm -f update.json.bak
fi

if [ -f website/public/api/v1/update.json ]; then
  sed -i.bak "s/\"version_code\": $CURRENT_CODE/\"version_code\": $NEW_CODE/" website/public/api/v1/update.json && rm -f website/public/api/v1/update.json.bak
  sed -i.bak "s/\"latest_version\": \"$CURRENT_VERSION\"/\"latest_version\": \"$NEW_VERSION\"/" website/public/api/v1/update.json && rm -f website/public/api/v1/update.json.bak
fi

echo "✅ Successfully bumped version to $NEW_VERSION (build $NEW_CODE)"
