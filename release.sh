#!/bin/bash
set -e

cd "$(dirname "$0")"

# Extract versions
POM_VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
PKG_VERSION=$(node -p "require('./package.json').version")
LOCK_VERSION=$(node -p "require('./package-lock.json').version")

echo "Versions found:"
echo "  pom.xml:          $POM_VERSION"
echo "  package.json:     $PKG_VERSION"
echo "  package-lock.json: $LOCK_VERSION"
echo ""

# 1. Sync versions across files (use the greatest)
VERSION=$(printf '%s\n' "$POM_VERSION" "$PKG_VERSION" "$LOCK_VERSION" | sort -V | tail -1)

update_pom_version() {
    local old=$1 new=$2
    MATCH_COUNT=$(grep -c "<version>${old}</version>" pom.xml)
    if [ "$MATCH_COUNT" -ne 1 ]; then
        echo "Error: expected 1 occurrence of <version>${old}</version> in pom.xml, found $MATCH_COUNT"
        exit 1
    fi
    sed -i '' "s/<version>${old}<\/version>/<version>${new}<\/version>/" pom.xml
}

if [ "$POM_VERSION" != "$VERSION" ] || [ "$PKG_VERSION" != "$VERSION" ] || [ "$LOCK_VERSION" != "$VERSION" ]; then
    echo "Version mismatch detected. Syncing all files to $VERSION..."
    if [ "$POM_VERSION" != "$VERSION" ]; then
        update_pom_version "$POM_VERSION" "$VERSION"
    fi
    if [ "$PKG_VERSION" != "$VERSION" ] || [ "$LOCK_VERSION" != "$VERSION" ]; then
        npm version "$VERSION" --no-git-tag-version
    fi
    POM_VERSION="$VERSION"
    echo ""
fi

echo "Version: $POM_VERSION"
echo ""

# 2. Check LanguageTool minor version sync
LT_VERSION=$(grep -A1 'languagetool-core' pom.xml | grep '<version>' | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
LT_MINOR=$(echo "$LT_VERSION" | cut -d. -f2)
PROJECT_MAJOR=$(echo "$POM_VERSION" | cut -d. -f1)
PROJECT_MINOR=$(echo "$POM_VERSION" | cut -d. -f2)

echo "LanguageTool version: $LT_VERSION (minor: $LT_MINOR)"
echo "Project minor: $PROJECT_MINOR"
echo ""

if [ "$PROJECT_MINOR" != "$LT_MINOR" ]; then
    NEW_VERSION="${PROJECT_MAJOR}.${LT_MINOR}.0"
    echo "Minor version mismatch. Updating project version to $NEW_VERSION..."
    update_pom_version "$POM_VERSION" "$NEW_VERSION"
    npm version "$NEW_VERSION" --no-git-tag-version
    POM_VERSION="$NEW_VERSION"
    echo "Updated all files to $NEW_VERSION"
    echo ""
fi

# 3. Check git tag does not already exist
TAG="v${POM_VERSION}"
if git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "Error: git tag $TAG already exists. Bump the version before releasing."
    exit 1
fi

# 4. Build
echo "Building..."
mvn package
echo ""

# 5. Tests
echo "Running tests..."
./test.sh
echo ""

# 6. Next steps
echo "========================================="
echo " Release $POM_VERSION ready!"
echo "========================================="
echo ""
echo "Next steps:"
echo ""
echo "  git add -A && git commit -m 'chore: release ${POM_VERSION}'"
echo "  git tag v${POM_VERSION}"
echo "  git push && git push --tags"
echo ""
echo "  npm login        # if not already logged in"
echo "  npm publish --access public"
