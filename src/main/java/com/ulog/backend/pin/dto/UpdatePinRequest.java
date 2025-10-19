package com.ulog.backend.pin.dto;

public class UpdatePinRequest {

    private String note;

    private String tags;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}

