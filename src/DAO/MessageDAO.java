package DAO;

import model.Message;
import utils.DButils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MessageDAO {
    public int insertUser(int sender,int receiver,String message){
        String sql = "insert into messages(sender_id,receiver_id,content,state) values(?,?,?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sender);
            ps.setInt(2, receiver);
            ps.setString(3, message);
            ps.setInt(4, 1);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int insertUser(int sender,int receiver,String message,String type){
        String sql = "insert into messages(sender_id,receiver_id,content,state,msg_type) values(?,?,?,?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sender);
            ps.setInt(2, receiver);
            ps.setString(3, message);
            ps.setInt(4, 1);
            ps.setString(5, type);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int insertGroup(int sender,int groupid,String message){
        String sql = "insert into messages(sender_id,group_id,content,state) values(?,?,?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sender);
            ps.setInt(2, groupid);
            ps.setString(3, message);
            ps.setInt(4, 1);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int insertGroup(int sender,int groupid,String message,String type){
        String sql = "insert into messages(sender_id,group_id,content,state,msg_type) values(?,?,?,?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sender);
            ps.setInt(2, groupid);
            ps.setString(3, message);
            ps.setInt(4, 1);
            ps.setString(5, type);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public ArrayList<Message> findUserChat (int sender, int receiver){
        String sql = "select * from messages where (sender_id=? and receiver_id=? and state = 1) " +
                "or(sender_id=? and receiver_id=? and state = 1) order by created_at ASC ";
        try(Connection conn = DButils.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sender);
            ps.setInt(2, receiver);
            ps.setInt(3, receiver);
            ps.setInt(4, sender);
            ArrayList<Message> messages = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Message msg = new Message();
                msg.setSenderId(rs.getInt(2));
                msg.setReceiverId(rs.getInt(3));
                msg.setMessage(rs.getString(5));
                msg.setType(rs.getString(6));
                msg.setDate(rs.getDate(7));
                messages.add(msg);
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public ArrayList<Message> findGroupChat (int group){
        String sql = "select * from messages where group_id=? and state = 1 order by created_at ASC ";
        try(Connection conn = DButils.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, group);
            ArrayList<Message> messages = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Message msg = new Message();
                msg.setSenderId(rs.getInt(2));
                msg.setReceiverId(rs.getInt(4));
                msg.setMessage(rs.getString(5));
                msg.setType(rs.getString(6));
                msg.setDate(rs.getDate(7));
                messages.add(msg);
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean deleteUserChat(int sender,int receiver){
        String sql = "update messages set state = 0 where (sender_id=? and receiver_id=?) or (sender_id=? and receiver_id=?) ";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sender);
            ps.setInt(2, receiver);
            ps.setInt(3, receiver);
            ps.setInt(4, sender);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
