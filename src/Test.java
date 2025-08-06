import DAO.UserDAO;
import model.User;

public class Test {
    public static void main(String[] args) {
        int id = 1;
        User u = new UserDAO().findById(1);
        System.out.println(u);
    }
}
