package com.example.webchatserver;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.util.ArrayList;

import java.io.IOException;


/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {
    ArrayList<ChatRoom> master = MasterList.getMaster();
    private String defaultname = "";
    boolean roomExists = false;

    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException {

        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Welcome to the chat room (" + roomID + "). Please state your username in text box to begin.\"}");

        // Check if roomID already exists in master
        for (ChatRoom room : master) {
            if (room.getCode().equals(roomID)) {
                room.setUserName(session.getId(),defaultname);
                roomExists = true;
                break;
            }
        }

        // If roomID doesn't exist in master, add it
        if (!roomExists) {
            ChatRoom newRoom = new ChatRoom(roomID, session.getId());
            master.add(newRoom);
        }
    }

    @OnClose
    public void close(Session session) throws IOException {

        String userId = session.getId();
        ChatRoom room = findRoom(userId);

        assert room != null;
        String roomID = room.getCode();
        String username = room.getUsers().get(userId);
        room.removeUser(userId);

        //broadcast this person left the server
        for (Session peer : session.getOpenSessions()) {
            if (peer.getId().equals(roomID)) {
                peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + username + " left the chat room.\"}");
            }
        }
    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException {

        String userID = session.getId();
        ChatRoom room = findRoom(userID);

        JSONObject jsonMsg = new JSONObject(comm);
        String message = (String) jsonMsg.get("msg");

        assert room != null;
        if(room.getUsers().get(userID).isEmpty()){

            room.setUserName(userID, message);
            //usernames.put(userID, message);
            session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Welcome, " + message + "!\"}");
            //broadcast this person joined the server to the rest
            for(Session peer: session.getOpenSessions()){
                if (!peer.getId().equals(userID) && room.inRoom(peer.getId())) {
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + message + " joined the chat room.\"}");
                }
            }
        }
        else
        {
            String username = room.getUsers().get(userID);
            System.out.println(username);

            for (Session peer : session.getOpenSessions()) {
                if(room.inRoom(peer.getId())) {
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + message + "\"}");
                }
            }
        }
    }

    private ChatRoom findRoom(String userId) {
        for (ChatRoom room : master) {
            if (room.inRoom(userId)) {
                return room;
            }
        }
        return null;
    }
}




