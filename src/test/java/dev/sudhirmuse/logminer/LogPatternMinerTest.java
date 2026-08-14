/* Copyright 2026 Sudhir Mishra. SPDX-License-Identifier: Apache-2.0 */
package dev.sudhirmuse.logminer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogPatternMinerTest {
    private final LogPatternMiner miner = new LogPatternMiner();

    @Test
    void groupsMessagesWithDifferentVolatileValues() {
        String logs = """
            2026-08-14T10:00:00Z ERROR Request 550e8400-e29b-41d4-a716-446655440000 failed after 120 ms from 10.2.3.4
            2026-08-14T10:01:00Z ERROR Request 6ba7b810-9dad-41d1-80b4-00c04fd430c8 failed after 450 ms from 192.168.1.9
            2026-08-14T10:02:00Z INFO Started worker 7
            """;

        var result = miner.mine(logs, MinerOptions.defaults());

        assertEquals(2, result.size());
        assertEquals(2, result.getFirst().count());
        assertEquals("ERROR Request <uuid> failed after <n> ms from <ip>", result.getFirst().pattern());
    }

    @Test
    void groupsMultilineStackTracesWithDifferentLineNumbers() {
        String logs = """
            ERROR Checkout failed
                at com.acme.Checkout.pay(Checkout.java:42)
                at java.base.Thread.run(Thread.java:100)
            ERROR Checkout failed
                at com.acme.Checkout.pay(Checkout.java:98)
                at java.base.Thread.run(Thread.java:200)
            """;

        var result = miner.mine(logs, new MinerOptions(2, 10, true));

        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().count());
        assertTrue(result.getFirst().pattern().contains("Checkout.java:<line>"));
    }

    @Test
    void honorsMinimumCountAndLimit() {
        String logs = "warn disk 1\nwarn disk 2\nerror once";
        var result = miner.mine(logs, new MinerOptions(2, 1, true));
        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().count());
    }

    @Test
    void ignoresByteOrderMarksBeforeTimestamps() {
        String logs = "\uFEFF2026-08-14T10:00:00Z ERROR failed 10\n"
            + "2026-08-14T10:01:00Z ERROR failed 20";
        var result = miner.mine(logs, MinerOptions.defaults());
        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().count());
    }
}
