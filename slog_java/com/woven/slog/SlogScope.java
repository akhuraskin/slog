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

public final class SlogScope implements AutoCloseable {
    private long nativePtr;

    SlogScope(SlogEvent event) {
        this.nativePtr = SlogNative.scopeCreate(event.getNativePtr());
        // Destroy the event to fire the scope-open record.
        event.destroy();
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            SlogNative.scopeDestroy(nativePtr);
            nativePtr = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
