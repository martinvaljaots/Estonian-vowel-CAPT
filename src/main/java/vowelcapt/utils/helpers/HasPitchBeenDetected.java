package vowelcapt.utils.helpers;

import java.util.concurrent.atomic.AtomicBoolean;

public final class HasPitchBeenDetected {

    private static final AtomicBoolean hasPitchBeenDetectedFlag = new AtomicBoolean(false);

    public static void set(boolean hasPitchBeenDetected) {
        hasPitchBeenDetectedFlag.set(hasPitchBeenDetected);
    }

    public static boolean get() {
        return hasPitchBeenDetectedFlag.get();
    }
}
