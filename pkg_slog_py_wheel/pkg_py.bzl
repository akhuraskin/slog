"""Macro to build and publish slog_py wheels for multiple Python versions."""

load("@rules_python//python:packaging.bzl", "py_package", "py_wheel")

# ---------------------------------------------------------------------------
# Config transition: reads the //pkg_slog_py_wheel:whl_build_python_version
# flag and forwards it to rules_python's python_version config setting.
# This makes pybind_extension pick up the correct Python headers/interpreter.
# ---------------------------------------------------------------------------

def _py_transition_impl(settings, attr):
    return {
        "@rules_python//python/config_settings:python_version": settings["//pkg_slog_py_wheel:whl_build_python_version"],
    }

_py_transition = transition(
    implementation = _py_transition_impl,
    inputs = ["//pkg_slog_py_wheel:whl_build_python_version"],
    outputs = ["@rules_python//python/config_settings:python_version"],
)

def _py_library_for_wheel_impl(ctx):
    actual_target = ctx.attr.dep[0]
    providers = [actual_target[DefaultInfo]]
    if PyInfo in actual_target:
        providers.append(actual_target[PyInfo])
    return providers

py_library_for_wheel = rule(
    implementation = _py_library_for_wheel_impl,
    attrs = {
        "dep": attr.label(cfg = _py_transition),
        "_allowlist_function_transition": attr.label(
            default = "@bazel_tools//tools/allowlists/function_transition_allowlist",
        ),
    },
)

# ---------------------------------------------------------------------------
# install_local_wheel: unpacks a .whl into a directory and exposes it as a
# PyInfo target. Useful for testing the packaged wheel rather than sources.
# ---------------------------------------------------------------------------

def _install_local_wheel_impl(ctx):
    wheel = ctx.file.wheel
    out_dir = ctx.actions.declare_directory(ctx.label.name + "_extracted")

    ctx.actions.run(
        outputs = [out_dir],
        inputs = [wheel],
        executable = "/usr/bin/unzip",
        arguments = [
            "-q",
            wheel.path,
            "-d",
            out_dir.path,
        ],
        progress_message = "Extracting wheel %s" % wheel.basename,
    )

    return [
        DefaultInfo(
            files = depset([out_dir]),
            runfiles = ctx.runfiles([out_dir]),
        ),
        PyInfo(
            transitive_sources = depset([]),
            imports = depset(["_main/" + out_dir.short_path]),
        ),
    ]

install_local_wheel = rule(
    implementation = _install_local_wheel_impl,
    attrs = {
        "wheel": attr.label(
            allow_single_file = [".whl"],
            mandatory = True,
        ),
    },
    provides = [PyInfo],
)

# ---------------------------------------------------------------------------
# py_distribution_bundle: creates wheel + local-install + publish targets.
#
# For the given <name> the macro produces:
#   <name>                   py_library (public, for use in bazel deps)
#   <name>_for_wheel         py_library with Python version transition applied
#   <name>_whl_package       py_package collecting all transitive sources
#   <name>_whl               the .whl file
#   <name>_whl_local_package installed wheel as a PyInfo target (for tests)
#   <name>_publish           bazel run target: twine upload to Artifactory
#
# Build a wheel for a specific Python version:
#   bazel build :<name>_whl --//pkg_slog_py_wheel:whl_build_python_version=3.8
#     --define SLOG_RELEASE_VERSION=1.2.3
#
# Publish:
#   bazel run :<name>_publish --//pkg_slog_py_wheel:whl_build_python_version=3.9
#     --define SLOG_RELEASE_VERSION=1.2.3
# ---------------------------------------------------------------------------

def py_distribution_bundle(name, deps, pip_requires = []):
    native.py_library(
        name = name,
        deps = deps,
        visibility = ["//visibility:public"],
    )

    py_library_for_wheel(
        name = name + "_for_wheel",
        dep = ":" + name,
    )

    py_package(
        name = name + "_whl_package",
        deps = [":" + name + "_for_wheel"],
    )

    tag_map = {}
    for py_version in range(8, 20):
        setting_name = name + "_v3" + str(py_version)
        native.config_setting(
            name = setting_name,
            flag_values = {"//pkg_slog_py_wheel:whl_build_python_version": "3." + str(py_version)},
        )
        tag_map[":" + setting_name] = "cp3" + str(py_version)

    py_wheel(
        name = name + "_whl",
        distribution = name,
        version = "$(SLOG_RELEASE_VERSION)",
        python_tag = select(tag_map),
        abi = select(tag_map),
        platform = "manylinux2014_x86_64",
        deps = [":" + name + "_whl_package"],
        requires = pip_requires,
    )

    install_local_wheel(
        name = name + "_whl_local_package",
        wheel = ":" + name + "_whl",
        visibility = ["//visibility:public"],
    )
