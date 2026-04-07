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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SlogTest {

    @Test
    public void testSeverities() {
        try (SlogBuffer slogBuffer = new SlogBuffer(SlogContext.getInstance())) {
            Slog.info("Hello World");

            slogBuffer.waitSlogQueue();
            SlogBufferData data = slogBuffer.flush();

            assertEquals(1, data.getRecords().size());
            assertEquals(SlogSeverity.INFO, data.getRecords().get(0).getSeverity());
            assertEquals("Hello World", data.getRecords().get(0).getFlatText());
        }
    }

    @Test
    public void testScope() {
        try (SlogBuffer slogBuffer = new SlogBuffer(SlogContext.getInstance())) {
            try (SlogScope scope = Slog.scope("test_scope")) {
                Slog.info("inside scope");
            }

            slogBuffer.waitSlogQueue();
            SlogBufferData data = slogBuffer.flush();

            // 3 records: scope-open, info, scope-close
            assertEquals(3, data.getRecords().size());
            assertEquals(".scope_name",
                    data.getRecords().get(0).getTags().get(0).getKey());
            assertEquals("test_scope",
                    data.getRecords().get(0).getTags().get(0).getValueString());
        }
    }
}
