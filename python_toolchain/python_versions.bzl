"""Versioned CPython standalone interpreter downloads for Bazel builds."""

# Each entry maps a Bazel-safe version string to download metadata.
# All archives use the python-build-standalone project:
#   https://github.com/indygreg/python-build-standalone
PYTHON_VERSIONS = {
    "3_8": {
        "url": "https://github.com/indygreg/python-build-standalone/releases/download/20210228/cpython-3.8.8-x86_64-unknown-linux-gnu-pgo+lto-20210228T1503.tar.zst",
        "sha256": "74c9067b363758e501434a02af87047de46085148e673547214526da6e2b2155",
        "bin_path": "python/install/bin/python3.8",
    },
    "3_9": {
        "url": "https://github.com/indygreg/python-build-standalone/releases/download/20210228/cpython-3.9.2-x86_64-unknown-linux-gnu-pgo+lto-20210228T1503.tar.zst",
        "sha256": "4537b1d7a7609221fdbb73d962b8de1136c4e3ecc050b8c5ad92e066db4b2afa",
        "bin_path": "python/install/bin/python3.9",
    },
    "3_10": {
        "url": "https://github.com/astral-sh/python-build-standalone/releases/download/20211017/cpython-3.10.0-x86_64-unknown-linux-gnu-pgo+lto-20211017T1616.tar.zst",
        "sha256": "ba898cb849b02e9f6f567a3151435d5f6ba4dd3962890a2e45503677c6bd203c",
        "bin_path": "python/install/bin/python3.10",
    },
}
