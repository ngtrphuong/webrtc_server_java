package com.example.demo.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.CopyOnWriteArrayList;

public class RoomInfo {
    // roomId
    @JsonFormat
    private String roomId;
    // Creator Id
    @JsonFormat
    private String userId;
    // People in the room
    @JsonIgnore
    private CopyOnWriteArrayList<UserBean> userBeans = new CopyOnWriteArrayList<>();
    // Room size
    @JsonFormat
    private int maxSize;
    // Existing number
    @JsonFormat
    private int currentSize;

    @JsonIgnore
    public RoomInfo() {
    }


    @JsonIgnore
    public CopyOnWriteArrayList<UserBean> getUserBeans() {
        return userBeans;
    }

    @JsonIgnore
    public void setUserBeans(CopyOnWriteArrayList<UserBean> userBeans) {
        this.userBeans = userBeans;
        setCurrentSize(this.userBeans.size());
    }

    @JsonIgnore
    public int getMaxSize() {
        return maxSize;
    }

    @JsonIgnore
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @JsonIgnore
    public String getRoomId() {
        return roomId;
    }

    @JsonIgnore
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @JsonIgnore
    public String getUserId() {
        return userId;
    }

    @JsonIgnore
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }
}
