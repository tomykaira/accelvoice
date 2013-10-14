package io.github.tomykaira.accelvoice.selector

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary

public interface RecognizerLibrary extends Library {
    RecognizerLibrary INSTANCE = RecognizerLibrary.LoadHelper.loadLibrary()

    void start(int argc, String []argv);
    void stop();
    int recognize(String[][] candidates, String[] unknown);

    private class LoadHelper {
        static RecognizerLibrary loadLibrary() {
            def path = System.getProperty("java.class.path")
                    .split(":")
                    .find { it.contains("libaccel_recognizer.so") }
                    .replace("/libaccel_recognizer.so", "")
            NativeLibrary.addSearchPath("accel_recognizer", path)
            Native.loadLibrary("accel_recognizer", RecognizerLibrary.class) as RecognizerLibrary;
        }
    }
}
