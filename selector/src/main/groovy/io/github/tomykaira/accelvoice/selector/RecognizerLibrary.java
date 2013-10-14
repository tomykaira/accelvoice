package io.github.tomykaira.accelvoice.selector;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public interface RecognizerLibrary extends com.sun.jna.Library {
    public void start(int argc, java.lang.String[] argv);

    public void stop();

    public int recognize(SplitWordList candidates, java.lang.String[] unknown);

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
