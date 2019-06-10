package com.example.livealone4.Models;

import com.google.firebase.database.ServerValue;

public class LiveAlone {
    private String key;
    private String title;
    private Object timestamp; //게시일
    private String uid;
    private String location;
    private String comment;
    private String aloneType;
    private Long startPeriod;
    private Long endPeriod;
    private String uidOfAloneTaker;
    private String waitingForDeletion; //삭제대기 상태

    public LiveAlone(){
    }

    public String getUidOfAloneTaker() {
        return uidOfAloneTaker;
    }

    public void setUidOfAloneTaker(String uidOfAloneTaker) { this.uidOfAloneTaker = uidOfAloneTaker; }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(){
        timestamp = ServerValue.TIMESTAMP;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAloneType() {
        return aloneType;
    }

    public void setAloneType(String aloneType) {
        this.aloneType = aloneType;
    }

    public Long getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(Long startPeriod) {
        this.startPeriod = startPeriod;
    }

    public Long getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(Long endPeriod) {
        this.endPeriod = endPeriod;
    }

    public String getWaitingForDeletion() {
        return waitingForDeletion;
    }

    public void setWaitingForDeletion(String waitingForDeletion) {
        this.waitingForDeletion = waitingForDeletion;
    }
}
