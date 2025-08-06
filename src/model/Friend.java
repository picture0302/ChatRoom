package model;

import javax.xml.crypto.Data;
import java.util.Date;

public class Friend {
    private int id;
    private int start;
    private int end;
    private Date date;
    private int state;
    public Friend() {}
    public Friend(int id, int start, int end, Date date, int state) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.date = date;
        this.state = state;
    }

    public Friend(int end, int start, int state) {
        this.end = end;
        this.start = start;
        this.state = state;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStatue() {
        return state;
    }

    public void setStatue(int state) {
        this.state = state;
    }
}
