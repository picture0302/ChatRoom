package service.tool;

import DAO.ChatgroupDAO;
import DAO.MemberDAO;
import DAO.UserDAO;
import model.Chatgroup;
import model.Member;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class MemberService {
    public static boolean addMember(String username,String group) {
        Chatgroup chatgroup = new ChatgroupDAO().findByName(group);
        User user = new UserDAO().findByUsername(username);
        if(user!=null&&chatgroup!=null) {
            if(new MemberDAO().insert(chatgroup.getId(), user.getId())!=0){
                return true;
            }
        }
        return false;
    }
    public static Member getMember(String username,String group) {
        User user = new UserDAO().findByUsername(username);
        Chatgroup chatgroup = new ChatgroupDAO().findByName(group);
        Member member = null;
        if(user!=null&&chatgroup!=null) {
            member = new MemberDAO().getMember(user.getId(), chatgroup.getId());
        }
        return member;
    }
    public static List<String> findGroups (String username) {
        List<String> groups = new ArrayList<String>();
        List<Integer> groupsId = new ArrayList<Integer>();
        User user = new UserDAO().findByUsername(username);
        if(user!=null) {
            groupsId = new MemberDAO().findGroups(user.getId());
        }
        for(Integer groupId : groupsId) {
            Chatgroup chatgroup = new ChatgroupDAO().findById(groupId);
            if(chatgroup!=null) {
                groups.add(chatgroup.getName());
            }
        }
        return groups;
    }
    public static List<String> findMembers(String group) {
        Chatgroup chatgroup = new ChatgroupDAO().findByName(group);
        List<String> m1 =new ArrayList<>();
        if(chatgroup!=null) {
            List<Integer> m = new MemberDAO().findMembers(chatgroup.getId());
            for(Integer memberId : m) {
                User user = new UserDAO().findById(memberId);
                if(user!=null) {
                    m1.add(user.getUsername());
                }
            }
        }
        return m1;
    }
    public static List<String> removeMember(String group) {
        Chatgroup chatgroup = new ChatgroupDAO().findByName(group);
        List<String> list = new ArrayList<>();
        if(chatgroup!=null) {
            List<Integer> l1 = new MemberDAO().findMembers(chatgroup.getId());
            for(Integer memberId : l1) {
                User member = new UserDAO().findById(memberId);
                if(member!=null) {
                    list.add(member.getUsername());
                }
            }
            new MemberDAO().dissolveGroups(chatgroup.getId());
        }
        return list;
    }
    public static boolean removeMember(String username, String group) {
        User user = new UserDAO().findByUsername(username);
        Chatgroup chatgroup = new ChatgroupDAO().findByName(group);
        if(user!=null&&chatgroup!=null) {
            if(new MemberDAO().deleteGroup(user.getId(), chatgroup.getId())!=0){
                return true;
            }
        }
        return false;
    }
    public static boolean banMember(String username, String group) {
        User user = new UserDAO().findByUsername(username);
        Chatgroup chatgroup = new ChatgroupDAO().findByName(group);
        if(user!=null&&chatgroup!=null) {
            if(new MemberDAO().banMember(user.getId(), chatgroup.getId())!=0){
                return true;
            }
        }
        return false;
    }
    public static boolean unbanMember(String username, String group) {
        User user = new UserDAO().findByUsername(username);
        Chatgroup chatgroup = new ChatgroupDAO().findByName(group);
        if(user!=null&&chatgroup!=null) {
            if(new MemberDAO().unbanMember(user.getId(), chatgroup.getId())!=0){
                return true;
            }
        }
        return false;
    }
}
