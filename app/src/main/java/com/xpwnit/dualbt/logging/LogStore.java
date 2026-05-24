package com.xpwnit.dualbt.logging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class LogStore {
    private final int maxEntries;
    private final ArrayList<Entry> entries = new ArrayList<>();

    public LogStore(int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
    }

    public synchronized Entry add(String level, String tag, String message) {
        Entry entry = new Entry(System.currentTimeMillis(), level, tag, message);
        entries.add(0, entry);
        while (entries.size() > maxEntries) {
            entries.remove(entries.size() - 1);
        }
        return entry;
    }

    public synchronized List<Entry> all() {
        return new ArrayList<>(entries);
    }

    public synchronized List<Entry> filter(String level) {
        if (level == null || level.equals("ALL")) {
            return all();
        }
        ArrayList<Entry> filtered = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.level.equals(level)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public synchronized void clear() {
        entries.clear();
    }

    public static final class Entry {
        private static final SimpleDateFormat FORMAT =
                new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

        public final long timestamp;
        public final String level;
        public final String tag;
        public final String message;

        Entry(long timestamp, String level, String tag, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.tag = tag;
            this.message = message;
        }

        public String formattedTime() {
            return FORMAT.format(new Date(timestamp));
        }
    }
}
