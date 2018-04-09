package com.example.gmo.notesapp.network.model;

/**
 * Created by GMO on 4/9/2018.
 */

public class Note extends BaseResponse{
    int id;
    String note;
    String timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
