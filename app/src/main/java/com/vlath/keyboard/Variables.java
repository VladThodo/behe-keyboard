package com.vlath.keyboard;

/**
 * Created by Vlad on 6/22/2017.
 */

public  class Variables {


    /**
     * Here we handle global variables
     */
    private static boolean IS_CTRL = false;
    private static boolean IS_ALT = false;
    private static boolean IS_SHIFT = false;


    public static boolean isAnyOn() {
        return IS_CTRL || IS_ALT;
    }

    public static boolean isCtrl() {
        return IS_CTRL;
    }

    public static boolean isAlt() {
        return IS_ALT;
    }

    public static void setIsCtrl(boolean on) {
        IS_CTRL = on;
    }


    public static void setIsAlt(boolean on) {
        IS_ALT = on;
    }

    public static void setAltOn() {
        IS_ALT = true;
    }

    public static void setAltOff() {
        IS_ALT = false;
    }

    public static void setCtrlOn() {
        IS_CTRL = true;
    }

    public static void setCtrlOff() {
        IS_CTRL = false;
    }

    public static void setShiftOn() {
        IS_SHIFT = true;
    }

    public static void setShiftOff() {
        IS_SHIFT = false;
    }

    public static boolean isShift() {
        return IS_SHIFT;
    }



}



