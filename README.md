# Slog
Slog (reads "S-log") is a simple, fast, and cross-language structured logging. It's like Glog but allows structure via tags and async handling.

# Importing
## Into a C++ project built by Bazel
Requirements:
* C++14

Instructions: 
* Follow the example from `test_import/example_project_cc_via_bazel` directory.

## Into a Python project built by Bazel
Requirements:
* All C++ requirements from above
* Python 3.8, 3.9, or 3.10
* pybind11 and pybind11_bazel

Instructions:
* Follow the example from `test_import/example_project_py_via_bazel` directory.

## Publishing slog_py wheels to Artifactory

`pkg_slog_py_wheel/publish_wheels.sh` builds wheels for Python 3.8, 3.9, and 3.10 and uploads them (see [Building a single wheel manually](#building-a-single-wheel-manually) for low-level Bazel commands). The version is read automatically from the `VERSION` file.

Prerequisites:
* `bazelisk` on `PATH`
* `twine` (`pip install twine`)
* Credentials — one of:
  * Set `ARTIFACTORY_USER` and `ARTIFACTORY_TOKEN` environment variables, or
  * Add an entry to `~/.netrc`:
    ```
    machine na1-artifactory.stargate.toyota
    login <username>
    password <token>
    ```
    `~/.netrc` must be owner-readable only: `chmod 600 ~/.netrc`

```bash
# Publish to the ephemeral dev repo (default)
pkg_slog_py_wheel/publish_wheels.sh

# Publish to the production repo
pkg_slog_py_wheel/publish_wheels.sh --prod

# Build only, skip upload
pkg_slog_py_wheel/publish_wheels.sh --dry-run
```

Resulting artifact paths (dev):
```
https://na1-artifactory.stargate.toyota/artifactory/aadadas-us-pypi-internal-ephemeral-dev-local/slog-py/0.0.1/slog_py-0.0.1-cp38-cp38-manylinux2014_x86_64.whl
https://na1-artifactory.stargate.toyota/artifactory/aadadas-us-pypi-internal-ephemeral-dev-local/slog-py/0.0.1/slog_py-0.0.1-cp39-cp39-manylinux2014_x86_64.whl
https://na1-artifactory.stargate.toyota/artifactory/aadadas-us-pypi-internal-ephemeral-dev-local/slog-py/0.0.1/slog_py-0.0.1-cp310-cp310-manylinux2014_x86_64.whl
```

Use `--prod` to publish to `aadadas-us-pypi-internal-dev-local` instead.

### Building a single wheel manually

The version is passed via `--define`. The target Python version is selected via a flag; the default is 3.10.

```bash
# Build wheel for Python 3.10 (default)
bazelisk build //pkg_slog_py_wheel:slog_py_whl \
  --define SLOG_RELEASE_VERSION=$(cat VERSION)

# Build wheel for a specific Python version
bazelisk build //pkg_slog_py_wheel:slog_py_whl \
  --//pkg_slog_py_wheel:whl_build_python_version=3.8 \
  --define SLOG_RELEASE_VERSION=$(cat VERSION)
```

The output wheel is written to `bazel-bin/pkg_slog_py_wheel/`:
```
slog_py-0.0.1-cp310-cp310-manylinux2014_x86_64.whl
```

## Into a Java project
Requirements:
* Java 8+
* All C++ requirements from above (for building from source)

### Usage
```java
import com.woven.slog.Slog;
import com.woven.slog.SlogScope;

Slog.info("Hello from Java");
Slog.info("with tags", Slog.tags("key", "value"));

try (SlogScope scope = Slog.scope("my_scope")) {
    Slog.info("inside scope");
}
```

### Building versioned artifacts
Run the packaging script to produce versioned, traceable artifacts:
```bash
pkg_slog_java/build_artifacts.sh
```
The version is auto-detected from the `VERSION` file and git SHA. It produces in `pkg_slog_java/out/`:
* `slog_java-<version>.jar` — the Java library
* `slog_jni-<version>.tar.gz` — native `.so` files required at runtime
* `slog_java-<version>.metadata.txt` — version, git SHA, and build timestamp

The script also creates an annotated git tag `slog_java-v<version>` for traceability. Upload the produced artifacts to Artifactory.

### Importing via Bazel
Import slog into your `WORKSPACE` as an `http_archive` (from Artifactory) or `git_repository` (from source), same as for C++. The Java target is included automatically. Then in your `BUILD` file:
```python
java_binary(
    name = "my_app",
    srcs = ["MyApp.java"],
    deps = ["@slog//:slog_java"],
)
```

### Importing via Gradle
The versioned artifacts on Artifactory (`slog_java-<version>.jar` and `slog_jni-<version>.tar.gz`) should be fetched as part of your project's build pipeline and placed into `libs/`. Then in your `build.gradle`:
```groovy
dependencies {
    implementation files('libs/slog_java.jar')
}

test {
    def nativeDir = file("${projectDir}/libs/native").absolutePath
    systemProperty 'java.library.path', nativeDir
    environment 'LD_LIBRARY_PATH', nativeDir
}
```

A development example for testing within this repo is in `test_import/example_project_java_via_gradle`.

# Contributing
## Installing tools for build
* `scripts/setup_dev_env.sh`

## Running tests
* `bazelisk test --test_output=errors //...` -- run all unit tests.

## Code formatting
* For the first time, install linters with `./scripts/install_linters.sh `
* `scripts/lint.sh ./ -i` -- automatically format all code.

## Releases.
CI automatically uploads a zip-archive to Artifactory. This .zip archive could be imported into another bazel project. The .zip file naming and content are matching a .zip file that could be created via GitHub releases. For Java artifacts, see [Building versioned artifacts](#building-versioned-artifacts) above.

