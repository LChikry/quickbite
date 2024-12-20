package org.quickbitehub.client;

import java.util.Stack;

public class User {
    private String firstName;
    private String lastName;
    private String middleNames;
    private final UserType userType;
    private Stack<NavigationState> userState = new Stack<>();

    public User(String firstName, String lastName, String middleNames, UserType userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleNames = middleNames;
        this.userType = userType;
    }

    public Stack<NavigationState> getUserState() {
        return userState;
    }
}
