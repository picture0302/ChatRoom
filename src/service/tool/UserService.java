package service.tool;

import DAO.UserDAO;
import model.User;

public class UserService {
    public static int login(String username, String password) {
        User user = new User();
        user = new UserDAO().findByUsername(username);
        if (user == null) {
            return -2;
        }else{
           if(user.getState()==1){
               return -1;
           }else{
               if(user.getPassword().equals(password)){
                   new UserDAO().login(user);
                   return 1;
               }else{
                   return 0;
               }
           }
        }
    }
    public static boolean register(String username, String password) {
        User user = new UserDAO().findByUsername(username);
        if (user == null) {
            User user1 = new User();
            user1.setPassword(password);
            user1.setUsername(username);
            if(new UserDAO().insert(user1)!=0){
                return true;
            }
        }
        return false;
    }
    public static boolean offline(String username) {
        User user = new User();
        user = new UserDAO().findByUsername(username);
        if (user == null) {
            return false;
        }else{
            if(new UserDAO().delete(user.getId())!=0){
                return true;
            }else{
                return false;
            }
        }
    }
    public static User getUser(String username) {
        return new UserDAO().findByUsername(username);
    }
}
