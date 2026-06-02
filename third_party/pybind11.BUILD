load("@rules_cc//cc:defs.bzl", "cc_library")

package(default_visibility = ["//visibility:public"])

licenses(["notice"])

exports_files(["LICENSE"])

# Header-only target — does not pull in any specific Python version's headers.
# Callers must add their version-specific @local_config_python_3_X//:python_headers
# to their own deps list.
cc_library(
    name = "pybind11",
    hdrs = glob(
        include = [
            "include/pybind11/*.h",
            "include/pybind11/detail/*.h",
        ],
        exclude = [
            "include/pybind11/common.h",
        ],
    ),
    copts = [
        "-fexceptions",
        "-Xclang-only=-Wno-undefined-inline",
        "-Xclang-only=-Wno-pragma-once-outside-header",
        "-Xgcc-only=-Wno-error",
    ],
    includes = ["include"],
)

config_setting(
    name = "osx",
    constraint_values = ["@platforms//os:osx"],
)
