package com.xpwnit.dualbt.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class FileLogSink {
    private final File file;
    private final int maxBytes;

    public FileLogSink(File file, int maxBytes) {
        this.file = file;
        this.maxBytes = Math.max(1, maxBytes);
    }

    public synchronized void append(LogStore.Entry entry) {
        ensureParent();
        byte[] line = (format(entry) + "\n").getBytes(StandardCharsets.UTF_8);
        if (file.exists() && file.length() + line.length > maxBytes) {
            clear();
        }
        try (FileOutputStream output = new FileOutputStream(file, true)) {
            output.write(line);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to append log file", exception);
        }
    }

    public synchronized List<String> readLines() {
        ArrayList<String> lines = new ArrayList<>();
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read log file", exception);
        }
    }

    public synchronized void clear() {
        ensureParent();
        try (FileOutputStream ignored = new FileOutputStream(file, false)) {
            // Truncate the log file.
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to clear log file", exception);
        }
    }

    public File file() {
        return file;
    }

    private void ensureParent() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Unable to create log directory: " + parent.getAbsolutePath());
        }
    }

    private String format(LogStore.Entry entry) {
        return entry.timestamp
                + "\t" + escape(entry.level)
                + "\t" + escape(entry.tag)
                + "\t" + escape(entry.message);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
