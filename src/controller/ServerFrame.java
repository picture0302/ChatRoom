package controller;

import javax.swing.*;
import java.awt.*;

public class ServerFrame extends JFrame {
    private JTextArea logArea;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;

    // 回调接口：用于服务端逻辑中执行强制下线
    public interface KickUserListener {
        void kickUser(String username);
    }

    private KickUserListener kickUserListener;

    public void setKickUserListener(KickUserListener listener) {
        this.kickUserListener = listener;
    }

    public ServerFrame() {
        setTitle("聊天室服务端");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("服务器日志"));
        add(logScroll, BorderLayout.CENTER);

        // 在线用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder("在线用户"));
        userScroll.setPreferredSize(new Dimension(150, 0));
        add(userScroll, BorderLayout.EAST);

        // 按钮区域
        JPanel buttonPanel = new JPanel();
        JButton kickButton = new JButton("强制下线");
        buttonPanel.add(kickButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 按钮事件处理
        kickButton.addActionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "确定要强制下线用户: " + selectedUser + " 吗？",
                        "确认操作",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (kickUserListener != null) {
                        kickUserListener.kickUser(selectedUser);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要踢下线的用户");
            }
        });

        setVisible(true);
    }

    public void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    public void addUser(String username) {
        SwingUtilities.invokeLater(() -> {
            if (!userListModel.contains(username)) {
                userListModel.addElement(username);
            }
        });
    }

    public void removeUser(String username) {
        SwingUtilities.invokeLater(() -> userListModel.removeElement(username));
    }
}
