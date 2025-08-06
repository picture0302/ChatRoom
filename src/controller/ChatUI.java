package controller;

import DAO.ChatgroupDAO;
import DAO.UserDAO;
import model.Chatgroup;
import model.Member;
import model.Message;
import model.User;
import service.tool.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel currentChatLabel;

    private DefaultListModel<String> friendModel = new DefaultListModel<>();
    private DefaultListModel<String> groupModel = new DefaultListModel<>();
    private JList<String> friendList = new JList<>(friendModel);
    private JList<String> groupList = new JList<>(groupModel);
    private JList<String> friendJList;
    private DefaultListModel<String> friendListModel;
    private SocketChannel socketChannel;
    private String username;

    private String currentTarget = null;
    private boolean isGroupChat = false;

    public ChatUI() {}
    public ChatUI(String username,SocketChannel socketChannel) throws IOException {
        this.username = username;
        this.socketChannel = socketChannel;
        setTitle("聊天室 - 用户: " + username);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        refreshfriend(username);
        refreshgroup(username);

        // 左侧列表布局
        JPanel listPanel = new JPanel(new GridLayout(2, 1));
        listPanel.setPreferredSize(new Dimension(180, 0));

        JPanel friendPanel = new JPanel(new BorderLayout());
        friendPanel.add(new JLabel("好友列表", SwingConstants.CENTER), BorderLayout.NORTH);
        friendPanel.add(new JScrollPane(friendList), BorderLayout.CENTER);


        JPanel groupPanel = new JPanel(new BorderLayout());
        groupPanel.add(new JLabel("群聊列表", SwingConstants.CENTER), BorderLayout.NORTH);
        groupPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);

        listPanel.add(friendPanel);
        listPanel.add(groupPanel);

        // 聊天区域
        currentChatLabel = new JLabel("未选择聊天对象");
        JButton viewMembersButton = new JButton("查看群成员");
        viewMembersButton.setEnabled(false); // 初始不可点击
        viewMembersButton.addActionListener(e -> {
            if (currentTarget != null && isGroupChat) {
                showGroupMembersWithMuteOption(currentTarget);
            }
        });

        JPanel topChatPanel = new JPanel(new BorderLayout());
        topChatPanel.add(currentChatLabel, BorderLayout.CENTER);
        topChatPanel.add(viewMembersButton, BorderLayout.EAST);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("发送");
        sendButton.addActionListener(e -> sendMessage());

        JButton sendFileButton = new JButton("发送文件");
        sendFileButton.addActionListener(e -> sendFile());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(sendFileButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);


        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(topChatPanel, BorderLayout.NORTH);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // ---- 好友列表 ----
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = friendList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedFriend = friendModel.getElementAt(index);
                        // 右键点击
                        if (SwingUtilities.isRightMouseButton(e)) {
                            int option = JOptionPane.showConfirmDialog(
                                    null,
                                    "是否删除好友：" + selectedFriend + "？",
                                    "删除好友",
                                    JOptionPane.YES_NO_OPTION
                            );
                            if (option == JOptionPane.YES_OPTION) {
                                // 调用删除方法
                                try {
                                    String s = "DELETEFRIEND,"+username+","+selectedFriend;
                                    ByteBuffer b = ByteBuffer.wrap(s.getBytes());
                                    socketChannel.write(b);
                                    chatArea.setText("");
                                    currentChatLabel.setText("当前私聊对象: " + null);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        // 左键点击（私聊）
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            currentTarget = selectedFriend;
                            isGroupChat = false;
                            chatArea.setText("");
                            currentChatLabel.setText("当前私聊对象: " + currentTarget);
                            uploadUserchat();
                        }
                    }
                }
            }
        });

        // ---- 群聊列表 ----
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = groupList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedGroup = groupModel.getElementAt(index);
                        // 右键点击
                        Chatgroup sGroup = new ChatgroupDAO().findByName(selectedGroup);
                        User sUser = new UserDAO().findByUsername(username);
                        if(sGroup.getOwnerId()==sUser.getId()){
                            if (SwingUtilities.isRightMouseButton(e)) {
                                int option = JOptionPane.showConfirmDialog(
                                        null,
                                        "是否解散群聊：" + selectedGroup + "？",
                                        "解散群聊",
                                        JOptionPane.YES_NO_OPTION
                                );
                                if (option == JOptionPane.YES_OPTION) {
                                    // 调用删除方法
                                    try {
                                        String s = "DISSOLVE_GROUP,"+selectedGroup+",_";
                                        ByteBuffer b = ByteBuffer.wrap(s.getBytes());
                                        socketChannel.write(b);
                                        currentChatLabel.setText("当前群聊对象: " + null);
                                        chatArea.setText("");
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                int option = JOptionPane.showConfirmDialog(
                                        null,
                                        "是否退出群聊：" + selectedGroup + "？",
                                        "退出群聊",
                                        JOptionPane.YES_NO_OPTION
                                );
                                if (option == JOptionPane.YES_OPTION) {
                                    // 调用删除方法
                                    try {
                                        String s = "DELETE_GROUP,"+selectedGroup+","+username;
                                        ByteBuffer b = ByteBuffer.wrap(s.getBytes());
                                        socketChannel.write(b);
                                        groupModel.removeElement(currentTarget);
                                        currentChatLabel.setText("当前群聊对象: " + null);
                                        chatArea.setText("");
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }

                        // 左键点击（群聊）
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            currentTarget = selectedGroup;
                            isGroupChat = true;
                            chatArea.setText("");
                            currentChatLabel.setText("当前群聊对象: " + currentTarget);
                            viewMembersButton.setEnabled(true);
                            uploadGroupchat();
                        }
                    }
                }
            }
        });

        // 主布局
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, chatPanel);
        splitPane.setDividerLocation(180);
        splitPane.setEnabled(false);
        add(splitPane);

        // ---- 菜单栏 ----
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("操作");

        JMenuItem addFriendItem = new JMenuItem("添加好友");
        addFriendItem.addActionListener(e -> addFriend(username));

        JMenuItem createGroupItem = new JMenuItem("创建群聊");
        createGroupItem.addActionListener(e -> createGroup());

        JMenuItem joinGroupItem = new JMenuItem("加入群聊");
        joinGroupItem.addActionListener(e -> joinGroup());

        JMenuItem quitItem = new JMenuItem("退出登录");
        quitItem.addActionListener(e -> quit(username));

        menu.add(addFriendItem);
        menu.add(createGroupItem);
        menu.add(joinGroupItem);
        menu.add(quitItem);
        menuBar.add(menu);

        JMenu notificationMenu = new JMenu("通知");
        ChatUI chatUI = new ChatUI();
        JMenuItem viewNotificationsItem = new JMenuItem("新朋友");
        viewNotificationsItem.addActionListener(e -> {
            new FriendRequestWindow(socketChannel,username,chatUI);
        });

        notificationMenu.add(viewNotificationsItem);

        menuBar.add(menu);
        menuBar.add(notificationMenu);

        // 确保在setVisible之前设置菜单栏
        setJMenuBar(menuBar);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(
                        ChatUI.this,
                        "确定要退出聊天吗？",
                        "确认退出",
                        JOptionPane.YES_NO_OPTION
                );
                if (option == JOptionPane.YES_OPTION) {
                    quit(getTitle().replace("聊天室 - 用户: ", "")); // 提取用户名并退出
                } else {
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // 取消关闭
                }
            }
        });

        // 最后设置可见
        setVisible(true);
        refreshfriend(username);
    }
    //发文件
    private void sendFile() {
        if (currentTarget == null) {
            JOptionPane.showMessageDialog(this, "请先选择聊天对象！");
            return;
        }
        Member member = MemberService.getMember(username, currentTarget);
        if(member != null && member.getState() == -1){
            JOptionPane.showMessageDialog(this, "您已被禁言");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName();
            long fileSize = file.length();

            // 判断文件类型（简单判断）
            String fileType = "file";

            // 发送文件元信息（标志、发送者、接收者、文件名、大小、类型）
            String header;
            if(isGroupChat){
                header = "FILE," +"Group,"+ username + "," + currentTarget + "," + fileName + "," + fileSize + "," + fileType;
            } else {
                header = "FILE," + "User," +username + "," + currentTarget + "," + fileName + "," + fileSize + "," + fileType;
            }
            try {
                // 先发送文件的元信息
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress("localhost", 8888));
                socketChannel.configureBlocking(false);
                socketChannel.write(ByteBuffer.wrap(header.getBytes()));
                // 发送文件内容
                String sender = "[我]";
                appendFileMessage(sender,fileName,fileSize);
                listen(socketChannel,file);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "发送文件失败");
            }
        }
    }
    //监听服务器是否准备好接收文件，然后发文件
    private void listen(SocketChannel socketChannel, File file){
        new Thread(() -> {
            try {
                while (true){
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = socketChannel.read(buffer);
                    if (bytesRead == -1) {
                        JOptionPane.showMessageDialog(this, "服务器断开连接");
                        return;
                    }
                    buffer.flip();
                    String response = new String(buffer.array());
                    if(response.contains("READY_FOR_FILE")){
                        Path path = file.toPath();
                        //用filechannel打开文件
                        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
                            ByteBuffer buffer2 = ByteBuffer.allocate(8192);
                            int bytesReadFromFile;
                            while ((bytesReadFromFile = fileChannel.read(buffer2)) != -1) {
                                buffer2.flip(); // 切换为读模式
                                while (buffer2.hasRemaining()) {
                                    socketChannel.write(buffer2); // 将缓冲区内容写入 SocketChannel
                                }
                                buffer2.clear(); // 清空缓冲区，准备下一次读取
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }
    //展示群成员，并进行操作
    private void showGroupMembersWithMuteOption(String groupName) {
        List<String> members = MemberService.findMembers(groupName);
        if (members == null || members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该群暂无成员");
            return;
        }
        Chatgroup group = new ChatgroupDAO().findByName(groupName);
        User currentUser = new UserDAO().findByUsername(username);
        boolean isOwner = group.getOwnerId() == currentUser.getId();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String m : members) {
            User user = new UserDAO().findByUsername(m);
            if(user.getId()==group.getOwnerId()){
                model.addElement(m+" (群主)");
            } else model.addElement(m);
        }
        //左键双击
        JList<String> memberList = new JList<>(model);
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (isOwner && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int index = memberList.locationToIndex(e.getPoint());
                    String targetUser = model.get(index);
                    if (!targetUser.equals(username)) {
                        String[] options = {"禁言", "取消禁言", "踢出群聊", "取消"};
                        int choice = JOptionPane.showOptionDialog(
                                null,
                                "对成员 " + targetUser + " 进行操作：",
                                "群主操作",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                options,
                                options[0]
                        );

                        String command = null;

                        switch (choice) {
                            case 0: // 禁言
                                command = "MUTE_USER," + groupName + "," + targetUser;
                                break;
                            case 1: // 取消禁言
                                command = "UNMUTE_USER," + groupName + "," + targetUser;
                                break;
                            case 2: // 踢出群聊
                                command = "KICK_USER," + groupName + "," + targetUser;
                                model.remove(index); // 直接从显示列表移除
                                break;
                            default:
                                return;
                        }

                        if (command != null) {
                            try {
                                socketChannel.write(ByteBuffer.wrap(command.getBytes()));
                                JOptionPane.showMessageDialog(null, "操作已发送：" + options[choice]);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(memberList);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        JOptionPane.showMessageDialog(this, scrollPane, "群成员 - " + groupName, JOptionPane.INFORMATION_MESSAGE);
    }
    //退出
    public void quit(String name) {
        try{
            String credentials = "QUIT,"+name+",_";
            ByteBuffer buffer = ByteBuffer.wrap(credentials.getBytes());
            socketChannel.write(buffer);
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //发送保存文件的请求
    private void appendFileMessage(String senderName, String fileName,long fileSize) {
        JLabel linkLabel = new JLabel("<html><a href='#'>" + fileName + "</a></html>");
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
                int option = fileChooser.showSaveDialog(ChatUI.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fileChooser.getSelectedFile();
                    try {
                        String request = "DOWNLOAD_FILE," + fileName + ",_";
                        SocketChannel socketChannel = SocketChannel.open();
                        socketChannel.connect(new InetSocketAddress("localhost", 8888));
                        socketChannel.configureBlocking(false);

                        socketChannel.write(ByteBuffer.wrap(request.getBytes()));

                        listenFile(socketChannel,saveFile,fileSize);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ChatUI.this, "发送下载请求失败");
                    }
                }
            }
        });
        if(!senderName.contains("我")){
            JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filePanel.add(new JLabel(senderName + " 发送了文件: "));
            filePanel.add(linkLabel);

            JOptionPane.showMessageDialog(this, filePanel, "文件消息", JOptionPane.INFORMATION_MESSAGE);
        }
        // 也可以保留文字信息在聊天窗口（可选）
        chatArea.append(senderName + " 发送了文件: " + fileName + "\n");
    }
    //监听从服务端发来的文件
    private void listenFile(SocketChannel socketChannel, File destFile,long fileSize){
        new Thread(() -> {
            try{
                while (true){
                    //准备接受文件内容
                    Path path = destFile.toPath();
                    try(FileChannel fc = (FileChannel) Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)){
                        socketChannel.configureBlocking(true);
                        ByteBuffer buffer = ByteBuffer.allocate(8192);
                        long filereceived = 0;
                        while(filereceived<fileSize){
                            buffer.clear();
                            int bytesread = socketChannel.read(buffer);
                            if(bytesread == -1) break;
                            buffer.flip();
                            fc.write(buffer);
                            filereceived += bytesread;
                        }
                        socketChannel.configureBlocking(false);
                        if(filereceived == fileSize || filereceived > fileSize){
                            JOptionPane.showMessageDialog(this, "文件保存成功：" + destFile.getAbsolutePath());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }
    //发消息
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (currentTarget == null) {
            JOptionPane.showMessageDialog(this, "请先选择聊天对象！");
            return;
        }
        if (!text.isEmpty()) {
            String message;
            Member member = MemberService.getMember(username, currentTarget);
            if (member == null||member.getState()==1) {
                message = "[我]:" + text + "\n";
                chatArea.append(message);
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
                inputField.setText("");
                try{
                    String information ;
                    if(isGroupChat){
                        information = "GROUP_CHAT,"+username+","+currentTarget+","+text;
                    } else {
                        information = "USER_CHAT,"+username+","+currentTarget+","+text;
                    }
                    ByteBuffer buffer = ByteBuffer.wrap(information.getBytes());
                    socketChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "消息发送失败");
                }
            }
            if(member != null && member.getState()==-1){
                JOptionPane.showMessageDialog(this, "您已被禁言");
            }
            // TODO: 发送消息到服务器（isGroupChat 区分私聊/群聊）
        }
    }
    //添加朋友
    private void addFriend(String start) {
        String name = JOptionPane.showInputDialog(this, "输入要添加的好友用户名：");
        if (name != null && !name.trim().isEmpty()) {
            if (!friendModel.contains(name)){
                User user = UserService.getUser(name);
                if(user != null) {
                    if(FriendService.findQuest(username,name)){
                        JOptionPane.showMessageDialog(this,"您已发送过请求");
                    } else {
                        try {
                            String credentials = "ADD_FRIEND"+","+start + "," + name;
                            ByteBuffer buffer = ByteBuffer.wrap(credentials.getBytes());
                            socketChannel.write(buffer);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(ChatUI.this, "连接服务器失败");
                        }
                    }
                }else{
                    JOptionPane.showMessageDialog(this,"该用户不存在,请重新输入");
                }
                // TODO: 通知服务器添加好友
            } else {
                JOptionPane.showMessageDialog(this, "好友已存在！");
            }
        }
    }
    //刷新好友列表
    public void refreshfriend(String name) {
        List<String> friends = FriendService.findFriend(name);
        SwingUtilities.invokeLater(() -> {
            if(friendModel!=null){
                friendModel.clear();
            }
            for (String f : friends) {
                if (!f.isEmpty()) friendModel.addElement(f);
            }
        });
    }
    //刷新群列表
    public void refreshgroup(String name) {
        List<String> groups = MemberService.findGroups(name);
        SwingUtilities.invokeLater(() -> {
            if(groupModel!=null){
                groupModel.clear();
            }
            for (String g : groups) {
                if (!g.isEmpty()) groupModel.addElement(g);
            }
        });
    }
    //创建群聊
    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "输入新建群聊名称：");
        if (groupName != null && !groupName.trim().isEmpty()) {
            if (!groupModel.contains(groupName)) {
                String information = "CREATE_GROUP,"+groupName+","+username;
                try {
                    ByteBuffer buffer = ByteBuffer.wrap(information.getBytes());
                    socketChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                groupModel.addElement(groupName);
                // TODO: 通知服务器创建群聊
            } else {
                JOptionPane.showMessageDialog(this, "群聊已存在！");
            }
        }
    }
    //加入群聊
    private void joinGroup() {
        String groupName = JOptionPane.showInputDialog(this, "输入要加入的群聊名称：");
        if (groupName != null && !groupName.trim().isEmpty()) {
            if (!groupModel.contains(groupName)) {
                Chatgroup group = GroupService.getGroup(groupName);
                if (group != null) {
                    if(group.getState()==1){
                        String information = "JOIN_GROUP,"+groupName+","+username;
                        ByteBuffer buffer = ByteBuffer.wrap(information.getBytes());
                        try {
                            socketChannel.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        groupModel.addElement(groupName);
                    } else {
                        JOptionPane.showMessageDialog(this, "该群聊已解散，请重新输入");
                    }
                } else{
                    JOptionPane.showMessageDialog(this, "该群聊不存在，请重新输入");
                }

                // TODO: 通知服务器加入群聊
            } else {
                JOptionPane.showMessageDialog(this, "已加入该群聊！");
            }
        }
    }
    //处理收到的消息
    public void receiveMessage(String sender,String message,String group) {
        if (isGroupChat && group.equals(currentTarget) && !sender.contains(username)) {
            // 处理群聊消息
            File file = new File(message);
            if(file.exists()&&file.length()!=0){
                appendFileMessage(sender, file.getName(), file.length());
            } else chatArea.append("["+sender+"]"+": " + message + "\n");
        }
        if (!isGroupChat && currentTarget != null && !sender.contains(currentTarget)) {
            // 只处理当前私聊对象的消息
            File file = new File(message);
            if(file.exists()&&file.length()!=0){
                appendFileMessage(sender, file.getName(), file.length());
            } else chatArea.append("["+sender+"]"+": " + message + "\n");
        }
    }
    //加载私聊消息
    public void uploadUserchat(){
        ArrayList <Message> list = new ArrayList <>();
        list = MessageService.getUserMessages(username,currentTarget);
        for(Message m : list){
            if(m.getType().equals("text")){
                int id = m.getSenderId();
                User u1 = new UserDAO().findById(id);
                if(u1!=null&&u1.getUsername().equals(username)){
                    chatArea.append(wrapText("[我]：" + m.getMessage(), 40));
                } else {
                    chatArea.append(wrapText("["+currentTarget+"]:"+m.getMessage(),40));
                }
            }
            if (m.getType().equals("file")) {
                int id = m.getSenderId();
                User u1 = new UserDAO().findById(id);
                String senderName = (u1 != null && u1.getUsername().equals(username)) ? "[我]" : "[" + u1.getUsername() + "]";
                File file = new File(m.getMessage());
                appendFileMessage(senderName, file.getName(), file.length());
            }
        }
    }
    //加载群聊消息
    public void uploadGroupchat(){
        ArrayList <Message> list = new ArrayList <>();
        list = MessageService.getGroupMessages(currentTarget);
        for(Message m : list){
            if(m.getType().equals("text")){
                int id = m.getSenderId();
                User u1 = new UserDAO().findById(id);
                if(u1!=null&&u1.getUsername().equals(username)){
                    chatArea.append(wrapText("[我]：" + m.getMessage(), 40));
                } else {
                    chatArea.append(wrapText("["+u1.getUsername()+"]:"+m.getMessage(),40));
                }
            }
            if(m.getType().equals("file")){
                String savefile = m.getMessage();
                File f = new File(savefile);
                User u = new UserDAO().findById(m.getSenderId());
                String sender;
                if(u!=null&&u.getUsername().equals(username)){
                    sender = "[我]";
                } else {
                    sender = "[" + u.getUsername() + "]";
                }
                appendFileMessage(sender,f.getName(),f.length());
            }
        }
    }
    //分割输出文字
    public String wrapText(String text, int lineLimit) {
        StringBuilder sb = new StringBuilder();
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            length++;
            if (length >= lineLimit) {
                sb.append("\n");
                length = 0;
            }
        }
        sb.append("\n");
        return sb.toString();
    }

}