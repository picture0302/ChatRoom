package controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


import static java.lang.System.exit;

public class LoginFrame extends JFrame {
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private static SocketChannel socketChannel;
    private static ChatUI chatUI;


    public LoginFrame() {}

    public LoginFrame(SocketChannel channel) {
        this.socketChannel = channel;
        setUpUI();
        // 启动Selector线程
        sendHeartbeat();
        new Thread(this::startSelector).start();
    }

    private void setUpUI() {
        setTitle("登录窗口");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setBounds(30, 30, 60, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 30, 150, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(30, 70, 60, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 70, 150, 25);
        add(passwordField);

        JButton loginButton = new JButton("登录");
        loginButton.setBounds(50, 110, 80, 25);
        add(loginButton);

        JButton registerButton = new JButton("注册");
        registerButton.setBounds(150, 110, 80, 25);
        add(registerButton);
        //登录
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String credentials = "LOGIN," + usernameField.getText() + "," + new String(passwordField.getPassword());
                    ByteBuffer buffer = ByteBuffer.wrap(credentials.getBytes());
                    socketChannel.write(buffer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginFrame.this, "连接服务器失败");
                }
            }
        });
        //注册
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String credentials = "REGISTER," + usernameField.getText() + "," + new String(passwordField.getPassword());
                    ByteBuffer buffer = ByteBuffer.wrap(credentials.getBytes());
                    socketChannel.write(buffer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginFrame.this, "连接服务器失败");
                }
            }
        });

        setVisible(true);
    }
    //检测服务端发来的事件
    private void startSelector() {
        try {
            Selector selector = Selector.open();
            // 等待连接完成
            while (!socketChannel.finishConnect()) {
                Thread.sleep(50);
            }
            socketChannel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {
                selector.select(); // 阻塞直到有事件发生
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        buffer.clear();
                        int bytesRead = sc.read(buffer);
                        if (bytesRead > 0) {
                            buffer.flip();
                            String response = new String(buffer.array(), 0, buffer.limit());
                            // Swing UI更新需要切换到事件线程，在处理线程事件时可以直接反映到UI界面上
                            SwingUtilities.invokeLater(() -> handleServerResponse(response));
                        } else if (bytesRead == -1) {
                            sc.close();
                            System.out.println("服务端关闭连接");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //处理服务端发来的消息
    private void handleServerResponse(String response) {
        if (!response.startsWith("MESSAGE")){
            JOptionPane.showMessageDialog(this, response);
        }
        String username = usernameField.getText();
        try {
            if ("登录成功".equals(response)) {
                chatUI = new ChatUI(username, socketChannel);
                dispose();
            } else if ("成功添加好友".equals(response)) {
                if (chatUI != null) {
                    chatUI.refreshfriend(username);
                }
            } else if ("成功删除好友".equals(response)) {
                if (chatUI != null) {
                    chatUI.refreshfriend(username);
                }
            } else if (response.startsWith("MESSAGE")) {
                String[] message = response.split(",");
                String action1 = message[1];
                String action2 = message[2];
                String action3 = message[3];
                chatUI.receiveMessage(action1,action2,action3);
            } else if (response.contains("群聊已解散")) {
                if (chatUI != null) {
                    chatUI.refreshgroup(username);
                }
            } else if (response.equals("您已被管理员强制下线")) {
                exit(0);
                socketChannel.close();
            } else if (response.contains("的群主将您移除群聊")) {
                if (chatUI != null) {
                    chatUI.refreshgroup(username);
                }
            } else if (response.contains("您的心跳长时间暂停，还请下线休息")) {
                showHeartbeatTimeoutDialog(this,10000,username);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHeartbeat() {
        new Thread(() -> {
            try {
                while (true) {
                    String re = "PING,-,-";
                    ByteBuffer buffer = ByteBuffer.wrap(re.getBytes());
                    socketChannel.write(buffer);
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //检查心跳包
    public static void showHeartbeatTimeoutDialog(Component parent, int timeoutMillis,String username) {
        final JDialog dialog = new JDialog((Frame) null, "心跳超时提醒", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parent);

        JLabel label = new JLabel("是否继续使用？", SwingConstants.CENTER);
        dialog.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton yesButton = new JButton("继续使用");
        JButton noButton = new JButton("下线退出");
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        final boolean[] userResponded = {false};

        yesButton.addActionListener(e -> {
            userResponded[0] = true;
            dialog.dispose(); // 继续使用
        });

        noButton.addActionListener(e -> {
            userResponded[0] = true;
            dialog.dispose();
            chatUI.quit(username);
             // 用户主动选择退出
        });

        // 定时器：timeout 后自动关闭对话框
        Timer timer = new Timer(timeoutMillis, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!userResponded[0]) {
                    dialog.dispose();
                    chatUI.quit(username); // 用户超时未响应，退出
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
        dialog.setVisible(true);
    }
}
