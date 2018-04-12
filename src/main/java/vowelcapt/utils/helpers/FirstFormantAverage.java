package vowelcapt.utils.helpers;

import com.google.common.util.concurrent.AtomicDouble;

public final class FirstFormantAverage {

    private static AtomicDouble firstFormantAverage = new AtomicDouble(0.0);

    public static void set(double newFirstFormantAverage) {
        firstFormantAverage.set(newFirstFormantAverage);
    }

    public static double get() {
        return firstFormantAverage.get();
    }
}
