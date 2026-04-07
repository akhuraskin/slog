Importing Slog Java bindings into a Gradle project. This example builds the slog Java library and JNI native libraries with Bazel, then uses them from a standard Gradle project.

# Prerequisites

* Bazel (bazelisk) to build the slog native artifacts
* Java 8+ JDK

# Running locally

Go to `test_import/example_project_java_via_gradle` directory:
```
cd test_import/example_project_java_via_gradle
```

Build the slog Java artifacts (JAR + native .so files) with Bazel:
```
./prepare.sh
```

Run the tests with Gradle:
```
./gradlew test
```
