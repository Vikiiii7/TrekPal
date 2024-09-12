package com.example.trekpal;

public class Activity {
    private String activityName;
    private String actType;

    public Activity() {
        // Firestore requires an empty constructor
    }

    public Activity(String activityName, String actType) {
        this.activityName = activityName;
        this.actType = actType;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityType() {
        return actType;
    }
}
