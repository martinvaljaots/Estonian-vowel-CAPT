package vowelcapt.helpers;

import java.util.concurrent.atomic.AtomicBoolean;

public final class IsRecording {

    private static final AtomicBoolean isRecordingFlag = new AtomicBoolean(false);

    public static void set(boolean isRecording) {
        isRecordingFlag.set(isRecording);
    }

    public static boolean get() {
        return isRecordingFlag.get();
    }
}
