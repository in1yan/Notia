package com.notia;

public class Note {
    private int id;
    private String title;
    private String content;

    public Note(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Note(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
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

    @Override
    public String toString() {
        return title;
    }
}
