package com.xpwnit.dualbt.state;

import java.util.Arrays;

public final class StreamRoutePlanTest {
    public static void main(String[] args) {
        StreamDevice first = new StreamDevice("Kitchen Speaker", "Bonded Bluetooth loudspeaker", "AA:BB:CC:00:01");
        StreamDevice second = new StreamDevice("Desk Speaker", "Bonded Bluetooth hi-fi audio", "AA:BB:CC:00:02");

        StreamRoutePlan plan = StreamRoutePlan.fromSelected(Arrays.asList(first, second), 2);
        assertEquals(2, plan.targetCount(), "target count");
        assertEquals("Kitchen Speaker, Desk Speaker", plan.displayNames(), "display names");

        String payload = plan.toPayload();
        assertContains(payload, "Kitchen+Speaker", "payload contains encoded first name");
        assertContains(payload, "AA%3ABB%3ACC%3A00%3A02", "payload encodes address punctuation");

        StreamRoutePlan parsed = StreamRoutePlan.fromPayload(payload, 2);
        assertEquals(2, parsed.targetCount(), "parsed target count");
        assertEquals("AA:BB:CC:00:01", parsed.targets().get(0).address, "first parsed address");
        assertEquals("Desk Speaker", parsed.targets().get(1).name, "second parsed name");

        assertThrows(
                () -> StreamRoutePlan.fromSelected(Arrays.asList(first), 2),
                "incomplete selected route plan is rejected"
        );
        assertThrows(
                () -> StreamRoutePlan.fromPayload("Kitchen Speaker|Route|AA:BB:CC:00:01", 2),
                "incomplete payload route plan is rejected"
        );
    }

    private static void assertContains(String value, String expected, String message) {
        if (!value.contains(expected)) {
            throw new AssertionError(message + ": expected " + value + " to contain " + expected);
        }
    }

    private static void assertThrows(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
