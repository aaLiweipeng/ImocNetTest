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
 *     time   : 2019/10/28 15:20
 *     desc   :
 * </pre>
 */
public class UdpClient {

    /**
     * 指定Server的 ip 和 port
     */
    private String mServerIp = "172.18.1.59";
    private int mServerPort = 7778;
    private InetAddress mServerAddress;
    /**
     * 通信用的Socket
     */
    private DatagramSocket mSocket;
    private Scanner mScanner;

    //构造方法中初始化
    public UdpClient() {
        try {

            /*
                直接实例化一个默认的Socket对象即可，
                因为我们不需要像服务端那样把别的Client接入过来，
                不必特别明确指定 自己的ip和port（服务程序），！！！！！！！！！！
                因为这里是Client，是数据请求获取方，不是数据提供方，！！！！

                所以只需要一个默认的Socket对象
                来进行send 和 receive 即可
             */
            mSocket = new DatagramSocket();

            mServerAddress = InetAddress.getByName(mServerIp);

            mScanner = new Scanner(System.in);
            mScanner.useDelimiter("\n");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void start() {

        while (true) {

            try {
                /*
                    完成向服务端发送数据
                 */
                String clientMsg = mScanner.next();
                byte[] clientMsgBytes = clientMsg.getBytes();
            /*
               封装数据包，传入数据数组以及服务端地址、端口号
             */
                DatagramPacket clientPacket = new DatagramPacket(clientMsgBytes,
                        clientMsgBytes.length, mServerAddress, mServerPort);
                mSocket.send(clientPacket);


                /*
                    接收服务端数据
                 */
                byte[] buf = new byte[1024];
                DatagramPacket serverMsgPacket = new DatagramPacket(buf, buf.length);
                mSocket.receive(serverMsgPacket);

                //拿到服务端地址、端口号、发送过来的数据
                InetAddress address = serverMsgPacket.getAddress();
                int port = serverMsgPacket.getPort();
                byte[] data = serverMsgPacket.getData();
                String serverMsg = new String(data, 0, data.length);//把接收到的字节数据转换成String

                //打印服务端信息和发送过来的数据
                System.out.println("(Server's) msg = " + serverMsg);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        new UdpClient().start();
    }

}
