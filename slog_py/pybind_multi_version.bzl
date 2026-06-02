"""Macro to build a pybind11 extension .so for each supported Python version."""

load("//python_toolchain:python_versions.bzl", "PYTHON_VERSIONS")

_PYBIND_COPTS = [
    "-fexceptions",
    "-fvisibility=hidden",
]

_PYBIND_FEATURES = [
    "-use_header_modules",
    "-parse_headers",
]

def pybind_multi_version_extension(name, srcs, deps = [], linkopts = [], tags = [], **kwargs):
    """Creates a pybind11 .so for each Python version in PYTHON_VERSIONS.

    Produces targets named <name>_cp38.so, <name>_cp39.so, <name>_cp310.so.
    """
    for version_key in PYTHON_VERSIONS:
        # "3_8" -> "cp38", "3_10" -> "cp310"
        cp_tag = "cp" + version_key.replace("_", "")
        native.cc_binary(
            name = "{name}_{cp_tag}.so".format(name = name, cp_tag = cp_tag),
            srcs = srcs,
            copts = _PYBIND_COPTS,
            features = _PYBIND_FEATURES,
            linkopts = linkopts + select({
                "@pybind11//:osx": [],
                "//conditions:default": ["-Wl,-Bsymbolic"],
            }),
            linkshared = True,
            # The slog_pybind.so library has to be linked dynamically because it
            # depends on `//slog_cc/context` that has a singleton. Given the library
            # has a singleton it must be linked dynamically and be shared across all
            # units that import it. E.g. a user can import `:slog_py` and create slog
            # events in their code and to handle them they can implement their own
            # subscriber in C++ with python bindings, so both emitting slog events and
            # subscriber must use the *same* context binary code otherwise they will
            # act like different programs despite being run under the same thread id.
            linkstatic = False,
            tags = tags + ["local"],
            deps = deps + [
                "@pybind11//:pybind11",
                "@local_config_python_{version_key}//:python_headers".format(version_key = version_key),
            ],
            **kwargs
        )
