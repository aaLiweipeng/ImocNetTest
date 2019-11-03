package com.lwp.sample.tcp.server;

import com.lwp.sample.tcp.client.TcpClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <pre>
 *     author : 李蔚蓬（简书_凌川江雪）
 *     time   : 2019/10/30 16:57
 *     desc   :指定服务端端口号（ip 默认为本机ip）
 *             启动循环读取消息队列的子线程，
 *             死循环，不断等待客户端请求连接，
 *             一旦连接上，直接新建一个子线程（丢给ClientTask）去处理这个socket，
 *
 *             于是主线程又可以回到accept() 阻塞，等待下一个连接请求；
 *
 *             同时，将连接上的socket 对应的线程类，注册为消息队列的观察者，
 *             让线程类担任观察者，负责接收被观察者的通知信息并做socket 通信
 * </pre>
 */
public class TcpServer {

    public void start() {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9090);
            MsgPool.getInstance().start();//启动读消息的子线程

            while (true) {
//            /*
//            阻塞的方法！！！  等待（客户端的） TCP 连接请求
//            客户端有 TCP 请求并连接上了 ServerSocket，.
//            那 accept() 就会返回一个 同一连接上 对应 客户一端socket 的 服务一端socket
//             */
                Socket socket = serverSocket.accept();

                //客户端连接之后，打印相关信息
//            System.out.println("ip: " + socket.getInetAddress().getHostAddress() +
//                    ", port = " + socket.getPort() + "is online...");
                System.out.println("ip = " + "***.***.***.***" +
                        ", port = " + socket.getPort() + " is online...");

//            /*
//                连接上了之后不能直接拿IO流去读写，
//                因为getInputStream() 和 getOutputStream() 都是阻塞的！！！！
//                如果直接拿IO 流，不做其他处理，
//                那么Server端的处理流程是这样的：
//                accept()-- getInputStream()处理第一个客户端 -- 处理完毕,accept()-- getInputStream()处理第二个客户端....
//                所以必须开启子线程去读写客户端，才能做成聊天室
//
//                针对每一个连接上来的客户端去单独起一个线程，跟客户端进行通信
//
//                过程：客户端连上之后，打印其信息，
//                然后直接新建一个子线程（丢给ClientTask）去处理这个socket，
//                于是主线程又可以回到accept() 阻塞，等待下一个连接请求
//             */
                ClientTask clientTask = new ClientTask(socket);
                MsgPool.getInstance().addMsgComingListener(clientTask);
                clientTask.start();


            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TcpServer().start();
    }
}
