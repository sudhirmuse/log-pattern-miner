/* Copyright 2026 Sudhir Mishra. SPDX-License-Identifier: Apache-2.0 */
package dev.sudhirmuse.logminer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LogPatternMinerCli {
    private LogPatternMinerCli() {}

    public static void main(String[] args) {
        int code = run(args);
        if (code != 0) System.exit(code);
    }

    static int run(String[] args) {
        boolean json = false;
        boolean stripTimestamps = true;
        int minimumCount = 1;
        int limit = 50;
        List<String> positional = new ArrayList<>();
        try {
            for (int index = 0; index < args.length; index++) {
                switch (args[index]) {
                    case "--json" -> json = true;
                    case "--keep-timestamps" -> stripTimestamps = false;
                    case "--min-count" -> minimumCount = Integer.parseInt(requireValue(args, ++index, "--min-count"));
                    case "--limit" -> limit = Integer.parseInt(requireValue(args, ++index, "--limit"));
                    case "--help", "-h" -> { printHelp(); return 0; }
                    default -> positional.add(args[index]);
                }
            }
            if (positional.size() > 1) throw new IllegalArgumentException("Expected at most one input file");
            String input = positional.isEmpty()
                ? new String(System.in.readAllBytes(), StandardCharsets.UTF_8)
                : Files.readString(Path.of(positional.getFirst()), StandardCharsets.UTF_8);
            var summaries = new LogPatternMiner().mine(input, new MinerOptions(minimumCount, limit, stripTimestamps));
            System.out.print(json ? toJson(summaries) : toText(summaries));
            return 0;
        } catch (IOException | IllegalArgumentException exception) {
            System.err.println("Unable to mine logs: " + exception.getMessage());
            return 2;
        }
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length) throw new IllegalArgumentException(option + " requires a value");
        return args[index];
    }

    private static String toText(List<PatternSummary> summaries) {
        if (summaries.isEmpty()) return "No patterns found.\n";
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < summaries.size(); index++) {
            PatternSummary item = summaries.get(index);
            result.append(index + 1).append(". count=").append(item.count()).append('\n')
                .append(item.pattern()).append("\n\n");
        }
        return result.toString();
    }

    private static String toJson(List<PatternSummary> summaries) {
        StringBuilder result = new StringBuilder("[\n");
        for (int index = 0; index < summaries.size(); index++) {
            PatternSummary item = summaries.get(index);
            if (index > 0) result.append(",\n");
            result.append("  {\"count\": ").append(item.count())
                .append(", \"pattern\": \"").append(escapeJson(item.pattern())).append("\"}");
        }
        return result.append("\n]\n").toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static void printHelp() {
        System.out.println("Usage: log-pattern-miner [--json] [--min-count N] [--limit N] [--keep-timestamps] [file]");
    }
}
