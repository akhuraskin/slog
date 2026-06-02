load("//slog_py:pybind_multi_version.bzl", "pybind_multi_version_extension")

# Multi-version .so targets for wheel packaging.
# Produces slog_pybind_cp38.so, slog_pybind_cp39.so, slog_pybind_cp310.so.
pybind_multi_version_extension(
    name = "slog_pybind",
    srcs = ["slog_pybind.cc"],

    # When slog is imported to a user repo the relative paths to .so libs
    # become different. We couldn't find an automated solution to this yet and
    # hardcoding a few paths. If user sees an error like
    # `libexternal_Sslog_Sslog_Ucc_Sbuffer_Slibbuffer_Ucc.so: cannot open shared object file: No such file or directory`
    # they have to use `ldd` and `readelf -d` with the generated slog_pybind.so
    # file, e.g.
    # `bazel-out/k8-fastbuild/bin/src/common/logging/slog/slog_py_test.runfiles/slog/slog_py/slog_pybind.so`
    # and patch this BUILD file with adding a path working in their repo.
    linkopts = [
        # TODO(vsbus): how to detect repo dir automatically? Maybe we can use patch in
        # http_archive rule and get values of repo name and solib dir?
        "-Wl,-rpath=$$ORIGIN/../../com_lyft_avsoftware/_solib_local/",
        "-Wl,-rpath=$$ORIGIN/../../__main__/_solib_k8/",
    ],
    visibility = [
        "//:__pkg__",
        "//pkg_slog_py_wheel:__pkg__",
    ],
    deps = [
        "//slog_cc/buffer:buffer_cc",
        "//slog_cc/events:events_cc",
    ],
)

# Rename the cp310 .so to the canonical name that slog.py imports at runtime.
# (The registered py_test toolchain runs Python 3.10.)
genrule(
    name = "slog_pybind_so",
    srcs = [":slog_pybind_cp310.so"],
    outs = ["slog_pybind.so"],
    cmd = "cp $< $@",
)

py_library(
    name = "slog_py",
    srcs = ["slog.py"],
    data = [
        ":slog_pybind_so",        # renamed to slog_pybind.so for Python import
        ":slog_pybind_cp310.so",  # pulls transitive C++ shared libs into runfiles
    ],
    visibility = [
        "//:__pkg__",
        "//pkg_slog_py_wheel:__pkg__",
    ],
)

py_test(
    name = "slog_py_test",
    srcs = ["slog_test.py"],
    main = "slog_test.py",
    deps = [":slog_py"],
)
