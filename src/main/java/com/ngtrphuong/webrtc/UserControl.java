package com.ngtrphuong.webrtc;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ngtrphuong.webrtc.bean.RoomInfo;
import com.ngtrphuong.webrtc.bean.UserBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class UserControl {

    @RequestMapping("/")
    public String index() {
        return "Welcome to my webRTC chat tool";
    }


    @RequestMapping("/roomList")
    public List<RoomInfo> roomList() {
        ConcurrentHashMap<String, RoomInfo> rooms = MemCons.rooms;
        Collection<RoomInfo> values = rooms.values();
        ArrayList<RoomInfo> objects = new ArrayList<>();
        values.forEach(roomInfo -> {
            if (roomInfo.getMaxSize() > 2) {
                objects.add(roomInfo);
            }
        });
        return objects;
    }

    @RequestMapping("/userList")
    public List<UserBean> userList() {
        ConcurrentHashMap<String, UserBean> userBeans = MemCons.userBeans;
        Collection<UserBean> values = userBeans.values();
        return new ArrayList<>(values);
    }

}
