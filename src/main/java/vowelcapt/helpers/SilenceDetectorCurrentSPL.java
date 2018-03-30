package vowelcapt.helpers;

import com.google.common.util.concurrent.AtomicDouble;

public final class SilenceDetectorCurrentSPL {

    private static AtomicDouble currentSPL = new AtomicDouble(0.0);

    public static void set(double newCurrentSPL) {
        currentSPL.set(newCurrentSPL);
    }

    public static double get() {
        return currentSPL.get();
    }
}
