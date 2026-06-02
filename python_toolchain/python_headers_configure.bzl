"""Custom repository rule to expose Python headers for a given interpreter.

Replaces python_configure from pybind11_bazel, which breaks on Python >= 3.10
because it uses distutils.sysconfig and treats deprecation warnings on stderr as errors.
Uses sysconfig (stdlib since Python 3.2) and symlinks the include dir directly.
"""

def _python_headers_configure_impl(repository_ctx):
    python_bin = str(repository_ctx.path(repository_ctx.attr.python_interpreter_target))

    result = repository_ctx.execute([
        python_bin,
        "-c",
        "import sysconfig; print(sysconfig.get_path('include'))",
    ])
    if result.return_code != 0 or not result.stdout.strip():
        fail("Failed to get Python include path from %s:\n%s" % (python_bin, result.stderr))

    python_include = result.stdout.strip()

    # Symlink the entire include dir so Bazel can glob it without a genrule.
    repository_ctx.symlink(python_include, "python_include")

    repository_ctx.file("BUILD.bazel", """
package(default_visibility = ["//visibility:public"])

cc_library(
    name = "python_headers",
    hdrs = glob(["python_include/**/*.h"]),
    includes = ["python_include"],
)
""")

python_headers_configure = repository_rule(
    implementation = _python_headers_configure_impl,
    attrs = {
        "python_interpreter_target": attr.label(mandatory = True),
    },
)
