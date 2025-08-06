package model;

import java.util.Date;

public class Member {
    private int id;
    private int groupId;
    private int userId;
    private Date date;
    private int state;

    public Member() {}
    public Member(Date date, int groupId, int id, int state, int userId) {
        this.date = date;
        this.groupId = groupId;
        this.id = id;
        this.state = state;
        this.userId = userId;
    }

    public Member(int groupId, int state, int userId) {
        this.groupId = groupId;
        this.state = state;
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
