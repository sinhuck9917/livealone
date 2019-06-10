package com.example.livealone4.Models;

public class User {

    private String name; //temp ""
    private String birthday;
    private String uid;
    private String current_livealone; //현재 진행중인, 또는 등록한 같이먹기의 key
    private Double star; //평점
    private Integer livealoneCount;
    private String phoneNumber;
    private Integer newMessages;
    private Integer money; //가상화폐
    private Boolean isOnline;
    private String gender;
    private String location;
    private String email;

    /* for machine learning */
    /* 비정상적인 사용자를 분류 */
    private Integer suspensions; // 중지 횟수

    /* output of machine learning  */
    // [type0, type1]
    // [1 0] : normal (0)
    // [0 1] : abnormal user (1)
    private Integer type0, type1;


    /* Constructors, Getters, and Setters */
    public User() {

    }

    public void setDefaultInfo(String uid){
        this.uid = uid;
        star = 0.0d;
        livealoneCount = 0;
        newMessages = 0;
        money = 0;
        suspensions = 0;
        type0 = 1;
        type1 = 0; //normal user

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



    public String getCurrent_livealone() {
        return current_livealone;
    }

    public void setCurrent_livealone(String current_livealone) {
        this.current_livealone = current_livealone;
    }

    public Integer getNewMessages() {
        return newMessages;
    }

    public void setNewMessages(Integer newMessages) {
        this.newMessages = newMessages;
    }

    public Double getStar() {
        return star;
    }

    public void setStar(Double star) {
        this.star = star;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getLivealoneCount() {
        return livealoneCount;
    }

    public void setLivealoneCount(Integer livealoneCount) {
        this.livealoneCount = livealoneCount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public Integer getSuspensions() {
        return suspensions;
    }

    public void setSuspensions(Integer suspensions) {
        this.suspensions = suspensions;
    }


    public Integer getType0() {
        return type0;
    }

    public void setType0(Integer type0) {
        this.type0 = type0;
    }

    public Integer getType1() {
        return type1;
    }

    public void setType1(Integer type1) {
        this.type1 = type1;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
