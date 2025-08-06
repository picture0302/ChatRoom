package service.tool;

import DAO.FriendDAO;
import DAO.UserDAO;
import model.Friend;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class FriendService {
    public static boolean addFriend(String start,String end) {
        User u1 = UserService.getUser(start);
        User u2 = UserService.getUser(end);
        if (u1 != null && u2 != null) {
            if(new FriendDAO().insert(u1.getId(), u2.getId())!=0){
                return true;
            }
        }
        return false;
    }
    public static List<String> findFriend(String start) {
        User u1 = UserService.getUser(start);
        List<String> list = new ArrayList<String>();
        List<Integer> l1 = new ArrayList<Integer>();
        l1 = new FriendDAO().findById(u1.getId());
        for(Integer i:l1){
            User u2 = new UserDAO().findById(i);
            list.add(u2.getUsername());
        }
        return list;
    }
    public static void saveQuest(String start,String end) {
        User u1 = UserService.getUser(start);
        User u2 = UserService.getUser(end);
        if (u1 != null && u2 != null) {
            new FriendDAO().save(u1.getId(), u2.getId());
        }
    }
    public static void deleteQuest(String start,String end) {
        User u1 = UserService.getUser(start);
        User u2 = UserService.getUser(end);
        if (u1 != null && u2 != null) {
            new FriendDAO().deleteQuest(u1.getId(), u2.getId());
        }
    }

    public static boolean findQuest (String start, String end) {
        User u1 = UserService.getUser(start);
        User u2 = UserService.getUser(end);
        if (u1 != null && u2 != null) {
            return new FriendDAO().selectQuest(u1.getId(), u2.getId());
        }
        return false;
    }
    public static List<String> checkQuest(String end) {
        User u1 = UserService.getUser(end);
        List<String> list = new ArrayList<>();
        List<Integer> l1 = new FriendDAO().check(u1.getId());
        for(Integer i:l1){
            User u2 = new UserDAO().findById(i);
            list.add(u2.getUsername());
        }
        return list;
    }
    public static boolean deleteFriend(String start,String end) {
        User u1 = UserService.getUser(start);
        User u2 = UserService.getUser(end);
        if (u1 != null && u2 != null) {
            if(new FriendDAO().delete(u1.getId(), u2.getId())!=0){
                return true;
            }
        }
        return false;
    }
}
