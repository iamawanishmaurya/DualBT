package com.xpwnit.dualbt.logging;

import java.io.File;
import java.util.List;

public final class FileLogSinkTest {
    public static void main(String[] args) {
        File dir = new File("/tmp/dualbt-file-log-test-0956");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new AssertionError("Unable to create temp log test directory");
        }
        File file = new File(dir, "dualbt.log");
        FileLogSink sink = new FileLogSink(file, 140);
        LogStore store = new LogStore(10);

        sink.clear();
        sink.append(store.add("INFO", "Test", "first message"));
        sink.append(store.add("WARN", "Test", "second line\nwrapped"));

        List<String> lines = sink.readLines();
        assertEquals(2, lines.size(), "two entries are persisted");
        assertContains(lines.get(0), "\tINFO\tTest\tfirst message", "first line content");
        assertContains(lines.get(1), "\tWARN\tTest\tsecond line\\nwrapped", "escaped newline content");

        sink.append(store.add("ERROR", "Test", "this long message forces rotation because the file limit is intentionally tiny"));
        List<String> rotated = sink.readLines();
        assertEquals(1, rotated.size(), "rotation keeps latest entry only");
        assertContains(rotated.get(0), "\tERROR\tTest\t", "rotated line content");

        sink.clear();
        assertEquals(0, sink.readLines().size(), "clear removes persisted lines");
    }

    private static void assertContains(String value, String expected, String message) {
        if (!value.contains(expected)) {
            throw new AssertionError(message + ": expected line to contain " + expected + " but got " + value);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
