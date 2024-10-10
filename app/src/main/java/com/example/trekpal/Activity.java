package com.example.trekpal;

public class Activity {
    private String activityName;
    private String actType;
    private String activityDate;
    private String creatorUniqueCode; // Store creator's unique code
    private boolean isInvitation;

    public Activity() {
        // Firestore requires an empty constructor
    }

    public Activity(String activityName, String actType, String activityDate, String creatorUniqueCode, boolean isInvitation) {
        this.activityName = activityName;
        this.actType = actType;
        this.activityDate = activityDate;
        this.creatorUniqueCode = creatorUniqueCode;
        this.isInvitation = isInvitation;
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

    public boolean isInvitation() {
        return isInvitation;
    }
}
