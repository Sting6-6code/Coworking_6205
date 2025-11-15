package model;

public class User {

    private String userId;       // 唯一id (e.g., "U-xxxxxx")
    private String username;     // 登录用户名
    private String password;     // 登录密码
    private String email;        // 邮箱
    private String type;         // User or Admin
    private String membership;   // Member or Non-member

    // ----------------------------
    // Constructors
    // ----------------------------

    public User(String userId, String username, String password,
                String email, String type, String membership) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.type = type;
        this.membership = membership;
    }

    // Constructor without userId (useful when registering)
    public User(String username, String password, String email,
                String type, String membership) {
        this.userId = null; // later assigned by controller
        this.username = username;
        this.password = password;
        this.email = email;
        this.type = type;
        this.membership = membership;
    }

    // Empty constructor (JavaFX or frameworks may need this)
    public User() {}

    // ----------------------------
    // Getters & Setters
    // ----------------------------

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type; // User / Admin
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    // ----------------------------
    // Convert to CSV line
    // ----------------------------

    public String toCSV() {
        return String.join(",", userId, username, password, email, type, membership);
    }

    // ----------------------------
    // Parse User from CSV
    // Format: userId,username,password,email,type,membership
    // ----------------------------

    public static User fromCSV(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid user csv line: " + line);
        }

        return new User(
                parts[0], // userId
                parts[1], // username
                parts[2], // password
                parts[3], // email
                parts[4], // type
                parts[5]  // membership
        );
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", type='" + type + '\'' +
                ", membership='" + membership + '\'' +
                '}';
    }
}
