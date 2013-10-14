package io.github.tomykaira.accelvoice.selector;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;

import java.util.ArrayList;
import java.util.List;

public class SplitWordList implements NativeMapped {

    private List<List<String>> data;

    public SplitWordList() {
        this(new ArrayList<List<String>>());
    }

    public SplitWordList(List<List<String>> data) {
        this.data = data;
    }

    @Override
    public Object fromNative(Object o, FromNativeContext fromNativeContext) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Object toNative() {
        Pointer[] pointers = new Pointer[data.size() + 1];
        for (int i = data.size() - 1; i >= 0; --i) {
            List<String> list = data.get(i);
            StringArray stringArray = new StringArray(list.toArray(new String[list.size()]));
            pointers[i] = stringArray;
        }
        pointers[data.size()] = Pointer.NULL;
        return pointers;
    }

    @Override
    public Class nativeType() {
        return Pointer[].class;
    }

    public List<List<String>> getData() {
        return data;
    }
}
