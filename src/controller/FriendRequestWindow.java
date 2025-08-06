package controller;

import service.tool.FriendService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestWindow extends JFrame {
    private JPanel requestPanel;
    private String currentUser;
    private SocketChannel socketChannel;
    private ChatUI chatUI;
    // 模拟请求数据
    private List<String> friendRequests = new ArrayList<>();

    public FriendRequestWindow(SocketChannel socketChannel,String username,ChatUI chatUI) {
        this.chatUI = chatUI;
        this.socketChannel = socketChannel;
        this.currentUser = username;
        setTitle("好友请求");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        requestPanel = new JPanel();
        requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));  // 垂直排列请求项

        JScrollPane scrollPane = new JScrollPane(requestPanel);
        scrollPane.setPreferredSize(new Dimension(300, 300));  // 设置滚动区域的大小
        add(scrollPane);

        friendRequests = FriendService.checkQuest(username);

        refreshRequests(friendRequests);
        setSize(new Dimension(400, 50 + friendRequests.size() * 60)); // 高度根据请求数动态计算
        setVisible(true);
    }

    // 用于刷新显示请求列表
    public void refreshRequests(List<String> requests) {
        requestPanel.removeAll();  // 清空之前的请求项

        for (String requester : requests) {
            JPanel singleRequestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label = new JLabel(requester + " 请求添加您为好友");

            JButton acceptBtn = new JButton("接受");
            JButton rejectBtn = new JButton("拒绝");

            acceptBtn.addActionListener((ActionEvent e) -> {
                acceptRequest(requester);
                friendRequests.remove(requester);
                refreshRequests(friendRequests);  // 更新界面
            });

            rejectBtn.addActionListener((ActionEvent e) -> {
                rejectRequest(requester);
                friendRequests.remove(requester);
                refreshRequests(friendRequests);  // 更新界面
            });

            singleRequestPanel.add(label);
            singleRequestPanel.add(acceptBtn);
            singleRequestPanel.add(rejectBtn);

            requestPanel.add(singleRequestPanel);
        }

        requestPanel.revalidate();  // 重新验证面板
        requestPanel.repaint();  // 重新绘制面板

        // 根据请求数量调整窗口高度
        setSize(new Dimension(300, 50 + friendRequests.size() * 60));  // 高度动态调整
    }

    private void acceptRequest(String requester) {
        try{
            String credentials = "ACCEPTFRIEND," + requester + "," + currentUser;
            ByteBuffer buffer = ByteBuffer.wrap(credentials.getBytes());
            socketChannel.write(buffer);
            chatUI.refreshfriend(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("已接受来自 " + requester + " 的好友请求");
        JOptionPane.showMessageDialog(this, "你已添加 " + requester + " 为好友");
    }

    private void rejectRequest(String requester) {
        try{
            String credentials = "REJECTFRIEND," + requester + "," + currentUser;
            ByteBuffer buffer = ByteBuffer.wrap(credentials.getBytes());
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: 通知服务器已拒绝好友请求
        System.out.println("已拒绝来自 " + requester + " 的好友请求");
        JOptionPane.showMessageDialog(this, "你已拒绝 " + requester + " 的好友请求");
    }
}
