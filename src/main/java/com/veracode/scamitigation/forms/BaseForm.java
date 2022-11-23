package com.veracode.scamitigation.forms;

import javax.swing.*;
import java.awt.*;

public abstract class BaseForm {
    protected JFrame frame;

    protected BaseForm(JFrame frame) {
        this.frame = frame;
    }

    protected static JFrame getFrame(String title) {
        return new JFrame(title);
    }

    protected void initialize() {
        this.frame.setContentPane(this.getRootPanel());
        this.frame.setDefaultCloseOperation(3);
        this.frame.pack();
        this.frame.setVisible(true);
    }

    protected abstract Container getRootPanel();

    public void afterLoad() {
    }
}
