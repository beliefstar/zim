package org.zim.common;

public class Assert {

    public static void check(boolean action) {
        if (!action) {
            throw new AssertException();
        }
    }

    public static class AssertException extends RuntimeException {}
}
