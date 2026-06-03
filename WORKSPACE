load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "com_github_gflags_gflags",
    sha256 = "34af2f15cf7367513b352bdcd2493ab14ce43692d2dcd9dfc499492966c64dcf",
    strip_prefix = "gflags-2.2.2",
    urls = ["https://github.com/gflags/gflags/archive/v2.2.2.tar.gz"],
)

http_archive(
    name = "com_github_google_googletest",
    sha256 = "8ad598c73ad796e0d8280b082cebd82a630d73e73cd3c70057938a6501bba5d7",
    strip_prefix = "googletest-1.14.0",
    urls = ["https://github.com/google/googletest/archive/v1.14.0.tar.gz"],
)

http_archive(
    name = "com_github_google_glog",
    sha256 = "122fb6b712808ef43fbf80f75c52a21c9760683dae470154f02bddfc61135022",
    strip_prefix = "glog-0.6.0",
    urls = ["https://github.com/google/glog/archive/v0.6.0.zip"],
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

######## Python toolchain (rules_python) begin ########

http_archive(
    name = "bazel_skylib",
    sha256 = "cd55a062e763b9349921f0f5db8c3933288dc8ba4f76dd9416aac68acee3cb94",
    urls = ["https://github.com/bazelbuild/bazel-skylib/releases/download/1.5.0/bazel-skylib-1.5.0.tar.gz"],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

http_archive(
    name = "rules_python",
    sha256 = "0e68f851a6fcf317eeab5f6dc79803cb183d30c0c65fb52e2c4b731d13b73349",
    strip_prefix = "rules_python-1.5.2",
    url = "https://github.com/bazel-contrib/rules_python/releases/download/1.5.2/rules_python-1.5.2.tar.gz",
)

load("@rules_python//python:repositories.bzl", "py_repositories", "python_register_toolchains")

py_repositories()

# Register per-version toolchains so the transition can select the right interpreter.
python_register_toolchains(
    name = "python_3_8",
    python_version = "3.8",
)

python_register_toolchains(
    name = "python_3_9",
    python_version = "3.9",
)

python_register_toolchains(
    name = "python_3_10",
    python_version = "3.10",
)

######## Python toolchain (rules_python) end ########

######## Import pybind11 begin ########

# pybind11_bazel 2.12.0 uses @rules_python//python/cc:current_py_cc_headers which
# automatically resolves to the headers for the active Python version transition —
# no python_configure call needed. pybind11 itself is fetched by
# internal_configure_extension in MODULE.bazel, but we also provide it via http_archive
# for the WORKSPACE (non-bzlmod) code path used by this project.
http_archive(
    name = "pybind11_bazel",
    sha256 = "a58c25c5fe063a70057fa20cb8e15f3bda19b1030305bcb533af1e45f36a4a55",
    strip_prefix = "pybind11_bazel-2.12.0",
    urls = ["https://github.com/pybind/pybind11_bazel/archive/v2.12.0.zip"],
)

http_archive(
    name = "pybind11",
    build_file = "@pybind11_bazel//:pybind11-BUILD.bazel",
    sha256 = "bf8f242abd1abcd375d516a7067490fb71abd79519a282d22b6e4d19282185a7",
    strip_prefix = "pybind11-2.12.0",
    urls = ["https://github.com/pybind/pybind11/archive/v2.12.0.tar.gz"],
)

######## Import pybind11 end ########
