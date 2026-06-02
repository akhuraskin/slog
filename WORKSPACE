load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "com_github_gflags_gflags",
    sha256 = "34af2f15cf7367513b352bdcd2493ab14ce43692d2dcd9dfc499492966c64dcf",
    strip_prefix = "gflags-2.2.2",
    urls = ["https://github.com/gflags/gflags/archive/v2.2.2.tar.gz"],
)

http_archive(
    name = "com_github_google_googletest",
    sha256 = "9dc9157a9a1551ec7a7e43daea9a694a0bb5fb8bec81235d8a1e6ef64c716dcb",
    strip_prefix = "googletest-release-1.10.0",
    url = "https://github.com/google/googletest/archive/release-1.10.0.tar.gz",
)

http_archive(
    name = "com_github_google_glog",
    sha256 = "21bc744fb7f2fa701ee8db339ded7dce4f975d0d55837a97be7d46e8382dea5a",
    strip_prefix = "glog-0.5.0",
    urls = ["https://github.com/google/glog/archive/v0.5.0.zip"],
)

http_archive(
    name = "com_github_google_benchmark",
    build_file_content =
        """
cc_library(
    name = "benchmark",
    srcs = glob(["src/*.h", "src/*.cc"]),
    hdrs = glob(["include/benchmark/benchmark.h"]),
    copts = ["-O3", "-DNDEBUG", "-fPIC"],
    includes = ["include"],
    linkstatic = 1,
    visibility = ["//visibility:public"],
)
""",
    sha256 = "21e6e096c9a9a88076b46bd38c33660f565fa050ca427125f64c4a8bf60f336b",
    strip_prefix = "benchmark-1.5.2",
    urls = ["https://github.com/google/benchmark/archive/v1.5.2.zip"],
)

# Importing python_build_standalone archives that will be used as python
# interpreters for bazel builds.
load("//python_toolchain:fetch_python_build_standalone.bzl", "fetch_all_python_interpreters")

# Per-version interpreters: @python_3_8, @python_3_9, @python_3_10
fetch_all_python_interpreters()

# Register python3 interpreter (3.10) from python_3_10 to be used by bazel builds.
register_toolchains("//python_toolchain")

######## Import pybind11 begin ########

# pybind11 2.11.1 — adds Python 3.10 support (was 2.5.0).
# Custom build_file makes @pybind11//:pybind11 header-only (no hardcoded python_headers dep)
# so that the multi-version extension macro can inject the right headers per version.
http_archive(
    name = "pybind11",
    build_file = "//third_party:pybind11.BUILD",
    sha256 = "d475978da0cdc2d43b73f30910786759d593a9d8ee05b1b6846d1eb16c6d2e0c",
    strip_prefix = "pybind11-2.11.1",
    urls = ["https://github.com/pybind/pybind11/archive/v2.11.1.tar.gz"],
)

load("//python_toolchain:python_headers_configure.bzl", "python_headers_configure")

# Per-version configs used by the multi-version wheel build in pkg_slog_py_wheel.
# Uses a custom repository rule (python_headers_configure) that calls sysconfig instead of
# distutils.sysconfig, avoiding deprecation-warning-as-error failures on Python 3.10+.
python_headers_configure(
    name = "local_config_python_3_8",
    python_interpreter_target = "@python_3_8//:python/install/bin/python3.8",
)

python_headers_configure(
    name = "local_config_python_3_9",
    python_interpreter_target = "@python_3_9//:python/install/bin/python3.9",
)

python_headers_configure(
    name = "local_config_python_3_10",
    python_interpreter_target = "@python_3_10//:python/install/bin/python3.10",
)

######## Import pybind11 end ########

http_archive(
    name = "python_wheel",
    build_file = "//pkg_slog_py_wheel:python_wheel.BUILD",
    sha256 = "9515fe0a94e823fd90b08d22de45d7bde57c90edce705b22f5e1ecf7e1b653c8",
    strip_prefix = "wheel-0.30.0",
    urls = ["https://files.pythonhosted.org/packages/fa/b4/f9886517624a4dcb81a1d766f68034344b7565db69f13d52697222daeb72/wheel-0.30.0.tar.gz"],
)
