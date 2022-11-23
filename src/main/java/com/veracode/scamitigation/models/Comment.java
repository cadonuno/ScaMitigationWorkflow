package com.veracode.scamitigation.models;

import java.util.Date;

public class Comment {
    private final Date dateCreated;
    private final String text;

    public Comment(Date dateCreated, String text) {
        this.dateCreated = dateCreated;
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public Date getDateCreated() {
        return this.dateCreated;
    }
}
