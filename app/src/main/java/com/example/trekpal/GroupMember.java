package com.example.trekpal;

public class GroupMember {
    private String username;
    private String uniqueCode;

    // Default constructor required for calls to DataSnapshot.getValue(GroupMember.class)
    public GroupMember() {
    }

    // Constructor with parameters
    public GroupMember(String username, String uniqueCode) {
        this.username = username;
        this.uniqueCode = uniqueCode;
    }

    // Getter and setter for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and setter for uniqueCode
    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }
}
