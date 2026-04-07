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

final class SlogEvent {
    private long nativePtr;

    SlogEvent(byte severity, int callSiteId) {
        this.nativePtr = SlogNative.eventCreate(severity, callSiteId);
    }

    void addTag(String key, String value) {
        checkNotDestroyed();
        SlogNative.eventAddTagString(nativePtr, key, value);
    }

    void addTag(String key, long value) {
        checkNotDestroyed();
        SlogNative.eventAddTagLong(nativePtr, key, value);
    }

    void addTag(String key, double value) {
        checkNotDestroyed();
        SlogNative.eventAddTagDouble(nativePtr, key, value);
    }

    void emitValue(String value) {
        checkNotDestroyed();
        SlogNative.eventEmitValue(nativePtr, value);
    }

    long getNativePtr() {
        checkNotDestroyed();
        return nativePtr;
    }

    void destroy() {
        if (nativePtr != 0) {
            SlogNative.eventDestroy(nativePtr);
            nativePtr = 0;
        }
    }

    private void checkNotDestroyed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("SlogEvent has already been destroyed");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
}
