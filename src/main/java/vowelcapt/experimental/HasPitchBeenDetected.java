package vowelcapt.experimental;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe class for keeping a boolean value for whether the application had detected pitch.
 * Was moved to this package because pitch detection ended up not being used in the application.
 */

public final class HasPitchBeenDetected {

    private static final AtomicBoolean hasPitchBeenDetectedFlag = new AtomicBoolean(false);

    public static void set(boolean hasPitchBeenDetected) {
        hasPitchBeenDetectedFlag.set(hasPitchBeenDetected);
    }

    public static boolean get() {
        return hasPitchBeenDetectedFlag.get();
    }
}
