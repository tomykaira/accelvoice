package io.github.tomykaira.accelvoice.selector;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public interface RecognizerLibrary extends com.sun.jna.Library {
    public void start(int argc, java.lang.String[] argv, String logFile);

    public void stop();

    interface cb_vader_start extends Callback {
        void invoke(long timestamp);
    }
    interface cb_vader_stop extends Callback {
        void invoke(long timestamp);
    }
    interface cb_recognized extends Callback {
        void invoke(int index);
    }

    void register_cb_vader_start(cb_vader_start cb);
    void register_cb_vader_stop(cb_vader_stop cb);
    void register_cb_recognized(cb_recognized cb);

    public int start_recognition(SplitWordList candidates, java.lang.String[] unknown);

    public void abort_recognition();

    public static final RecognizerLibrary INSTANCE =
            RecognizerLibrary.Loader.loadLibrary();

    static class Loader {
        static RecognizerLibrary loadLibrary() {
            String[] paths = System.getProperty("java.class.path").split(":");
            for (String p : paths) {
                if (!p.contains("/libaccel_recognizer.so"))
                    continue;
                String path = p.replace("/libaccel_recognizer.so", "");
                NativeLibrary.addSearchPath("accel_recognizer", path);
            }
            return (RecognizerLibrary) Native.loadLibrary("accel_recognizer", RecognizerLibrary.class);
        }
    }
}
