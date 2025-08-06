package service.tool;

import DAO.ChatgroupDAO;
import DAO.MessageDAO;
import DAO.UserDAO;
import model.Chatgroup;
import model.Message;
import model.User;

import java.util.ArrayList;

public class MessageService {
    public static boolean addUserMessage(String sender,String receiver, String message) {
        User u1 = new UserDAO().findByUsername(sender);
        User u2 = new UserDAO().findByUsername(receiver);
        if (u1 != null && u2 != null) {
            if(new MessageDAO().insertUser(u1.getId(), u2.getId(), message)!=0){
                return true;
            }
        }
        return false;
    }
    public static ArrayList<Message> getUserMessages(String name1, String name2) {
        User u1 = new UserDAO().findByUsername(name1);
        User u2 = new UserDAO().findByUsername(name2);
        ArrayList<Message> messages = new ArrayList<>();
        if (u1 != null && u2 != null) {
            messages = new MessageDAO().findUserChat(u1.getId(), u2.getId());
        }
        return messages;
    }
    public static ArrayList<Message> getGroupMessages(String name) {
        Chatgroup g2 = new ChatgroupDAO().findByName(name);
        ArrayList<Message> messages = new ArrayList<>();
        if (g2 != null) {
            messages = new MessageDAO().findGroupChat(g2.getId());
        }
        return messages;
    }
    public static boolean addGroupMessage(String sender,String group, String message) {
        User u1 = new UserDAO().findByUsername(sender);
        Chatgroup g2 = new ChatgroupDAO().findByName(group);
        if (u1 != null && g2 != null) {
            if(new MessageDAO().insertGroup(u1.getId(), g2.getId(), message)!=0){
                return true;
            }
        }
        return false;
    }
    public static boolean deleteUserChat(String sender, String receiver) {
        User u1 = new UserDAO().findByUsername(sender);
        User u2 = new UserDAO().findByUsername(receiver);
        if (u1 != null && u2 != null) {
            return new MessageDAO().deleteUserChat(u1.getId(), u2.getId());
        }
        return false;
    }
}
