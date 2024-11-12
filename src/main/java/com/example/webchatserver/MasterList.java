package com.example.webchatserver;

import java.util.ArrayList;

public class MasterList {

    //Master makes the list of all the rooms
    private static final ArrayList<ChatRoom> master = new ArrayList<>();

    // Method to get the master list
    public static ArrayList<ChatRoom> getMaster() {
        return master;
    }

}