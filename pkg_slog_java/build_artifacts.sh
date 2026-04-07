#!/bin/bash

# Builds versioned slog Java artifacts (JAR + native .so files) for upload
# to Artifactory.
#
# Usage:
#   ./build_artifacts.sh
#
# Version is auto-detected from the VERSION file and git SHA.
#
# Output:
#   out/slog_java-<version>.jar              (Java library)
#   out/slog_jni-<version>.tar.gz            (native .so files)
#   out/slog_java-<version>.metadata.txt     (version, git SHA, build date)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SLOG_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
GIT_SHA="$(git -C "${SLOG_ROOT}" rev-parse --short HEAD)"

if [ -f "${SLOG_ROOT}/VERSION" ]; then
    VERSION="$(cat "${SLOG_ROOT}/VERSION")-${GIT_SHA}"
else
    VERSION="${GIT_SHA}"
fi
OUT_DIR="${SCRIPT_DIR}/out"

rm -rf "${OUT_DIR}"
mkdir -p "${OUT_DIR}"

echo "Building slog_java artifacts (version=${VERSION}, git=${GIT_SHA})..."
(cd "${SLOG_ROOT}" && bazelisk build //slog_java:libslog_jni.so //slog_java:slog_java)

BAZEL_BIN="${SLOG_ROOT}/bazel-bin"

# Copy and rename JAR with version info.
JAR_NAME="slog_java-${VERSION}.jar"
cp "${BAZEL_BIN}/slog_java/libslog_java.jar" "${OUT_DIR}/${JAR_NAME}"
echo "Created ${OUT_DIR}/${JAR_NAME}"

# Bundle native .so files into a versioned tarball.
NATIVE_STAGING="${OUT_DIR}/native"
mkdir -p "${NATIVE_STAGING}"
cp "${BAZEL_BIN}/slog_java/libslog_jni.so" "${NATIVE_STAGING}/"

SOLIB_DIR="${BAZEL_BIN}/slog_java/libslog_jni.so.runfiles/__main__/_solib_k8"
if [ -d "${SOLIB_DIR}" ]; then
    cp "${SOLIB_DIR}"/*.so "${NATIVE_STAGING}/"
else
    echo "WARNING: solib directory not found at ${SOLIB_DIR}"
    echo "Trying fallback location..."
    cp "${BAZEL_BIN}/_solib_k8"/libslog_*.so "${NATIVE_STAGING}/"
fi

TARBALL_NAME="slog_jni-${VERSION}.tar.gz"
(cd "${OUT_DIR}" && tar czf "${TARBALL_NAME}" -C native .)
rm -rf "${NATIVE_STAGING}"
echo "Created ${OUT_DIR}/${TARBALL_NAME}"

# Write a version metadata file for traceability.
cat > "${OUT_DIR}/slog_java-${VERSION}.metadata.txt" <<METADATA
version=${VERSION}
git_sha=${GIT_SHA}
build_date=$(date -u +%Y-%m-%dT%H:%M:%SZ)
METADATA
echo "Created ${OUT_DIR}/slog_java-${VERSION}.metadata.txt"

# Tag the commit with the version for traceability.
TAG_NAME="slog_java-v${VERSION}"
if git -C "${SLOG_ROOT}" rev-parse "${TAG_NAME}" >/dev/null 2>&1; then
    echo "Git tag ${TAG_NAME} already exists, skipping."
else
    git -C "${SLOG_ROOT}" tag -a "${TAG_NAME}" -m "slog_java release ${VERSION}"
    echo "Created git tag: ${TAG_NAME}"
fi

echo ""
echo "Artifacts ready in ${OUT_DIR}/:"
ls -la "${OUT_DIR}/"
