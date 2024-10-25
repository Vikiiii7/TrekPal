package com.example.trekpal;

public class ChatMessage {
    private String messageText;
    private String timestamp;
    private String senderUniqueCode;  // Add this line to store sender's unique code

    public ChatMessage(String messageText, String timestamp, String senderUniqueCode) {
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.senderUniqueCode = senderUniqueCode;  // Initialize sender's unique code
    }

    public String getMessageText() {
        return messageText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSenderUniqueCode() {
        return senderUniqueCode;  // Add this getter method
    }
}
