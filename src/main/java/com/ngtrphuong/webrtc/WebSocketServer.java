package com.ngtrphuong.webrtc;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ngtrphuong.webrtc.bean.EventData;
import com.ngtrphuong.webrtc.bean.RoomInfo;
import com.ngtrphuong.webrtc.bean.UserBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import static com.ngtrphuong.webrtc.MemCons.rooms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint("/ws/{userId}/{device}")
@Component
public class WebSocketServer {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServer.class);

    private String userId;
    private static Gson gson = new Gson();
    private static String avatar = "image.jpeg";


    // User userId login in
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("device") String de) {
        int device = Integer.parseInt(de);
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            userBean = new UserBean(userId, avatar);
        }
        if (device == 0) {
            userBean.setPhoneSession(session, device);
            userBean.setPhone(true);
            LOG.info("Phone user login: " + userBean.getUserId() + ",session: " + session.getId());
        } else {
            userBean.setPcSession(session, device);
            userBean.setPhone(false);
            LOG.info("PC user login: " + userBean.getUserId() + ",session: " + session.getId());
        }
        this.userId = userId;

        //Add to list
        MemCons.userBeans.put(userId, userBean);

        // Log in successfully, return personal information
        EventData send = new EventData();
        send.setEventName("__login_success");
        Map<String, Object> map = new HashMap<>();
        map.put("userID", userId);
        map.put("avatar", avatar);
        send.setData(map);
        session.getAsyncRemote().sendText(gson.toJson(send));


    }

    // User offline
    @OnClose
    public void onClose() {
        System.out.println(userId + "-->onClose......");
        // Find out the room based on the username,
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean != null) {
            if (userBean.isPhone()) {
                Session phoneSession = userBean.getPhoneSession();
                if (phoneSession != null) {
                    try {
                        phoneSession.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    userBean.setPhoneSession(null, 0);
                    MemCons.userBeans.remove(userId);
                }
                LOG.info("Phone user leaves: " + userBean.getUserId());
            } else {
                Session pcSession = userBean.getPcSession();
                if (pcSession != null) {
                    try {
                        pcSession.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    userBean.setPcSession(null, 0);
                    MemCons.userBeans.remove(userId);
                    LOG.info("The PC user leaves: " + userBean.getUserId());
                }
            }
        }

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Receive data: " + message);
        handleMessage(message, session);
    }

    // Send various messages
    private void handleMessage(String message, Session session) {
        EventData data;
        try {
            data = gson.fromJson(message, EventData.class);
        } catch (JsonSyntaxException e) {
            System.out.println("JSON parsing error: " + message);
            return;
        }
        switch (data.getEventName()) {
            case "__create":
                createRoom(message, data.getData());
                break;
            case "__invite":
                invite(message, data.getData());
                break;
            case "__ring":
                ring(message, data.getData());
                break;
            case "__cancel":
                cancel(message, data.getData());
                break;
            case "__reject":
                reject(message, data.getData());
                break;
            case "__join":
                join(message, data.getData());
                break;
            case "__ice_candidate":
                iceCandidate(message, data.getData());
                break;
            case "__offer":
                offer(message, data.getData());
                break;
            case "__answer":
                answer(message, data.getData());
                break;
            case "__leave":
                leave(message, data.getData());
                break;
            case "__audio":
                transAudio(message, data.getData());
                break;
            case "__disconnect":
                disconnet(message, data.getData());
                break;
            default:
                break;
        }

    }

    // Create a room
    private void createRoom(String message, Map<String, Object> data) {
        String room = (String) data.get("room");
        String userId = (String) data.get("userID");

        System.out.println(String.format("createRoom:%s ", room));

        RoomInfo roomParam = rooms.get(room);
        // No such room
        if (roomParam == null) {
            int size = (int) Double.parseDouble(String.valueOf(data.get("roomSize")));
            // Create a room
            RoomInfo roomInfo = new RoomInfo();
            roomInfo.setMaxSize(size);
            roomInfo.setRoomId(room);
            roomInfo.setUserId(userId);
            // Store the room
            rooms.put(room, roomInfo);


            CopyOnWriteArrayList<UserBean> copy = new CopyOnWriteArrayList<>();
            // Add yourself to the room
            UserBean my = MemCons.userBeans.get(userId);
            copy.add(my);
            rooms.get(room).setUserBeans(copy);

            // Send to yourself
            EventData send = new EventData();
            send.setEventName("__peers");
            Map<String, Object> map = new HashMap<>();
            map.put("connections", "");
            map.put("you", userId);
            map.put("roomSize", size);
            send.setData(map);
            System.out.println(gson.toJson(send));
            sendMsg(my, -1, gson.toJson(send));

        }

    }

    // First invitation
    private void invite(String message, Map<String, Object> data) {
        String userList = (String) data.get("userList");
        String room = (String) data.get("room");
        String inviteId = (String) data.get("inviteID");
        boolean audioOnly = (boolean) data.get("audioOnly");
        String[] users = userList.split(",");

        System.out.println(String.format("room:%s,%s invite %s audioOnly:%b", room, inviteId, userList, audioOnly));
        // Send invitations to others
        for (String user : users) {
            UserBean userBean = MemCons.userBeans.get(user);
            if (userBean != null) {
                sendMsg(userBean, -1, message);
            }
        }


    }

    // Ring back
    private void ring(String message, Map<String, Object> data) {
        String room = (String) data.get("room");
        String inviteId = (String) data.get("toID");

        UserBean userBean = MemCons.userBeans.get(inviteId);
        if (userBean != null) {
            sendMsg(userBean, -1, message);
        }
    }

    // Cancel outgoing
    private void cancel(String message, Map<String, Object> data) {
        String room = (String) data.get("room");
        String userList = (String) data.get("userList");
        String[] users = userList.split(",");
        for (String userId : users) {
            UserBean userBean = MemCons.userBeans.get(userId);
            if (userBean != null) {
                sendMsg(userBean, -1, message);
            }
        }

        if (MemCons.rooms.get(room) != null) {
            MemCons.rooms.remove(room);
        }


    }

    // Refuse to answer
    private void reject(String message, Map<String, Object> data) {
        String room = (String) data.get("room");
        String toID = (String) data.get("toID");
        UserBean userBean = MemCons.userBeans.get(toID);
        if (userBean != null) {
            sendMsg(userBean, -1, message);
        }
        RoomInfo roomInfo = MemCons.rooms.get(room);
        if (roomInfo != null) {
            if (roomInfo.getMaxSize() == 2) {
                MemCons.rooms.remove(room);
            }
        }


    }

    // Join room
    private void join(String message, Map<String, Object> data) {
        String room = (String) data.get("room");
        String userID = (String) data.get("userID");

        RoomInfo roomInfo = rooms.get(room);

        int maxSize = roomInfo.getMaxSize();
        CopyOnWriteArrayList<UserBean> roomUserBeans = roomInfo.getUserBeans();

        //The room is full
        if (roomUserBeans.size() >= maxSize) {
            return;
        }
        UserBean my = MemCons.userBeans.get(userID);
        // 1. Add me to the room
        roomUserBeans.add(my);
        roomInfo.setUserBeans(roomUserBeans);
        rooms.put(room, roomInfo);

        // 2. Return the information of everyone in the room
        EventData send = new EventData();
        send.setEventName("__peers");
        Map<String, Object> map = new HashMap<>();

        String[] cons = new String[roomUserBeans.size()];
        for (int i = 0; i < roomUserBeans.size(); i++) {
            UserBean userBean = roomUserBeans.get(i);
            if (userBean.getUserId().equals(userID)) {
                continue;
            }
            cons[i] = userBean.getUserId();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cons.length; i++) {
            if (cons[i] == null) {
                continue;
            }
            sb.append(cons[i]).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        // Send a message to yourself
        map.put("connections", sb.toString());
        map.put("you", userID);
        map.put("roomSize", roomInfo.getMaxSize());
        send.setData(map);
        sendMsg(my, -1, gson.toJson(send));

        // 3. Send messages to other people in the room
        EventData newPeer = new EventData();
        newPeer.setEventName("__new_peer");
        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put("userID", userID);
        newPeer.setData(sendMap);

        for (UserBean userBean : roomUserBeans) {
            if (userBean.getUserId().equals(userID)) {
                continue;
            }
            sendMsg(userBean, -1, gson.toJson(newPeer));
        }


    }

    // Switch to voice answering
    private void transAudio(String message, Map<String, Object> data) {
        String userId = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("User " + userId + " does not exist");
            return;
        }
        sendMsg(userBean, -1, message);
    }

    // Accidentally disconnected
    private void disconnet(String message, Map<String, Object> data) {
        String userId = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("User " + userId + " does not exist");
            return;
        }
        sendMsg(userBean, -1, message);
    }

    // Send offer
    private void offer(String message, Map<String, Object> data) {
        String userId = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        sendMsg(userBean, -1, message);
    }

    // Send answer
    private void answer(String message, Map<String, Object> data) {
        String userId = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("User " + userId + " does not exist");
            return;
        }
        sendMsg(userBean, -1, message);

    }

    // Send ICE message
    private void iceCandidate(String message, Map<String, Object> data) {
        String userId = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("User " + userId + " does not exist");
            return;
        }
        sendMsg(userBean, -1, message);
    }

    // Leave the room
    private void leave(String message, Map<String, Object> data) {
        String room = (String) data.get("room");
        String userId = (String) data.get("fromID");
        if (userId == null) return;
        // Get room information
        RoomInfo roomInfo = MemCons.rooms.get(room);
        // Get the user list in the room
        CopyOnWriteArrayList<UserBean> roomInfoUserBeans = roomInfo.getUserBeans();
        // Send the message to the other people from the room
        for (UserBean userBean : roomInfoUserBeans) {
            // Exclude yourself
            if (userId.equals(userBean.getUserId())) {
                continue;
            }
            // Send a message
            sendMsg(userBean, -1, message);

            if (roomInfoUserBeans.size() == 1) {
                System.out.println("There is only one person left in the room");
                if (roomInfo.getMaxSize() == 2) {
                    MemCons.rooms.remove(room);
                }
            }

            if (roomInfoUserBeans.size() == 0) {
                System.out.println("Nobody in the room");
                MemCons.rooms.remove(room);
            }
        }


    }


    private static final Object object = new Object();

    // Send messages to different devices
    private void sendMsg(UserBean userBean, int device, String str) {
        if (device == 0) {
            Session phoneSession = userBean.getPhoneSession();
            if (phoneSession != null) {
                synchronized (object) {
                    phoneSession.getAsyncRemote().sendText(str);
                }
            }
        } else if (device == 1) {
            Session pcSession = userBean.getPcSession();
            if (pcSession != null) {
                synchronized (object) {
                    pcSession.getAsyncRemote().sendText(str);
                }
            }
        } else {
            Session phoneSession = userBean.getPhoneSession();
            if (phoneSession != null) {
                synchronized (object) {
                    phoneSession.getAsyncRemote().sendText(str);
                }
            }
            Session pcSession = userBean.getPcSession();
            if (pcSession != null) {
                synchronized (object) {
                    pcSession.getAsyncRemote().sendText(str);
                }
            }

        }

    }


}