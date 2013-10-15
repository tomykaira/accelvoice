package io.github.tomykaira.accelvoice.selector;

import groovy.lang.Closure;

public class CallbackWrapper {
    static RecognizerLibrary.cb_recognized callbackRecognized(final Closure body) {
        return new RecognizerLibrary.cb_recognized() {
            @Override
            public void invoke(int index) {
                body.call(index);
            }
        };
    }
}
