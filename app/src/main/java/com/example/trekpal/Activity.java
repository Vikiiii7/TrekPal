package com.example.trekpal;

public class Activity {
    private String activityName;
    private String actType;
    private String activityDate;
    private String creatorUniqueCode; // Store creator's unique code

    public Activity() {
        // Firestore requires an empty constructor
    }

    public Activity(String activityName, String actType, String activityDate, String creatorUniqueCode) {
        this.activityName = activityName;
        this.actType = actType;
        this.activityDate = activityDate;
        this.creatorUniqueCode = creatorUniqueCode;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityType() {
        return actType;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public String getCreatorUniqueCode() {
        return creatorUniqueCode;
    }
}
