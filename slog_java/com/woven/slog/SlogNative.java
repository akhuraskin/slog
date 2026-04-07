// Copyright 2022 Woven Planet Holdings
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.woven.slog;

final class SlogNative {
    static {
        System.loadLibrary("slog_jni");
    }

    private SlogNative() {}

    // SlogContext
    static native long contextGetInstance();
    static native void contextRelease(long contextPtr);

    // Call site management
    static native int addOrReuseCallSiteVerySlow(
            String function, String file, int line);

    // SlogEvent lifecycle
    static native long eventCreate(byte severity, int callSiteId);
    static native void eventAddTagString(long eventPtr, String key, String value);
    static native void eventAddTagLong(long eventPtr, String key, long value);
    static native void eventAddTagDouble(long eventPtr, String key, double value);
    static native void eventEmitValue(long eventPtr, String value);
    static native void eventDestroy(long eventPtr);

    // SlogScope lifecycle
    static native long scopeCreate(long eventPtr);
    static native void scopeDestroy(long scopePtr);

    // SlogBuffer lifecycle
    static native long bufferCreate(long contextPtr);
    static native void bufferDestroy(long bufferPtr);
    static native void bufferWaitSlogQueue(long bufferPtr);
    static native SlogBufferData bufferFlush(long bufferPtr);
}
