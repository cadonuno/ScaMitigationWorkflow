package com.veracode.scamitigation;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHandler {
    private static final SimpleDateFormat COMMENT_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss 'GMT'Z");

    private DateHandler() {
    }

    public static String getDateAsString(Date date) {
        return date == null ? "" : COMMENT_DATE_FORMAT.format(date);
    }
}
