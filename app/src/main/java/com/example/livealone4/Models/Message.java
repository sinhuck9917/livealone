package com.example.livealone4.Models;

import com.google.firebase.database.ServerValue;

public class Message {

    String uid;
    String content;
    Object timestamp;


    public Message() {
    }

    public Message(String uid, String content) {
        timestamp = ServerValue.TIMESTAMP;
        this.uid = uid;
        this.content = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
