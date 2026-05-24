package com.xpwnit.dualbt.state;

public final class StreamSessionControllerTest {
    public static void main(String[] args) {
        StreamDevice one = new StreamDevice("Speaker One", "Route", "01");
        StreamDevice two = new StreamDevice("Speaker Two", "Route", "02");
        StreamDevice three = new StreamDevice("Speaker Three", "Route", "03");
        StreamSessionController controller = new StreamSessionController(2);

        assertEquals("Select 2 speakers", controller.statusMessage(), "initial status");
        assertTrue(controller.toggle(one), "first device can be selected");
        assertEquals("Select 1 more speaker", controller.statusMessage(), "single selection status");
        assertEquals(StreamSessionController.StartResult.NEED_TWO_DEVICES, controller.start(), "one device cannot stream");

        assertTrue(controller.toggle(two), "second device can be selected");
        assertEquals("Ready to stream", controller.statusMessage(), "two selection status");
        assertFalse(controller.toggle(three), "third device is rejected");
        assertEquals(2, controller.selectedCount(), "selection remains capped at two");

        assertEquals(
                StreamSessionController.StartResult.CAPTURE_PERMISSION_REQUIRED,
                controller.start(),
                "two devices request capture permission before streaming"
        );
        assertTrue(controller.isAwaitingCapturePermission(), "controller waits for capture permission");
        assertEquals("Waiting for capture permission", controller.statusMessage(), "capture permission status");
        assertFalse(controller.toggle(one), "selection is locked while permission is pending");
        assertEquals(
                StreamSessionController.StartResult.ALREADY_WAITING_FOR_PERMISSION,
                controller.start(),
                "repeated start is ignored while permission is pending"
        );

        assertTrue(controller.cancelCapturePermission(), "capture denial returns to ready state");
        assertEquals("Ready to stream", controller.statusMessage(), "denial returns to ready status");
        assertEquals(
                StreamSessionController.StartResult.CAPTURE_PERMISSION_REQUIRED,
                controller.start(),
                "start can be requested again after denial"
        );
        assertTrue(controller.confirmCapturePermission(), "capture grant starts streaming");
        assertEquals("Streaming to 2 speaker(s)", controller.statusMessage(), "streaming status");
        assertFalse(controller.toggle(one), "selection is locked while streaming");
        assertEquals(2, controller.selectedCount(), "streaming keeps both devices selected");

        controller.stop();
        assertEquals("Ready to stream", controller.statusMessage(), "stop returns to ready status");
        assertTrue(controller.toggle(one), "device can be deselected after stop");
        assertEquals("Select 1 more speaker", controller.statusMessage(), "post-stop deselection status");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
