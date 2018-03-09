package vowelcapt.helpers;

import java.util.concurrent.atomic.AtomicBoolean;

public final class IsRecording {

    private static final AtomicBoolean flag = new AtomicBoolean(false);

    public static void set(boolean value) {
        flag.set(value);
    }

    public static boolean get() {
        return flag.get();
    }
}
