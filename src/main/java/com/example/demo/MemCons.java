package com.example.demo;

import com.example.demo.bean.RoomInfo;
import com.example.demo.bean.UserBean;

import java.util.concurrent.ConcurrentHashMap;

public class MemCons {


    // Online User Form
    public static ConcurrentHashMap<String, UserBean> userBeans = new ConcurrentHashMap<>();

    // Online room table
    public static ConcurrentHashMap<String, RoomInfo> rooms = new ConcurrentHashMap<>();

}
