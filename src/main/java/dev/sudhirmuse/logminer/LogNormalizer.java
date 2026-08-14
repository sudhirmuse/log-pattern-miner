/* Copyright 2026 Sudhir Mishra. SPDX-License-Identifier: Apache-2.0 */
package dev.sudhirmuse.logminer;

import java.util.regex.Pattern;

final class LogNormalizer {
    private static final Pattern ISO_TIMESTAMP = Pattern.compile("^\\s*\\d{4}-\\d{2}-\\d{2}[T ][0-9:.+-]+(?:Z|[+-]\\d{2}:?\\d{2})?\\s*");
    private static final Pattern BRACKET_TIMESTAMP = Pattern.compile("^\\s*\\[?\\d{2}[:/]\\d{2}[:/]\\d{2,4}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d+)?]?\\s*");
    private static final Pattern UUID = Pattern.compile("(?i)\\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\b");
    private static final Pattern IPV4 = Pattern.compile("(?<![\\d.])(?:\\d{1,3}\\.){3}\\d{1,3}(?![\\d.])");
    private static final Pattern HEX = Pattern.compile("(?i)\\b0x[0-9a-f]+\\b|\\b[0-9a-f]{16,}\\b");
    private static final Pattern FRAME_LINE = Pattern.compile("(\\([^():]+:)(\\d+)(\\))");
    private static final Pattern NUMBER = Pattern.compile("(?<![\\w.])-?\\d+(?:\\.\\d+)?(?![\\w.])");
    private static final Pattern WHITESPACE = Pattern.compile("[ \\t]+");

    String normalize(String event, boolean stripTimestamps) {
        String[] lines = event.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        StringBuilder result = new StringBuilder();
        for (String original : lines) {
            String line = original.replace("\uFEFF", "").replace("\uFFFD", "");
            if (stripTimestamps && result.isEmpty()) {
                line = BRACKET_TIMESTAMP.matcher(ISO_TIMESTAMP.matcher(line).replaceFirst("")).replaceFirst("");
            }
            line = UUID.matcher(line).replaceAll("<uuid>");
            line = IPV4.matcher(line).replaceAll("<ip>");
            line = HEX.matcher(line).replaceAll("<hex>");
            line = FRAME_LINE.matcher(line).replaceAll("$1<line>$3");
            line = NUMBER.matcher(line).replaceAll("<n>");
            line = WHITESPACE.matcher(line).replaceAll(" ").strip();
            if (!line.isEmpty()) {
                if (!result.isEmpty()) result.append('\n');
                result.append(line);
            }
        }
        return result.toString();
    }
}
