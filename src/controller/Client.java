package controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class Client {
    private static SocketChannel socketChannel;
    private static final int port = 8888;
    public static void main(String[] args) throws Exception {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", port));
        socketChannel.configureBlocking(false);

        new LoginFrame(socketChannel);
    }

}
