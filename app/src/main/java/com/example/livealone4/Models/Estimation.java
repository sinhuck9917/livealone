package com.example.livealone4.Models;


public class Estimation {

    private String comment, key, uid, name;
    private Double rating; //평가

    public Estimation() {

    }

    public Estimation(String key, String comment, Double rating, String uid, String name) {
        this.comment = comment;
        this.rating = rating;
        this.key = key;
        this.uid = uid;
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
