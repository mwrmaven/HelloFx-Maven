package org.example.interfaces;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Test {
    public static void main(String[] args) {
        ServiceLoader<Function> loader = ServiceLoader.load(Function.class);
        Iterator<Function> iterator = loader.iterator();
        if (iterator.hasNext()) {
            Function next = iterator.next();
            System.out.println(next.tabName());
        }
    }
}
