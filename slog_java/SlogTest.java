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

// This test is running various scenarios for Slog in Java. It mirrors the
// Python test in slog_py/slog_test.py. It is not easy to check what is printed
// to stdout via underlying C++ code so these tests are not checking correctness
// of the printed/published logs, that should be checked manually. But this test
// may catch typos and missing implementations in the code automatically.

package com.woven.slog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SlogTest {

    @Test
    public void testSeverities() {
        try (SlogBuffer slogBuffer = new SlogBuffer(SlogContext.getInstance())) {
            Slog.info("Hi INFO");
            Slog.warning("Hi WARNING");
            Slog.error("Hi ERROR");

            slogBuffer.waitSlogQueue();
            SlogBufferData data = slogBuffer.flush();

            assertEquals(3, data.getRecords().size());
            assertEquals(SlogSeverity.INFO, data.getRecords().get(0).getSeverity());
            assertEquals("Hi INFO", data.getRecords().get(0).getFlatText());
            assertEquals(SlogSeverity.WARNING, data.getRecords().get(1).getSeverity());
            assertEquals("Hi WARNING", data.getRecords().get(1).getFlatText());
            assertEquals(SlogSeverity.ERROR, data.getRecords().get(2).getSeverity());
            assertEquals("Hi ERROR", data.getRecords().get(2).getFlatText());
        }
    }

    @Test
    public void testSilentNoisy() {
        try (SlogBuffer slogBuffer = new SlogBuffer(SlogContext.getInstance())) {
            Slog.info("some noise", Slog.tags("is_noisy", 1L));
            Slog.info(Slog.tags("is_noisy", 0L));

            slogBuffer.waitSlogQueue();
            SlogBufferData data = slogBuffer.flush();

            assertEquals(2, data.getRecords().size());
            SlogRecord record0 = data.getRecords().get(0);
            assertEquals(2, record0.getTags().size());
            assertEquals("_t0_s", record0.getTags().get(0).getKey());
            assertEquals("some noise", record0.getTags().get(0).getValueString());
            assertEquals("is_noisy", record0.getTags().get(1).getKey());
            assertEquals(1L, record0.getTags().get(1).getValueInt());

            SlogRecord record1 = data.getRecords().get(1);
            assertEquals(1, record1.getTags().size());
            assertEquals("is_noisy", record1.getTags().get(0).getKey());
            assertEquals(0L, record1.getTags().get(0).getValueInt());
        }
    }

    @Test
    public void testCallSite() {
        try (SlogBuffer slogBuffer = new SlogBuffer(SlogContext.getInstance())) {
            Slog.info(Slog.tags("foo", "bar"));

            slogBuffer.waitSlogQueue();
            SlogBufferData data = slogBuffer.flush();

            assertEquals(1, data.getRecords().size());
            SlogRecord record0 = data.getRecords().get(0);
            SlogCallSite callSite =
                    data.getCallSites().get(record0.getCallSiteId());
            assertTrue(callSite.getFile().endsWith("SlogTest.java"));
            assertEquals("testCallSite", callSite.getFunction());
            assertEquals(77, callSite.getLine());
        }
    }

    @Test
    public void testScope() {
        try (SlogBuffer slogBuffer = new SlogBuffer(SlogContext.getInstance())) {
            try (SlogScope scope = Slog.scope("foo_scope",
                    Slog.tags("foo", "bar"))) {
                Slog.info(Slog.tags("foo", 1.5));
            }

            slogBuffer.waitSlogQueue();
            SlogBufferData data = slogBuffer.flush();

            assertEquals(3, data.getRecords().size());

            SlogRecord record0 = data.getRecords().get(0);
            SlogCallSite callSite =
                    data.getCallSites().get(record0.getCallSiteId());
            assertTrue(callSite.getFile().endsWith("SlogTest.java"));
            assertEquals("testScope", callSite.getFunction());
            // Find the "foo" tag by key to avoid depending on internal tag ordering.
            String fooValue = null;
            for (SlogTag tag : record0.getTags()) {
                if ("foo".equals(tag.getKey())) {
                    fooValue = tag.getValueString();
                }
            }
            assertEquals("bar", fooValue);
            assertEquals(".scope_name", record0.getTags().get(0).getKey());
            assertEquals("foo_scope", record0.getTags().get(0).getValueString());

            SlogRecord record1 = data.getRecords().get(1);
            assertEquals(1, record1.getTags().size());
            assertEquals("foo", record1.getTags().get(0).getKey());
            assertEquals(1.5, record1.getTags().get(0).getValueDouble(), 0.001);

            SlogRecord record2 = data.getRecords().get(2);
            assertEquals(0, record2.getCallSiteId());
            assertEquals(2, record2.getTags().size());
        }
    }

    @Test
    public void testScopeWithException() {
        try (SlogBuffer slogBuffer = new SlogBuffer(SlogContext.getInstance())) {
            try {
                try (SlogScope scope = Slog.scope("fooz_scope")) {
                    Slog.info("before exception");
                    if (true) throw new RuntimeException("foo-error");
                    Slog.info("after exception");
                }
            } catch (RuntimeException e) {
                // Exception caught; scope was closed by try-with-resources.
            }

            slogBuffer.waitSlogQueue();
            SlogBufferData data = slogBuffer.flush();

            // 3 records: scope-open, "before exception", scope-close.
            // "after exception" was never reached due to the throw.
            assertEquals(3, data.getRecords().size());

            SlogRecord record0 = data.getRecords().get(0);
            SlogCallSite callSite =
                    data.getCallSites().get(record0.getCallSiteId());
            assertTrue(callSite.getFile().endsWith("SlogTest.java"));
            assertEquals("testScopeWithException", callSite.getFunction());
            assertEquals(4, record0.getTags().size());
            assertEquals(".scope_name", record0.getTags().get(0).getKey());
            assertEquals("fooz_scope", record0.getTags().get(0).getValueString());

            SlogRecord record1 = data.getRecords().get(1);
            assertEquals("before exception", record1.getFlatText());

            SlogRecord record2 = data.getRecords().get(2);
            assertEquals(0, record2.getCallSiteId());
            assertEquals(2, record2.getTags().size());
        }
    }
}
