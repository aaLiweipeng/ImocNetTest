package com.lwp.sample.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * <pre>
 *     author : 李蔚蓬（简书_凌川江雪）
 *     time   : 2019/10/27 17:08
 *     desc   :
 * </pre>
 */
public class UdpServer {

    private InetAddress mInetAddress;
    private int mPort = 7778;//尽可能用5000以后的

    private DatagramSocket mSocket;

    private Scanner mScanner;

    //构造方法中初始化
    public UdpServer() {
        try {
            mInetAddress = InetAddress.getLocalHost();

            //传入，设置好本服务器ip 和 本服务程序指定的端口，虚拟“链接”的服务器一端
            mSocket = new DatagramSocket(mPort, mInetAddress);

            //用于控制面板的输入
            mScanner = new Scanner(System.in);
            mScanner.useDelimiter("\n");//指定控制面板的输入以换行来结束

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() {

        //让Server端持续运行
        while (true) {

            try {
                //类似于缓存区的一个字节数组
                //UDP每次通信的数据大小受限制
                //限制就来自于服务端传给DatagramPacket的字节数组
                //因为UDP是通过DatagramPacket封装数据的，
                // 而DatagramPacket的创建必须传入一个字节数组，这个数组便是通信数据包的大小限制
                //
                //这里指定的是1024，也就是客户端发送过来的数据包，
                // 每次不能超过1024个字节，1byte = 8bit
                byte[] buf = new byte[1024];

                //接收客户端数据
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);

                //如果没有数据包到来的话，程序会一直阻塞在receive()这里，receive()会阻塞，
                // 如果有一个客户端发送一个数据包到这个程序中，
                // 程序就会去执行receive()方法，将接收到的数据传输到receivedPacket中进而传输给receive()
                mSocket.receive(receivedPacket);
                //所以如果程序能往下走，就证明接收到数据了


                //拿到客户端地址、端口号、发送过来的数据
                InetAddress address = receivedPacket.getAddress();
                int port = receivedPacket.getPort();
                byte[] data = receivedPacket.getData();
                String clientMsg = new String(data, 0, data.length);//把接收到的字节数据转换成String

                //打印客户端信息和发送过来的数据
                System.out.println("address = " + "***" +
                        ", port = " + port + ",(Client's) msg = " + clientMsg);

                /*
                  读取Terminal的输入
                  next()也是阻塞的，监听Terminal输入（消息+回车）

                  给客户端返回数据,
                  返回的数据我们希望可以在控制面板Terminal上写，
                  写完按Enter键完成
                 */
                String returnedMsg = mScanner.next();
                byte[] returnedMsgBytes = returnedMsg.getBytes();//将String转换成byte数组

                //getSocketAddress中包含getAddress(), getPort()，即包含地址跟数组
                //下面把需要返回给客户端的数据封装成一个DatagramPacket
                DatagramPacket sendPacket = new DatagramPacket(returnedMsgBytes,
                        returnedMsgBytes.length, receivedPacket.getSocketAddress());
                mSocket.send(sendPacket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new UdpServer().start();
    }
}