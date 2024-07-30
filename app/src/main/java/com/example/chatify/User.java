package com.example.chatify;

public class User {

    private String userId;
    private String userName;
    private String email;
    private String displayName;
    private String profilePic;

    public User(String userId,String email, String userName,String displayName) {

        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.displayName = displayName;

    }
    public  User()
    {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


}
