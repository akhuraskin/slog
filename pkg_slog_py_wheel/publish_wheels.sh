#!/usr/bin/env bash
set -euo pipefail

REPO_DEV="https://na1-artifactory.stargate.toyota/artifactory/api/pypi/aadadas-us-pypi-internal-ephemeral-dev-local"
REPO_PROD="https://na1-artifactory.stargate.toyota/artifactory/api/pypi/aadadas-us-pypi-internal-dev-local"

ARTIFACTORY_BASE="https://na1-artifactory.stargate.toyota/artifactory"
REPO_DEV_PATH="aadadas-us-pypi-internal-ephemeral-dev-local"
REPO_PROD_PATH="aadadas-us-pypi-internal-dev-local"

PYTHON_VERSIONS=(3.8 3.9 3.10)

usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Build and publish slog_py wheels for Python 3.8, 3.9, 3.10.

Options:
  --version VERSION   Package version (required if SLOG_RELEASE_VERSION is not set)
  --prod              Publish to prod repo (default: ephemeral dev repo)
  --dry-run           Build wheels but do not upload

Environment variables:
  SLOG_RELEASE_VERSION  Package version (overridden by --version flag)
  ARTIFACTORY_USER      Artifactory username (falls back to ~/.netrc)
  ARTIFACTORY_TOKEN     Artifactory password/token (falls back to ~/.netrc)

Example resulting paths (dev):
  ${ARTIFACTORY_BASE}/${REPO_DEV_PATH}/slog-py/1.2.3/slog_py-1.2.3-cp38-cp38-manylinux2014_x86_64.whl
  ${ARTIFACTORY_BASE}/${REPO_DEV_PATH}/slog-py/1.2.3/slog_py-1.2.3-cp39-cp39-manylinux2014_x86_64.whl
  ${ARTIFACTORY_BASE}/${REPO_DEV_PATH}/slog-py/1.2.3/slog_py-1.2.3-cp310-cp310-manylinux2014_x86_64.whl

Example resulting paths (--prod):
  ${ARTIFACTORY_BASE}/${REPO_PROD_PATH}/slog-py/1.2.3/slog_py-1.2.3-cp38-cp38-manylinux2014_x86_64.whl
  ${ARTIFACTORY_BASE}/${REPO_PROD_PATH}/slog-py/1.2.3/slog_py-1.2.3-cp39-cp39-manylinux2014_x86_64.whl
  ${ARTIFACTORY_BASE}/${REPO_PROD_PATH}/slog-py/1.2.3/slog_py-1.2.3-cp310-cp310-manylinux2014_x86_64.whl
EOF
}

# ---- prerequisite checks ---------------------------------------------------

if ! command -v bazelisk &>/dev/null; then
    echo "ERROR: bazelisk is not installed. Install it from https://github.com/bazelbuild/bazelisk" >&2
    exit 1
fi

if ! command -v twine &>/dev/null; then
    echo "ERROR: twine is not installed. Install it with: pip install twine" >&2
    exit 1
fi

# ---- argument parsing -------------------------------------------------------

VERSION="${SLOG_RELEASE_VERSION:-}"
PROD=false
DRY_RUN=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --version)
            VERSION="$2"
            shift 2
            ;;
        --prod)
            PROD=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --help|-h)
            usage
            exit 0
            ;;
        *)
            echo "ERROR: Unknown argument: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

if [[ -z "${VERSION}" ]]; then
    echo "ERROR: --version is required (or set SLOG_RELEASE_VERSION)" >&2
    usage >&2
    exit 1
fi

# ---- credential resolution --------------------------------------------------

if [[ -n "${ARTIFACTORY_USER:-}" && -n "${ARTIFACTORY_TOKEN:-}" ]]; then
    TWINE_AUTH=(--username "${ARTIFACTORY_USER}" --password "${ARTIFACTORY_TOKEN}")
else
    if ! grep -q "machine na1-artifactory.stargate.toyota" ~/.netrc 2>/dev/null; then
        echo "ERROR: No credentials found. Set ARTIFACTORY_USER/ARTIFACTORY_TOKEN or add an entry for na1-artifactory.stargate.toyota to ~/.netrc" >&2
        exit 1
    fi
    TWINE_AUTH=()
fi

# ---- repo selection ---------------------------------------------------------

if [[ "${PROD}" == "true" ]]; then
    REPO_URL="${REPO_PROD}"
    REPO_PATH="${REPO_PROD_PATH}"
else
    REPO_URL="${REPO_DEV}"
    REPO_PATH="${REPO_DEV_PATH}"
fi

# ---- build ------------------------------------------------------------------

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "[INFO] Building slog_py ${VERSION} for Python ${PYTHON_VERSIONS[*]}"

for v in "${PYTHON_VERSIONS[@]}"; do
    echo "[INFO] Building for Python ${v}..."
    (
        cd "${REPO_ROOT}"
        bazelisk build //pkg_slog_py_wheel:slog_py_whl \
            "--//pkg_slog_py_wheel:whl_build_python_version=${v}" \
            "--define=SLOG_RELEASE_VERSION=${VERSION}"
    )
done

WHEELS=("${REPO_ROOT}/bazel-bin/pkg_slog_py_wheel/slog_py-${VERSION}"-*.whl)

if [[ ${#WHEELS[@]} -eq 0 ]]; then
    echo "ERROR: No wheels found in bazel-bin/pkg_slog_py_wheel/ for version ${VERSION}" >&2
    exit 1
fi

echo "[INFO] Built wheels:"
for whl in "${WHEELS[@]}"; do
    echo "  ${whl}"
done

# ---- upload -----------------------------------------------------------------

if [[ "${DRY_RUN}" == "true" ]]; then
    echo "[INFO] Dry run — skipping upload"
    echo "[INFO] Would publish to: ${REPO_URL}"
    echo "[INFO] Resulting paths:"
    for whl in "${WHEELS[@]}"; do
        fname="$(basename "${whl}")"
        echo "  ${ARTIFACTORY_BASE}/${REPO_PATH}/slog-py/${VERSION}/${fname}"
    done
else
    echo "[INFO] Uploading to ${REPO_URL}..."
    twine upload \
        --repository-url "${REPO_URL}" \
        "${TWINE_AUTH[@]}" \
        "${WHEELS[@]}"

    echo "[INFO] Published paths:"
    for whl in "${WHEELS[@]}"; do
        fname="$(basename "${whl}")"
        echo "  ${ARTIFACTORY_BASE}/${REPO_PATH}/slog-py/${VERSION}/${fname}"
    done
fi
