package service.tool;

import DAO.ChatgroupDAO;
import DAO.UserDAO;
import model.Chatgroup;
import model.User;

import javax.swing.*;

public class GroupService {
    public static boolean addGroup(String owner,String name) {
        User user = new UserDAO().findByUsername(owner);
        if (user != null) {
            if(new ChatgroupDAO().insert(user.getId(), name)!=0) {
                return true;
            }
        }
        return false;
    }
    public static boolean deleteGroup(String name) {
        if(new ChatgroupDAO().delete(name)!=0){
            return true;
        }
        return false;
    }
    public static Chatgroup getGroup(String name) {
        Chatgroup group = new ChatgroupDAO().findByName(name);
        return group;
    }
}
