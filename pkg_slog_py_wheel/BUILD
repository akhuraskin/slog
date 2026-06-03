load("@bazel_skylib//rules:common_settings.bzl", "string_flag")
load("//pkg_slog_py_wheel:pkg_py.bzl", "py_distribution_bundle")

# Build-time flag that selects the target Python version for wheel packaging.
# Pass it on the command line:
#   bazel build :slog_py_whl --//pkg_slog_py_wheel:whl_build_python_version=3.8
string_flag(
    name = "whl_build_python_version",
    build_setting_default = "3.10",
    values = [
        "3.8",
        "3.9",
        "3.10",
    ],
)

py_distribution_bundle(
    # Target name sets the PyPI package name.
    # Install with: pip3 install slog-py --extra-index-url <repo>/simple
    name = "slog_py",
    deps = ["//slog_py"],
)
