package edu.stanford.cs276.util;


public class Assert {

    public static void check(boolean expression, String ... failureMessage) {

        StringBuilder sb = new StringBuilder();

        for (String s : failureMessage) {
            sb.append(s);
        }

        if (!expression) {
            throw new RuntimeException(sb.toString());
        }
    }

}
