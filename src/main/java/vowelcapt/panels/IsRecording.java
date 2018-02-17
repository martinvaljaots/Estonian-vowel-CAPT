package vowelcapt.panels;

import java.util.concurrent.atomic.AtomicBoolean;

public final class IsRecording {
    private static final AtomicBoolean recording = new AtomicBoolean(false);

    public static boolean get() {
        return recording.get();
    }

    public static void set(boolean value) {
        recording.set(value);
    }
}
