package DAO;

import model.Chatgroup;
import utils.DButils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatgroupDAO implements BaseDao<Chatgroup>{
    @Override
    public int insert(Chatgroup chatgroup) {
        String sql="insert into chatgroup (owner_id,name,state)"+"values(?,?,?)";
        PreparedStatement ps=null;
        try(Connection conn = DButils.getConnection()) {
            ps=conn.prepareStatement(sql);
            ps.setInt(1,chatgroup.getOwnerId());
            ps.setString(2,chatgroup.getName());
            ps.setInt(3,chatgroup.getState());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int insert(int ownerId, String name) {
        String sql="insert into chat_group (owner_id,name,state)"+"values(?,?,?)";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setInt(1,ownerId);
            ps.setString(2,name);
            ps.setInt(3,1);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    @Override
    public int update(Chatgroup chatgroup) {
        String sql="update chatgroup set name=?,owner_id=? where id=? and state = 1";
        PreparedStatement ps=null;
        try(Connection conn = DButils.getConnection()) {
            ps=conn.prepareStatement(sql);
            ps.setString(1,chatgroup.getName());
            ps.setInt(2,chatgroup.getOwnerId());
            ps.setInt(3,chatgroup.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(int id) {
        String sql="update chatgroup set state = ? where id=?";
        PreparedStatement ps=null;
        try(Connection conn = DButils.getConnection()) {
            ps=conn.prepareStatement(sql);
            ps.setInt(1,0);
            ps.setInt(2,id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int delete(String name) {
        String sql="update chat_group set state = ? where name=?";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setInt(1,0);
            ps.setString(2,name);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    //加上判断群是否存在，就会导致删群时，没办法删群成员，不加吧，又会导致再加用同一个名字的群时，聊天记录会被刷出来，怎么办？重载？
    public Chatgroup findByName(String name) {
        String sql="select * from chat_group where name=?";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setString(1,name);
            ResultSet rs=ps.executeQuery();
            if(rs.next()) {
                Chatgroup chatgroup=new Chatgroup();
                chatgroup.setId(rs.getInt("id"));
                chatgroup.setName(rs.getString("name"));
                chatgroup.setState(rs.getInt("state"));
                chatgroup.setOwnerId(rs.getInt("owner_id"));
                chatgroup.setDate(rs.getDate("date"));
                return chatgroup;
            }else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Chatgroup findById(int id) {
        String sql="select * from chat_group where id=?";
        try(Connection conn = DButils.getConnection()) {
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setInt(1,id);
            ResultSet rs=ps.executeQuery();
            if(rs.next()) {
                Chatgroup chatgroup=new Chatgroup();
                chatgroup.setName(rs.getString("name"));
                chatgroup.setState(rs.getInt("state"));
                chatgroup.setOwnerId(rs.getInt("owner_id"));
                chatgroup.setDate(rs.getDate("date"));
                return chatgroup;
            }else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public List<Chatgroup> findAll() {
        String sql="select * from chatgroup";
        PreparedStatement ps=null;
        try(Connection conn = DButils.getConnection()) {
            ps=conn.prepareStatement(sql);
            ResultSet rs=ps.executeQuery();
            List<Chatgroup> chatgroups=new ArrayList<Chatgroup>();
            while(rs.next()) {
                Chatgroup chatgroup=new Chatgroup();
                chatgroup.setName(rs.getString("name"));
                chatgroup.setState(rs.getInt("state"));
                chatgroup.setOwnerId(rs.getInt("owner_id"));
                chatgroup.setDate(rs.getDate("date"));
                chatgroups.add(chatgroup);
            }
            return chatgroups;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
