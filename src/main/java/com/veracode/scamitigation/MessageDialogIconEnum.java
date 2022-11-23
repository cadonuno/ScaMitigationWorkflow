package com.veracode.scamitigation;

import javax.swing.*;
import java.net.URL;

public enum MessageDialogIconEnum {
    SUCCESS_ICON("Success", "/images/success.gif"),
    INFO_ICON("Info", "/images/"),
    WARNING_ICON("Warning!", "/images/warning.gif"),
    ERROR_ICON("Error!", "/images/error.gif");

    private final ImageIcon imageIcon;

    private MessageDialogIconEnum(String description, String path) {
        URL imgURL = this.getClass().getResource(path);
        if (imgURL != null) {
            this.imageIcon = new ImageIcon(imgURL, description);
            this.imageIcon.setImage(this.imageIcon.getImage().getScaledInstance(100, 100, 1));
        } else {
            this.imageIcon = null;
        }

    }

    public Icon getImageIcon() {
        return this.imageIcon;
    }
}
