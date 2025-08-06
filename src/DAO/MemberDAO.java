package DAO;

import model.Member;
import utils.DButils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {
    public int insert (int groupId,int userId ){
        String sql = "insert into group_members(group_id,user_id,state) values(?,?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.setInt(3, 1);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<Integer> findGroups (int userId){
        String sql = "select group_id from group_members where user_id=? and state != 0";
        List<Integer> groupIds = new ArrayList<>();
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                groupIds.add(rs.getInt("group_id"));
            }
            return groupIds;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Integer> findMembers (int groupId){
        String sql = "select user_id from group_members where group_id=? and state != 0";
        List<Integer> memberIds = new ArrayList<>();
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                memberIds.add(rs.getInt("user_id"));
            }
            return memberIds;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public int dissolveGroups(int groupId){
        String sql = "update group_members set state=0 where group_id=? ";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int deleteGroup(int userId,int groupId){
        String sql = "update group_members set state = 0 where user_id=? and group_id=?";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int banMember(int userId,int groupId){
        String sql = "update group_members set state = -1 where user_id=? and group_id=?";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int unbanMember(int userId,int groupId){
        String sql = "update group_members set state = 1 where user_id=? and group_id=?";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public Member getMember(int userId,int groupId){
        String sql = "select * from group_members where user_id=? and group_id=?";
        Member member = new Member();
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                member.setId(rs.getInt("user_id"));
                member.setState(rs.getInt("state"));
                member.setGroupId(rs.getInt("group_id"));
            }
            return member;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
