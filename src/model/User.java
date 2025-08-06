package model;

import java.util.Date;

public class User {
    private int id;
    private String username;
    private String password;
    private int state;
    private Date date;
    public User() {}
    public User(int id, String username, String password, int state, Date date) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.state = state;
        this.date = date;
    }
    public User(String password, String username) {
        this.password = password;
        this.username = username;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", date=" + date +
                '}';
    }
}
