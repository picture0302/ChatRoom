package controller;

import DAO.ChatgroupDAO;
import DAO.MessageDAO;
import DAO.UserDAO;
import controller.ServerFrame;
import model.Chatgroup;
import model.Member;
import model.Message;
import model.User;
import service.tool.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;


public class Server {
    private static Selector selector;
    private static ServerSocketChannel serverSocketChannel;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    //单线程的心跳检测器，防止并发
    private static ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);
    private static ServerFrame serverFrame;
    private static int port = 8888;
    //维护用户和其所在通道的连接
    private static final Map<String, SocketChannel> onlineUsers = new ConcurrentHashMap<>();
    //检测最后的心跳时间
    private static final Map<SocketChannel, Long> lastHeartbeat = new ConcurrentHashMap<>();

    public  static void main(String[] args) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverFrame = new ServerFrame();

        serverFrame.appendLog("服务器启动，监听端口: " + port);

        new Thread(() -> {
            try {
                listen();
            } catch (IOException e) {
                e.printStackTrace();
                serverFrame.appendLog("监听异常: " + e.getMessage());
            }
        }).start();
        heartbeatExecutor.scheduleAtFixedRate(() -> checkHeartbeat(), 10, 10, TimeUnit.SECONDS);
        serverFrame.setKickUserListener(username -> {
            SocketChannel targetChannel = onlineUsers.get(username);
            if (targetChannel != null) {
                try {
                    String kickMsg = "您已被管理员强制下线";
                    ByteBuffer kickBuffer = ByteBuffer.wrap(kickMsg.getBytes());
                    targetChannel.write(kickBuffer);
                    targetChannel.close();
                    onlineUsers.remove(username);
                    serverFrame.removeUser(username);
                    UserService.offline(username);
                    serverFrame.appendLog("管理员强制下线用户: " + username);
                } catch (IOException e) {
                    serverFrame.appendLog("强制下线失败: " + e.getMessage());
                }
            } else {
                serverFrame.appendLog("用户 " + username + " 不在线");
            }
        });

    }

    private static void listen() throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    //这里每次注册的通道都是不同的
                    //这个 socketChannel 是 新建的独立连接实例，专属于这个客户端
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    serverFrame.appendLog("客户端连接: " + socketChannel.getRemoteAddress());
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    //每次监听到可读，都会启动一个新线程，读取该 专属 socketChannel 的数据
                    executor.submit(() -> readClient(socketChannel, key));
                }
            }
        }
    }

    private static void checkHeartbeat() {
        long now = System.currentTimeMillis();
        //检查每一个心跳还跳着没
        Iterator<Map.Entry<SocketChannel, Long>> iterator = lastHeartbeat.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SocketChannel, Long> entry = iterator.next();
            SocketChannel socketChannel = entry.getKey();
            long lastHeartbeat = entry.getValue();
            if (now - lastHeartbeat > 300000) {
                String name = getUserchannel(socketChannel);
                if(name != null) {
                    try{
                        String re = "您的心跳长时间暂停，还请下线休息";
                        ByteBuffer kickBuffer = ByteBuffer.wrap(re.getBytes());
                        socketChannel.write(kickBuffer);
/*                        socketChannel.close();
                        onlineUsers.remove(name);
                        serverFrame.removeUser(name);*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            iterator.remove();
        }
    }

    private static String getUserchannel(SocketChannel socketChannel) {
        //找到这个心脏停跳的主人
        for(Map.Entry<String, SocketChannel> entry : onlineUsers.entrySet()) {
            if(entry.getValue().equals(socketChannel)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static void readClient(SocketChannel socketChannel, SelectionKey key) {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        try {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                String disconnectedUser = null;
                for (Map.Entry<String, SocketChannel> entry : onlineUsers.entrySet()) {
                    if (entry.getValue().equals(socketChannel)) {
                        disconnectedUser = entry.getKey();
                        break;
                    }
                }
                //去除这些非正常链接的客户端
                if (disconnectedUser != null) {
                    onlineUsers.remove(disconnectedUser);
                    serverFrame.removeUser(disconnectedUser);
                    serverFrame.appendLog(disconnectedUser + " 非正常断开连接");
                }
                key.cancel();
                socketChannel.close();
                return;
            }

            if (bytesRead > 0) {
                lastHeartbeat.put(socketChannel, System.currentTimeMillis());
                buffer.flip();
                String msg = new String(buffer.array(), 0, buffer.limit());
                String[] parts = msg.split(",");
                String action = parts[0];
                String action1 = parts[1];
                String action2 = parts[2];
                String result;

                if ("LOGIN".equalsIgnoreCase(action)) {
                    int a = UserService.login(action1, action2);
                    if (a==1) {
                        onlineUsers.put(action1, socketChannel); // 记录在线
                        serverFrame.addUser(action1); // 界面更新
                        result = "登录成功";
                        serverFrame.appendLog(action1 + "登录成功");
                    } else if(a == -2){
                        result = "该用户不存在";
                    } else if(a == -1){
                        result = "您已登录过";
                    } else {
                        result = "密码错误";
                    }
                } else if ("REGISTER".equalsIgnoreCase(action)) {
                    if (UserService.register(action1, action2)) {
                        result = "注册成功,请重新登录";
                        serverFrame.appendLog(action1 + "注册成功");
                    } else {
                        result = "注册失败";
                    }
                } else if ("FILE".equalsIgnoreCase(action)) {
                    key.cancel();
                    executor.submit(()->handleFileTransfer(socketChannel,parts));
                    return;
                } else if ("DOWNLOAD_FILE".equalsIgnoreCase(action)) {
                    String savePath = "D:\\code\\ChatRoom\\client\\"+action1;
                    File file = new File(savePath);
                    sendfile(socketChannel,file);
                    result = "*";
                } else if ("ADD_FRIEND".equalsIgnoreCase(action)) {
                    FriendService.saveQuest(action1,action2);
                    result = "已发送好友申请";
                } else if ("ACCEPTFRIEND".equalsIgnoreCase(action)) {
                    if(FriendService.addFriend(action1,action2)){
                        FriendService.deleteQuest(action1,action2);
                        result = "成功添加好友";
                        SocketChannel socketChannel1 = onlineUsers.get(action1);
                        ByteBuffer buffer1 = ByteBuffer.wrap(result.getBytes());
                        if(socketChannel1!=null){
                            socketChannel1.write(buffer1);
                        }
                        serverFrame.appendLog(action1 + "和" + action2 + "成为好友");
                    } else {
                        result = "添加失败";
                    }
                } else if ("REJECTFRIEND".equalsIgnoreCase(action)) {
                    FriendService.deleteQuest(action1,action2);
                    result = "拒绝添加好友";
                    serverFrame.appendLog(action2+"拒绝添加"+action1+"为好友");
                } else if ("DELETEFRIEND".equalsIgnoreCase(action)) {
                    if(FriendService.deleteFriend(action1,action2)){
                        MessageService.deleteUserChat(action1,action2);
                        result = "成功删除好友";
                        SocketChannel socketChannel1 = onlineUsers.get(action2);
                        ByteBuffer buffer1 = ByteBuffer.wrap(result.getBytes());
                        if(socketChannel1!=null){
                            socketChannel1.write(buffer1);
                        }
                    } else {
                        result = "删除失败";
                    }
                } else if ("GROUP_CHAT".equalsIgnoreCase(action)) {
                    String action3 = parts[3];
                    if(MessageService.addGroupMessage(action1,action2,action3)){
                        result = "MESSAGE,"+action1+","+action3+","+action2;
                        List<String> list = MemberService.findMembers(action2);
                        for(String s : list){
                            SocketChannel socketChannel1 = onlineUsers.get(s);
                            ByteBuffer buffer1 = ByteBuffer.wrap(result.getBytes());
                            if(socketChannel1!=null&& !s.equals(action1)){
                                socketChannel1.write(buffer1);
                            }
                        }
                    } else result = "发送失败";
                } else if ("USER_CHAT".equalsIgnoreCase(action)) {
                    String action3 = parts[3];
                    if(MessageService.addUserMessage(action1,action2,action3)){
                        String re = "MESSAGE,"+action1 + "," + action3 +",_";
                        SocketChannel socketChannel2 = onlineUsers.get(action2);
                        ByteBuffer buffer2 = ByteBuffer.wrap(re.getBytes());
                        if(socketChannel2!=null){
                            socketChannel2.write(buffer2);
                        }
                        result = "*";
                    } else result = "发送失败";
                } else if ("CREATE_GROUP".equalsIgnoreCase(action)) {
                    if(GroupService.addGroup(action2,action1)){
                        MemberService.addMember(action2,action1);
                        result = "成功创建群聊";
                    } else result = "创建群聊失败";
                } else if ("JOIN_GROUP".equalsIgnoreCase(action)) {
                    if(MemberService.addMember(action2,action1)){
                        result = "成功加入群聊";
                    } else result = "添加群聊失败";
                } else if ("DISSOLVE_GROUP".equalsIgnoreCase(action)) {
                    if(GroupService.deleteGroup(action1)){
                        result = "'"+action1+"'"+"群聊已解散";
                        List<String> l1 = MemberService.removeMember(action1);
                        for(String s : l1){
                            SocketChannel sc = onlineUsers.get(s);
                            if(sc!=null){
                                ByteBuffer buffer3 = ByteBuffer.wrap(result.getBytes());
                                sc.write(buffer3);
                            }
                        }
                    } else result = "群聊解散失败";
                } else if ("DELETE_GROUP".equalsIgnoreCase(action)) {
                    if(MemberService.removeMember(action2,action1)){
                        result = "成功退出群聊";
                    } else result = "退出群聊失败";
                } else if ("MUTE_USER".equalsIgnoreCase(action)) {
                    if(MemberService.banMember(action2,action1)){
                        result = "已禁言"+action2;
                        String re = action1+"的群主将您禁言";
                        SocketChannel socketChannel3 = onlineUsers.get(action2);
                        ByteBuffer buffer3 = ByteBuffer.wrap(re.getBytes());
                        if(socketChannel3!=null){
                            socketChannel3.write(buffer3);
                        }
                    } else result = "禁言失败";
                } else if ("UNMUTE_USER".equalsIgnoreCase(action)) {
                    if(MemberService.unbanMember(action2,action1)){
                        result = "取消禁言"+action2;
                        String re = action1+"的群主取消您的禁言";
                        SocketChannel socketChannel3 = onlineUsers.get(action2);
                        ByteBuffer buffer3 = ByteBuffer.wrap(re.getBytes());
                        if(socketChannel3!=null){
                            socketChannel3.write(buffer3);
                        }
                    } else result = "取消禁言失败";
                } else if ("KICK_USER".equalsIgnoreCase(action)) {
                    if(MemberService.removeMember(action2,action1)){
                        result = "将"+action2+"移出群聊";
                        String re = action1+"的群主将您移除群聊";
                        SocketChannel socketChannel3 = onlineUsers.get(action2);
                        ByteBuffer buffer3 = ByteBuffer.wrap(re.getBytes());
                        if(socketChannel3!=null){
                            socketChannel3.write(buffer3);
                        }
                    } else result = "踢人失败";
                } else if ("QUIT".equalsIgnoreCase(action)) {
                    onlineUsers.remove(action1);
                    serverFrame.removeUser(action1);
                    UserService.offline(action1);
                    result = "已退出登录";
                } else {
                    result = "未知操作";
                }
                ByteBuffer response = ByteBuffer.wrap(result.getBytes());
                if(!result.equals("*")){
                    socketChannel.write(response);
                }
                serverFrame.appendLog("收到消息: " + msg);
                if(result.equals("已退出登录")){
                    socketChannel.close();
                }
            }

        } catch (IOException e) {
            serverFrame.appendLog("读取失败: " + e.getMessage());
            try {
                key.cancel();
                socketChannel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void sendfile(SocketChannel socketChannel, File file) {
        //发送文件本体
        Path path = file.toPath();
        try(FileChannel fileChannel = FileChannel.open(path,StandardOpenOption.READ)){
            ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
            while(fileChannel.read(byteBuffer)!=-1){
                byteBuffer.flip();
                //保证缓冲区的数据全部写入输出流中
                while(byteBuffer.hasRemaining()){
                    socketChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleFileTransfer(SocketChannel socketChannel, String[] parts) {
        try {
            long fileSize = Long.parseLong(parts[5]);
            String fileName = parts[4];
            String savePath = "D:\\code\\ChatRoom\\client\\"+fileName;
            Path path =Path.of(savePath);
            String filetype = parts[6];
            String sender = parts[2];
            String receiver = parts[3];
            String re = "READY_FOR_FILE";
            ByteBuffer buffer = ByteBuffer.wrap(re.getBytes());
            socketChannel.write(buffer);
            int bytesRead;
            socketChannel.configureBlocking(true);
            try (FileChannel fc = (FileChannel) Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                ByteBuffer b = ByteBuffer.allocate(8192);
                b.clear();
                while (fileSize > 0) {
                    b.clear();
                    //保证读取文件不产生空洞
                    int bytesToRead = (int) Math.min(fileSize, b.capacity());
                    b.limit(bytesToRead);
                    bytesRead = socketChannel.read(b);
                    if (bytesRead == -1) break;
                    b.flip();
                    fc.write(b);
                    //保证文件正确的读取次数
                    fileSize -= bytesRead;
                }
            }
            socketChannel.configureBlocking(false);
            User user = new UserDAO().findByUsername(sender);
            if(parts[1].equals("Group")){
                Chatgroup chatgroup = new ChatgroupDAO().findByName(receiver);
                new MessageDAO().insertGroup(user.getId(), chatgroup.getId(), savePath,filetype);
                String r = "MESSAGE,"+sender+","+savePath+","+receiver;
                List<String> list = MemberService.findMembers(receiver);
                for(String s : list){
                    SocketChannel socketChannel1 = onlineUsers.get(s);
                    ByteBuffer buffer1 = ByteBuffer.wrap(r.getBytes());
                    if(socketChannel1!=null&& s.equals(sender)){
                        socketChannel1.write(buffer1);
                    }
                }
            } else {
                User u = new UserDAO().findByUsername(receiver);
                new MessageDAO().insertUser(user.getId(), u.getId(), savePath,filetype);
                String r = "MESSAGE,"+sender+","+savePath+",_";
                SocketChannel socketChannel2 = onlineUsers.get(receiver);
                ByteBuffer buffer2 = ByteBuffer.wrap(r.getBytes());
                if(socketChannel2!=null){
                    socketChannel2.write(buffer2);
                }
            }
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
