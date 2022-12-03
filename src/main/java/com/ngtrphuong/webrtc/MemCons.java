package com.ngtrphuong.webrtc;

import java.util.concurrent.ConcurrentHashMap;

import com.ngtrphuong.webrtc.bean.RoomInfo;
import com.ngtrphuong.webrtc.bean.UserBean;

public class MemCons {


    // Online User Form
    public static ConcurrentHashMap<String, UserBean> userBeans = new ConcurrentHashMap<>();

    // Online room table
    public static ConcurrentHashMap<String, RoomInfo> rooms = new ConcurrentHashMap<>();

}
