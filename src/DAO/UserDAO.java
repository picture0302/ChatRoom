package DAO;

import model.User;
import utils.DButils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements BaseDao<User>{
    @Override
    public int insert(User user) {
        String sql = "insert into user(name,password)"+"values (?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = DButils.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            if (ps.executeUpdate() == 0){
                throw new SQLException("创建用户失败，没有行受到影响");
            }
            try(ResultSet rs = ps.getGeneratedKeys()) {
                if(rs.next()){
                    return rs.getInt(1);
                }else{
                    throw new SQLException("创建用户失败，无用户id返回值");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DButils.close(conn, ps, null);
        }
    }


    @Override
    public int update(User user) {
        String sql = "update user set name=?,password=? where id=? and state = 1";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()){
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("数据更新有误");
            e.printStackTrace();
            return 0;
        }
    }
    public int login(User user) {
        String sql = "update user set state=1 where id=?";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()){
            ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("数据更新有误");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(int id) {
        String sql = "update user set state=? where id=?";
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

    public User findById(int id) {
        String sql = "select * from user where id=? ";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()) {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                User user = new User();
                user.setUsername(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                user.setDate(rs.getDate("created_id"));
                user.setState(rs.getInt("state"));
                return user;
            }else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public User findByUsername(String username) {
        String sql = "select * from user where name=? ";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()) {
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                user.setDate(rs.getDate("created_id"));
                user.setState(rs.getInt("state"));
                return user;
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List findAll() {
        String sql = "select * from user where state=1";
        PreparedStatement ps = null;
        try(Connection conn = DButils.getConnection()) {
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            List<User> list = new ArrayList<>();
            while(rs.next()){
                User user = new User();
                user.setUsername(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                user.setState(rs.getInt("state"));
                list.add(user);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
