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

public final class SlogBuffer implements AutoCloseable {
    private long nativePtr;

    public SlogBuffer(SlogContext context) {
        this.nativePtr = SlogNative.bufferCreate(context.getNativePtr());
    }

    public void waitSlogQueue() {
        checkNotClosed();
        SlogNative.bufferWaitSlogQueue(nativePtr);
    }

    public SlogBufferData flush() {
        checkNotClosed();
        return SlogNative.bufferFlush(nativePtr);
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            SlogNative.bufferDestroy(nativePtr);
            nativePtr = 0;
        }
    }

    private void checkNotClosed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("SlogBuffer has been closed");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
