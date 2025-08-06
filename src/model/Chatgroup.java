package model;

import java.util.Date;

public class Chatgroup {
    private int id;
    private int ownerId;
    private String name;
    private Date date;
    private int state;
    public Chatgroup() {
    }
    public Chatgroup(int id,int ownerId, String name, Date date, int state) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.date = date;
        this.state = state;
    }

    public Chatgroup(int ownerId, String name, int state) {
        this.ownerId = ownerId;
        this.name = name;
        this.state = state;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
