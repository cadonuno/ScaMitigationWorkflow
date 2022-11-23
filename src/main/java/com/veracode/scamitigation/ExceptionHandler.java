package com.veracode.scamitigation;

import com.veracode.scamitigation.dialogs.MessageDialog;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionHandler {
    private static final boolean IS_DEBUG = System.getProperty("java.class.path").contains("idea_rt.jar");

    public ExceptionHandler() {
    }

    public static void logException(Exception anException) {
        MessageDialog.showErrorDialog(IS_DEBUG ? anException.getMessage() + ":\n" + ExceptionUtils.getStackTrace(anException) : "Unexpected error");
    }
}
