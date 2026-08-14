/* Copyright 2026 Sudhir Mishra. SPDX-License-Identifier: Apache-2.0 */
package dev.sudhirmuse.logminer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LogPatternMiner {
    private final LogNormalizer normalizer = new LogNormalizer();

    public List<PatternSummary> mine(String input, MinerOptions options) {
        Map<String, MutableSummary> grouped = new LinkedHashMap<>();
        for (String event : splitEvents(input)) {
            String pattern = normalizer.normalize(event, options.stripTimestamps());
            if (!pattern.isBlank()) {
                grouped.computeIfAbsent(pattern, ignored -> new MutableSummary(event.strip())).count++;
            }
        }
        return grouped.entrySet().stream()
            .map(entry -> new PatternSummary(entry.getKey(), entry.getValue().count, entry.getValue().example))
            .filter(summary -> summary.count() >= options.minimumCount())
            .sorted((left, right) -> {
                int byCount = Long.compare(right.count(), left.count());
                return byCount != 0 ? byCount : left.pattern().compareTo(right.pattern());
            })
            .limit(options.limit())
            .toList();
    }

    private List<String> splitEvents(String input) {
        if (input == null || input.isBlank()) return List.of();
        List<String> events = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : input.replace("\r\n", "\n").replace('\r', '\n').split("\n")) {
            boolean continuation = line.isBlank() || Character.isWhitespace(line.charAt(0))
                || line.startsWith("Caused by:") || line.startsWith("Suppressed:");
            if (!continuation && !current.isEmpty()) {
                events.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) current.append('\n');
            current.append(line);
        }
        if (!current.isEmpty()) events.add(current.toString());
        return events;
    }

    private static final class MutableSummary {
        private final String example;
        private long count;
        private MutableSummary(String example) { this.example = example; }
    }
}
