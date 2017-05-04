package edu.stanford.cs276.util;


public class Logger {

    private Logger() { /* private constructor to defeat instantiation */ }

    // WARNING: this flag must always be set to false before submitting
    private static boolean DEBUG_FLAG = true;

    /**
     * Utility method that can be used throughout the application to print messages to standard output.
     * The first argument must be true to enable the printing of the message in question. This allows a finer level of
     * control of the individual print statements, so that we can disable a print statement simply by passing false
     * instead of true. This saves us trouble of commenting (out) calls to this method;
     *
     * @param doPrint whether or not to print the passed message to stdout
     * @param objects objects to print to stdout
     */
    public static void print(boolean doPrint, Object... objects) {

        if ( DEBUG_FLAG && !doPrint) return;

        for (Object o : objects) {
            System.out.print(o + " ");
        }

        System.out.println();
    }

}
