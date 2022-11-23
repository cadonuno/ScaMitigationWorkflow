package com.veracode.scamitigation.ui.components;

import java.awt.Cursor;
import java.util.function.Supplier;
import javax.swing.JFrame;

public final class CursorHandler {
    private CursorHandler() {
    }

    public static void runWithWaitCursor(JFrame frame, Runnable toRun) {
        runWithWaitCursor(frame, () -> {
            toRun.run();
            return null;
        });
    }

    public static <T> T runWithWaitCursor(JFrame frame, Supplier<T> toRun) {
        Cursor currentCursor = frame.getCursor();
        frame.setCursor(Cursor.getPredefinedCursor(3));

        try {
            return toRun.get();
        } finally {
            frame.setCursor(currentCursor);
        }
    }
}
