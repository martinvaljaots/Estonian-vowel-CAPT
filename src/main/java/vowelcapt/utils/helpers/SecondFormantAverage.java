package vowelcapt.utils.helpers;

import com.google.common.util.concurrent.AtomicDouble;

public final class SecondFormantAverage {

    private static AtomicDouble secondFormantAverage = new AtomicDouble(0.0);

    public static void set(double newSecondFormantAverage) {
        secondFormantAverage.set(newSecondFormantAverage);
    }

    public static double get() {
        return secondFormantAverage.get();
    }
}
