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

public final class SlogBufferData {
    private final List<SlogRecord> records;
    private final List<SlogCallSite> callSites;

    SlogBufferData(SlogRecord[] records, SlogCallSite[] callSites) {
        this.records = Collections.unmodifiableList(Arrays.asList(records));
        this.callSites = Collections.unmodifiableList(Arrays.asList(callSites));
    }

    public List<SlogRecord> getRecords() { return records; }
    public List<SlogCallSite> getCallSites() { return callSites; }
}
