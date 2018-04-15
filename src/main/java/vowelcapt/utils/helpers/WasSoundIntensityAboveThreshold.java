package vowelcapt.utils.helpers;

import java.util.concurrent.atomic.AtomicBoolean;

public final class WasSoundIntensityAboveThreshold {

    private static final AtomicBoolean wasSoundIntensityAboveThreshold = new AtomicBoolean(false);

    public static void set(boolean wasSoundIntensityAboveThreshold) {
        WasSoundIntensityAboveThreshold.wasSoundIntensityAboveThreshold.set(wasSoundIntensityAboveThreshold);
    }

    public static boolean get() {
        return wasSoundIntensityAboveThreshold.get();
    }
}
