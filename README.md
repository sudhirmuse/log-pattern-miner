# Log Pattern Miner

A dependency-free Java 21 CLI that turns noisy logs into ranked, normalized event patterns.

It replaces volatile values such as timestamps, UUIDs, IP addresses, hexadecimal identifiers, and numbers before grouping messages. This makes recurring failures visible even when every line contains different request data.

## Build

```bash
./gradlew build
```

## Usage

```bash
java -jar build/libs/log-pattern-miner.jar application.log
cat application.log | java -jar build/libs/log-pattern-miner.jar --min-count 2 --limit 20
java -jar build/libs/log-pattern-miner.jar --json application.log
```

Options:

```text
--json             Produce JSON instead of a text table
--min-count N      Hide patterns occurring fewer than N times (default: 1)
--limit N          Return at most N patterns (default: 50)
--keep-timestamps  Do not remove leading timestamps
--help             Show help
```

Multiline Java stack traces are attached to their preceding log event. Stack-frame line numbers are normalized, allowing equivalent exceptions to group together.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).
