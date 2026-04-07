cc_binary(
    name = "libslog_jni.so",
    srcs = ["slog_jni.cc"],
    linkshared = True,
    linkstatic = False,
    # Same rpath concern as slog_py: the SlogContext singleton must be shared
    # across all compilation units that use slog, so we link dynamically.
    linkopts = [
        "-Wl,-rpath=$$ORIGIN/../../__main__/_solib_k8/",
    ],
    deps = [
        "//slog_cc/buffer:buffer_cc",
        "//slog_cc/events:events_cc",
        "//slog_cc/printer",
        "@bazel_tools//tools/jdk:jni",
    ],
)

java_library(
    name = "slog_java",
    srcs = glob(["com/woven/slog/*.java"]),
    data = [":libslog_jni.so"],
    javacopts = [
        "--release",
        "8",
    ],
    visibility = [
        "//:__pkg__",
    ],
)

java_test(
    name = "slog_java_test",
    srcs = ["SlogTest.java"],
    data = [":libslog_jni.so"],
    javacopts = [
        "--release",
        "8",
    ],
    jvm_flags = [
        "-Djava.library.path=slog_java",
        "-Djava.security.manager=allow",
    ],
    test_class = "com.woven.slog.SlogTest",
    deps = [
        ":slog_java",
    ],
)
