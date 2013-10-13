package io.github.tomykaira.accelvoice.selector

import com.sun.jna.Library
import com.sun.jna.Native

public interface RecognizerLibrary extends Library {
    RecognizerLibrary INSTANCE = (RecognizerLibrary) Native.loadLibrary("recognizer", RecognizerLibrary.class);

    void start(int argc, String []argv);
    void stop();
    String recognize(String[] candidates);
}
