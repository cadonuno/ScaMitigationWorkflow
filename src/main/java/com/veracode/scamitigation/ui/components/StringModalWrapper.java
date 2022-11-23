package com.veracode.scamitigation.ui.components;

public class StringModalWrapper {
    private String theString;
    private boolean isCancel;

    public StringModalWrapper() {
    }

    public String getTheString() {
        return this.theString;
    }

    public void setTheString(String theString) {
        this.theString = theString;
    }

    public boolean isSet() {
        return this.isCancel || this.theString != null && !this.theString.trim().isEmpty();
    }

    public boolean isCancel() {
        return this.isCancel;
    }

    public void setCancel(boolean cancel) {
        this.isCancel = cancel;
    }
}