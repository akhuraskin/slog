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

import java.util.Map;
import java.util.TreeMap;

// Methods starting with `_` are private. Others are public and supposed to
// be used only by external users. To ensure call stack is parsed properly,
// public methods should not call each other, they rather should call only
// private methods.

public final class Slog {
    private Slog() {}

    public static Map<String, Object> tags(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "tags() requires an even number of arguments");
        }
        TreeMap<String, Object> map = new TreeMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return map;
    }

    // Stack trace layout for _log:
    //   [0] getStackTrace
    //   [1] _log
    //   [2] info/warning/error
    //   [3] caller
    private static final int LOG_CALLER_STACK_DEPTH = 3;

    private static void _log(byte severity, String message,
                             Map<String, Object> tags) {
        StackTraceElement frame =
                Thread.currentThread().getStackTrace()[LOG_CALLER_STACK_DEPTH];
        int callSiteId = SlogNative.addOrReuseCallSiteVerySlow(
                frame.getMethodName(), frame.getFileName(),
                frame.getLineNumber());

        long eventPtr = SlogNative.eventCreate(severity, callSiteId);
        try {
            if (message != null) {
                SlogNative.eventEmitValue(eventPtr, message);
            }
            if (tags != null) {
                for (Map.Entry<String, Object> entry : tags.entrySet()) {
                    _addTag(eventPtr, entry.getKey(), entry.getValue());
                }
            }
        } finally {
            SlogNative.eventDestroy(eventPtr);
        }
    }

    private static void _addTag(long eventPtr, String key, Object value) {
        if (value instanceof String) {
            SlogNative.eventAddTagString(eventPtr, key, (String) value);
        } else if (value instanceof Long) {
            SlogNative.eventAddTagLong(eventPtr, key, (Long) value);
        } else if (value instanceof Integer) {
            SlogNative.eventAddTagLong(eventPtr, key,
                    ((Integer) value).longValue());
        } else if (value instanceof Double) {
            SlogNative.eventAddTagDouble(eventPtr, key, (Double) value);
        } else if (value instanceof Float) {
            SlogNative.eventAddTagDouble(eventPtr, key,
                    ((Float) value).doubleValue());
        } else {
            SlogNative.eventAddTagString(eventPtr, key, String.valueOf(value));
        }
    }

    public static void info(String message) {
        _log(SlogSeverity.INFO, message, null);
    }

    public static void info(String message, Map<String, Object> tags) {
        _log(SlogSeverity.INFO, message, tags);
    }

    public static void info(Map<String, Object> tags) {
        _log(SlogSeverity.INFO, null, tags);
    }

    public static void warning(String message) {
        _log(SlogSeverity.WARNING, message, null);
    }

    public static void warning(String message, Map<String, Object> tags) {
        _log(SlogSeverity.WARNING, message, tags);
    }

    public static void warning(Map<String, Object> tags) {
        _log(SlogSeverity.WARNING, null, tags);
    }

    public static void error(String message) {
        _log(SlogSeverity.ERROR, message, null);
    }

    public static void error(String message, Map<String, Object> tags) {
        _log(SlogSeverity.ERROR, message, tags);
    }

    public static void error(Map<String, Object> tags) {
        _log(SlogSeverity.ERROR, null, tags);
    }

    // Stack trace layout for _scope:
    //   [0] getStackTrace
    //   [1] _scope
    //   [2] scope (public)
    //   [3] caller
    private static final int SCOPE_CALLER_STACK_DEPTH = 3;

    private static SlogScope _scope(String scopeName, Map<String, Object> tags) {
        StackTraceElement frame =
                Thread.currentThread().getStackTrace()[SCOPE_CALLER_STACK_DEPTH];
        int callSiteId = SlogNative.addOrReuseCallSiteVerySlow(
                frame.getMethodName(), frame.getFileName(),
                frame.getLineNumber());

        TreeMap<String, Object> mergedTags = new TreeMap<>();
        if (tags != null) {
            mergedTags.putAll(tags);
        }
        mergedTags.put(SlogSeverity.TAG_KEY_SCOPE_NAME, scopeName);

        SlogEvent event = new SlogEvent(SlogSeverity.INFO, callSiteId);
        for (Map.Entry<String, Object> entry : mergedTags.entrySet()) {
            _addTagToEvent(event, entry.getKey(), entry.getValue());
        }

        return new SlogScope(event);
    }

    private static void _addTagToEvent(SlogEvent event, String key,
                                        Object value) {
        if (value instanceof String) {
            event.addTag(key, (String) value);
        } else if (value instanceof Long) {
            event.addTag(key, (Long) value);
        } else if (value instanceof Integer) {
            event.addTag(key, ((Integer) value).longValue());
        } else if (value instanceof Double) {
            event.addTag(key, (Double) value);
        } else if (value instanceof Float) {
            event.addTag(key, ((Float) value).doubleValue());
        } else {
            event.addTag(key, String.valueOf(value));
        }
    }

    public static SlogScope scope(String scopeName) {
        return _scope(scopeName, null);
    }

    public static SlogScope scope(String scopeName, Map<String, Object> tags) {
        return _scope(scopeName, tags);
    }
}
