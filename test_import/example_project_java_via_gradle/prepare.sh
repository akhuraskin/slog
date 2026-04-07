#!/bin/bash

# Builds slog Java artifacts using Bazel and copies them into this project's
# libs/ directory so that Gradle can use them.
#
# Usage:
#   ./prepare.sh
#
# Prerequisites: Run from the slog repository root or from this directory.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SLOG_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

echo "Building slog_java with Bazel..."
(cd "${SLOG_ROOT}" && bazel build //slog_java:libslog_jni.so //slog_java:slog_java)

BAZEL_BIN="${SLOG_ROOT}/bazel-bin"
LIBS_DIR="${SCRIPT_DIR}/libs"
NATIVE_DIR="${LIBS_DIR}/native"

rm -rf "${LIBS_DIR}"
mkdir -p "${NATIVE_DIR}"

echo "Copying JAR..."
cp "${BAZEL_BIN}/slog_java/libslog_java.jar" "${LIBS_DIR}/slog_java.jar"

echo "Copying native libraries..."
cp "${BAZEL_BIN}/slog_java/libslog_jni.so" "${NATIVE_DIR}/"

# Copy all dependent shared libraries that libslog_jni.so needs at runtime.
SOLIB_DIR="${BAZEL_BIN}/slog_java/libslog_jni.so.runfiles/__main__/_solib_k8"
if [ -d "${SOLIB_DIR}" ]; then
    cp "${SOLIB_DIR}"/*.so "${NATIVE_DIR}/"
else
    echo "WARNING: solib directory not found at ${SOLIB_DIR}"
    echo "Trying fallback location..."
    cp "${BAZEL_BIN}/_solib_k8"/libslog_*.so "${NATIVE_DIR}/"
fi

echo "Done. Artifacts in ${LIBS_DIR}:"
ls -la "${LIBS_DIR}/"
ls -la "${NATIVE_DIR}/"
