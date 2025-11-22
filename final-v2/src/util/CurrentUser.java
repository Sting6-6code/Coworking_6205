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
    
   
    public static boolean isMember() {
        if (currentUser == null) {
            System.out.println("DEBUG CurrentUser is null");
            return false;
        }

        String membership = currentUser.getMembership();
        String username   = currentUser.getUsername();

        System.out.println("DEBUG username = " + username +
                ", membership = [" + membership + "]");

        return membership != null
                && membership.trim().equalsIgnoreCase("Member");
    }
}
