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

public final class SlogTag {
    public enum ValueType { NONE, STRING, INT, DOUBLE }

    private final String key;
    private final String valueString;
    private final long valueInt;
    private final double valueDouble;
    private final ValueType valueType;

    SlogTag(String key, String valueString, long valueInt, double valueDouble,
            int valueTypeOrdinal) {
        this.key = key;
        this.valueString = valueString;
        this.valueInt = valueInt;
        this.valueDouble = valueDouble;
        this.valueType = ValueType.values()[valueTypeOrdinal];
    }

    public String getKey() { return key; }
    public String getValueString() { return valueString; }
    public long getValueInt() { return valueInt; }
    public double getValueDouble() { return valueDouble; }
    public ValueType getValueType() { return valueType; }
}
