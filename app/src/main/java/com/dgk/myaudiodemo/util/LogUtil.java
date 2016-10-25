package com.dgk.myaudiodemo.util;

import android.util.Log;

/**
 * Log的管理类
 */
public class LogUtil {

    // 正式发版的时候，将其置为false
    private static boolean allowDebug = true;

    // debug
    public static void d(String tag, String msg) {
        if (allowDebug) {
            Log.d(tag, msg);
        }
    }

    public static void d(Object tag, String msg) {
        if (allowDebug) {
            Log.d(tag.getClass().getSimpleName(), msg);
        }
    }

    // info
    public static void i(String tag, String msg) {
        if (allowDebug) {
            Log.i(tag, msg);
        }
    }

    public static void i(Object tag, String msg) {
        if (allowDebug) {
            Log.i(tag.getClass().getSimpleName(), msg);
        }
    }

    // warn
    public static void w(String tag, String msg) {
        if (allowDebug) {
            Log.w(tag, msg);
        }
    }

    public static void w(Object tag, String msg) {
        if (allowDebug) {
            Log.w(tag.getClass().getSimpleName(), msg);
        }
    }

    // error
    public static void e(String tag, String msg) {
        if (allowDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(Object tag, String msg) {
        if (allowDebug) {
            Log.e(tag.getClass().getSimpleName(), msg);
        }
    }

}
