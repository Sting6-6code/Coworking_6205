package util;

import model.User;

public class CurrentUser {
    private static User currentUser;

    public static void set(User user) {
        currentUser = user;
    }

    public static User get() {
        return currentUser;
    }

    public static String getUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }
}
