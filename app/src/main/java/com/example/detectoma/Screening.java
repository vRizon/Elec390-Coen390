package com.example.detectoma;

public class Screening {
    private String timestamp;
    private String tempDifference;
    private String distance1;
    private String distance2;

    public Screening() {
        // Default constructor for Firebase
    }

    public Screening(String timestamp, String tempDifference, String distance1, String distance2) {
        this.timestamp = timestamp;
        this.tempDifference = tempDifference;
        this.distance1 = distance1;
        this.distance2 = distance2;
    }

    // Getters and setters for all fields
    public String getTimestamp() {
        return timestamp;
    }

    public String getTempDifference() {
        return tempDifference;
    }

    public String getDistance1() {
        return distance1;
    }

    public String getDistance2() {
        return distance2;
    }
}
