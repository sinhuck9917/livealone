package com.example.livealone4.Models;

public class Chat {

    String key;
    String uid;
    String uidOfAloneTaker;


    public Chat() {
    }

    public Chat(String key, String uid, String uidOfAloneTaker) {
        this.key = key;
        this.uid = uid;
        this.uidOfAloneTaker = uidOfAloneTaker;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUidOfAloneTaker() {
        return uidOfAloneTaker;
    }

    public void setUidOfAloneTaker(String uidOfAloneTaker) { this.uidOfAloneTaker = uidOfAloneTaker; }
}
