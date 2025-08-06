package DAO;

import model.Friend;
import utils.DButils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendDAO implements BaseDao<Friend> {
    public int insertdoubble (int start,int end) {
        String sql="update Friend set state = 1  where start=? and end=? and state = 0";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,start);
            ps.setInt(2,end);
            int rows = ps.executeUpdate();
            PreparedStatement ps1 = conn.prepareStatement(sql);
            ps1.setInt(1,end);
            ps1.setInt(2,start);
            int rows1 = ps1.executeUpdate();
            return rows + rows1;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    @Override
    public int insert(Friend friend) {
        String sql = "insert into Friend (start,end) values(?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, friend.getStart());
            ps.setInt(2, friend.getEnd());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int insert(int start, int end) {
        int a = insertdoubble(start, end);
        if(a!=0){
            return a;
        } else {
            String sql = "insert into Friend (start,end,state) values(?,?,?)";
            try(Connection conn = DButils.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, start);
                ps.setInt(2, end);
                ps.setInt(3, 1);
                int row1 = ps.executeUpdate();
                PreparedStatement ps1 = conn.prepareStatement(sql);
                ps1.setInt(1, end);
                ps1.setInt(2, start);
                ps1.setInt(3, 1);
                int row2 = ps1.executeUpdate();
                return row1+row2;
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
    public int save(int start, int end) {
        String sql = "insert into Friend (start,end,state) values(?,?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, start);
            ps.setInt(2, end);
            ps.setInt(3, -1);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public void deleteQuest(int start, int end) {
        String sql = "update Friend set state = -2  where start=? and end=? and state = -1";
        try(Connection conn = DButils.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, start);
            ps.setInt(2, end);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean selectQuest(int start, int end) {
        String sql = "select * from Friend where start=? and end=? and state = -1";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, start);
            ps.setInt(2, end);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return true;
            } else return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public int update(Friend friend) {
        String sql = "update Friend set start=?,end=? where id=?";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, friend.getStart());
            ps.setInt(2, friend.getEnd());
            ps.setInt(3, friend.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(int id) {
        String sql = "update Friend set state = ? where id=?";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()) {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, 0);
            ps.setInt(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int delete(int start, int end) {
        String sql = "update Friend set state = ? where start=? and end=? and state = 1";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, 0);
            ps.setInt(2, start);
            ps.setInt(3, end);
            int row1 = ps.executeUpdate();
            PreparedStatement p1 = conn.prepareStatement(sql);
            p1.setInt(1, 0);
            p1.setInt(2, end);
            p1.setInt(3,start);
            int row2 = p1.executeUpdate();
            return row1+row2;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<Integer> findById(int start) {
        String sql = "select end from Friend where start = ? and state = 1";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()) {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, start);
            ResultSet rs = ps.executeQuery();
            List<Integer> list = new ArrayList<>();
            while(rs.next()) {
                int end = rs.getInt("end");
                list.add(end);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Friend> findAll() {
        String sql = "select * from Friend where state=1";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()) {
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            List<Friend> list = new ArrayList<>();
            while(rs.next()) {
                Friend friend = new Friend();
                friend.setId(rs.getInt("id"));
                friend.setStart(rs.getInt("start"));
                friend.setEnd(rs.getInt("end"));
                friend.setDate(rs.getDate("create_date"));
                list.add(friend);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Integer> check (int end) {
        String sql = "select start from Friend where end=? and state=-1";
        try (Connection conn = DButils.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, end);
            ResultSet rs = ps.executeQuery();
            List<Integer> list = new ArrayList<>();
            while(rs.next()) {
                int start = rs.getInt("start");
                list.add(start);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
