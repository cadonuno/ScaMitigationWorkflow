package com.veracode.scamitigation;

public class Logger {
    private Logger() {
    }

    public static void log(Object aMessage) {
        System.out.println(aMessage);
    }
}
