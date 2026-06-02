load("//python_toolchain:python_versions.bzl", "PYTHON_VERSIONS")

def _fetch_python_build_standalone_impl(repository_ctx):
    repository_ctx.download(
        url = [repository_ctx.attr.url],
        sha256 = repository_ctx.attr.sha256,
        output = "python.tar.zst",
    )

    unzstd_bin_path = repository_ctx.which("unzstd")
    if unzstd_bin_path == None:
        fail("Python toolchain requires that the zstd and unzstd are available on the $PATH, but it was not found.")

    res = repository_ctx.execute([unzstd_bin_path, "python.tar.zst"])
    if res.return_code:
        fail("Error decompressing with zstd" + res.stdout + res.stderr)

    repository_ctx.extract(archive = "python.tar")
    repository_ctx.delete("python.tar")
    repository_ctx.delete("python.tar.zst")

    bin_path = repository_ctx.attr.bin_path
    repository_ctx.file("BUILD.bazel", """
package(default_visibility = ["//visibility:public"])
filegroup(
    name = "files",
    srcs = glob(["install/**"], exclude = ["**/* *"]),
)
filegroup(
    name = "interpreter",
    srcs = ["{bin_path}"],
)
sh_binary(
    name = "foo",
    srcs = ["{bin_path}"],
)
""".format(bin_path = bin_path))

_fetch_python_build_standalone = repository_rule(
    implementation = _fetch_python_build_standalone_impl,
    attrs = {
        "url": attr.string(mandatory = True),
        "sha256": attr.string(mandatory = True),
        "bin_path": attr.string(mandatory = True),
    },
)

def fetch_all_python_interpreters():
    """Fetches all supported CPython versions as @python_3_8, @python_3_9, @python_3_10."""
    for version_key, info in PYTHON_VERSIONS.items():
        _fetch_python_build_standalone(
            name = "python_" + version_key,
            url = info["url"],
            sha256 = info["sha256"],
            bin_path = info["bin_path"],
        )
