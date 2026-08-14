/* Copyright 2026 Sudhir Mishra. SPDX-License-Identifier: Apache-2.0 */
package dev.sudhirmuse.logminer;

public record MinerOptions(int minimumCount, int limit, boolean stripTimestamps) {
    public MinerOptions {
        if (minimumCount < 1) throw new IllegalArgumentException("minimumCount must be at least 1");
        if (limit < 1) throw new IllegalArgumentException("limit must be at least 1");
    }

    public static MinerOptions defaults() {
        return new MinerOptions(1, 50, true);
    }
}
