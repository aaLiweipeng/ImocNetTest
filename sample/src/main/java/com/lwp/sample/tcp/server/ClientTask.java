package com.lwp.sample.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * <pre>
 *     author : 李蔚蓬（简书_凌川江雪）
 *     time   : 2019/10/30 17:23
 *     desc   :针对每一个连接上来的客户端去单独起一个线程，跟客户端进行通信,
 *             这里便是线程类；
 *             run()中死循环不断读取客户端发来的信息，发送给客户端（服务端）要发送的信息；
 *             实现MsgPool.MsgComingListener， 成为消息队列的观察者！！！
 * </pre>
 */
public class ClientTask extends Thread implements MsgPool.MsgComingListener {

    private Socket mSocket;
    private InputStream mIs;
    private OutputStream mOs;

    public ClientTask(Socket socket) {

        try {
            mSocket = socket;
            mIs = socket.getInputStream();
            mOs = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(mIs));

        String line = null;
        /*
            读取并输出客户端信息。
            如果没有客户端发送信息，readLine() 便会阻塞在原地
         */
        try {
            while ((line = br.readLine()) != null) {
                //在命令行窗口打印
                System.out.println("read " + mSocket.getPort() + " = " + line);

                //把信息发送加入到消息队列，！！！！！！！！！！！！！！！！
                // 借助消息队列的被观察者通知方法，
                // 将消息转发至其他Socket！！！！！！！！！！！！！！！！！！
                // （所有socket都在创建ClientTask的时候，
                // 备注成为MsgPool 的观察者）
                MsgPool.getInstance().sendMsg(mSocket.getPort() + " : " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //作为消息队列的观察者对应的更新方法,
    // 消息队列中最新的消息会推送通知到这里的msg参数，
    // 这里拿到最新的推送消息后，写进输出流，
    // 推到TCP 连接的客户一端的 socket
    @Override
    public void onMsgComing(String msg) {
        try {
            mOs.write(msg.getBytes());
            mOs.write("\n".getBytes());
            mOs.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
