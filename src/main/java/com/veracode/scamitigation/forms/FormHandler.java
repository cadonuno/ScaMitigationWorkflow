package com.veracode.scamitigation.forms;

import com.veracode.scamitigation.ExceptionHandler;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class FormHandler {
    public static <T extends BaseForm> JFrame handleInitialize(Class<T> pageToInitialize,
                                                               String pageTitle, JFrame form) {
        if (form == null) {
            try {
                form = initializeForm(pageToInitialize, pageTitle);
            } catch (IllegalAccessException | InvocationTargetException
                     | InstantiationException | NoSuchMethodException e) {
                ExceptionHandler.logException(e);
            }
        } else {
            form.setVisible(true);
        }

        return form;
    }

    private static <T extends BaseForm> JFrame initializeForm(Class<T> pageToInitialize, String pageTitle) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        JFrame frame = BaseForm.getFrame(pageTitle);
        T landingForm = pageToInitialize.getDeclaredConstructor(JFrame.class).newInstance(frame);
        landingForm.initialize();
        landingForm.afterLoad();
        return frame;
    }
}
