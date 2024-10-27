package com.example.trekpal;

public class SOSAlert {

    private String timeSent;
    private String senderUsername;
    private String locationDescription;
    private String message;
    private double latitude;  // Add latitude field
    private double longitude; // Add longitude field
    private String uniqueCode; // Add uniqueCode field

    // Empty constructor needed for Firestore
    public SOSAlert() {
    }

    public SOSAlert(String timeSent, String senderUsername, String locationDescription, String message,  String uniqueCode) {

        this.timeSent = timeSent;
        this.senderUsername = senderUsername;
        this.locationDescription = locationDescription;
        this.message = message;
        this.uniqueCode = uniqueCode; // Initialize uniqueCode
    }

    // Add getters and setters for all fields

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }
}
