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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SlogRecord {
    private final int threadId;
    private final int callSiteId;
    private final byte severity;
    private final long elapsedNs;
    private final long globalNs;
    private final List<SlogTag> tags;
    private final String jsonString;
    private final String slogText;
    private final String flatText;

    SlogRecord(int threadId, int callSiteId, byte severity,
               long elapsedNs, long globalNs,
               SlogTag[] tags, String jsonString,
               String slogText, String flatText) {
        this.threadId = threadId;
        this.callSiteId = callSiteId;
        this.severity = severity;
        this.elapsedNs = elapsedNs;
        this.globalNs = globalNs;
        this.tags = Collections.unmodifiableList(Arrays.asList(tags));
        this.jsonString = jsonString;
        this.slogText = slogText;
        this.flatText = flatText;
    }

    public int getThreadId() { return threadId; }
    public int getCallSiteId() { return callSiteId; }
    public byte getSeverity() { return severity; }
    public long getElapsedNs() { return elapsedNs; }
    public long getGlobalNs() { return globalNs; }
    public List<SlogTag> getTags() { return tags; }
    public String getSlogText() { return slogText; }
    public String getFlatText() { return flatText; }

    @Override
    public String toString() { return jsonString; }
}
