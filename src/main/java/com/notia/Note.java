package com.notia;

import java.sql.Date;

public class Note {
    private int id;
    private String title;
    private String content;
    private Date createdOn;
    private Date updatedOn;
    private boolean isEmbedded;
    private boolean isSubnote;
    private int parentId;

    public Note(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Note(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public Note(int id, String title, String content, Date createdOn, Date updatedOn, boolean isEmbedded, boolean isSubnote, int parentId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.isEmbedded = isEmbedded;
        this.isSubnote = isSubnote;
        this.parentId = parentId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public boolean isSubnote() {
        return isSubnote;
    }

    public int getParentId() {
        return parentId;
    }

    // Setters for updating note content
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return title;
    }
}
