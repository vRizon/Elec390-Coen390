package com.example.detectoma;

public class UserProfile {
    public String firstName;
    public String lastName;
    public String dateOfBirth;

    public UserProfile() {
        // Default constructor required for Firebase calls to DataSnapshot.getValue(UserProfile.class)
    }

    public UserProfile(String firstName, String lastName, String dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }
}
