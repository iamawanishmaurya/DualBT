package com.xpwnit.dualbt.state;

import java.util.Arrays;
import java.util.List;

public final class StreamSessionStateCodecTest {
    public static void main(String[] args) {
        encodesSelectedAddressesInOrder();
        decodesSavedAddressesInOrder();
        dropsBlankAndDuplicateAddresses();
    }

    private static void encodesSelectedAddressesInOrder() {
        String encoded = StreamSessionStateCodec.encodeSelectedAddresses(Arrays.asList(
                new StreamDevice("Mini boost 1", "Route", "41:42:26:B3:62:1C"),
                new StreamDevice("Mini boost 2", "Route", "41:42:2E:9E:5E:AE")
        ));

        assertEquals(
                "41:42:26:B3:62:1C\n41:42:2E:9E:5E:AE",
                encoded,
                "selected addresses should persist in button order"
        );
    }

    private static void decodesSavedAddressesInOrder() {
        List<String> addresses = StreamSessionStateCodec.decodeSelectedAddresses("AA:01\nBB:02");

        assertEquals(2, addresses.size(), "decoded address count");
        assertEquals("AA:01", addresses.get(0), "first decoded address");
        assertEquals("BB:02", addresses.get(1), "second decoded address");
    }

    private static void dropsBlankAndDuplicateAddresses() {
        List<String> addresses = StreamSessionStateCodec.decodeSelectedAddresses("\nAA:01\nAA:01\n  \nBB:02\n");

        assertEquals(2, addresses.size(), "decoded unique address count");
        assertEquals("AA:01", addresses.get(0), "first unique address");
        assertEquals("BB:02", addresses.get(1), "second unique address");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
