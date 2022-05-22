/*
A class for logs and error reports.
For debug use.
 */

package com.ryanzhang.panic;

import java.lang.String;
import java.lang.System;

public class panic {

    //whether debug, log and error messages will be printed out respectively.
    private static final boolean DEBUG_MODE = false;
    private static final boolean LOG_MODE = false;
    private static final boolean ERROR_MODE = true;

    public static void fatalError(String msg) {
        System.out.println("FATAL ERROR: "+msg);
        System.exit(1);
    }

    public static void error(String msg) {
        if(!ERROR_MODE) return;
        System.out.println("Error: "+msg);
    }

    public static void log(String msg, String tag) {
        if(!LOG_MODE) return;
        if(tag.isEmpty()) tag = "Untagged Log";
        System.out.println("log, "+tag+": "+msg);
    }

    public static void debug(String msg) {
        if(!DEBUG_MODE) return;
        System.out.println("dbg: "+msg);
    }

}
