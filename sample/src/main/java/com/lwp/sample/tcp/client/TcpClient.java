package com.lwp.sample.tcp.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * <pre>
 *     author : 李蔚蓬（简书_凌川江雪）
 *     time   : 2019/10/31 15:36
 *     desc   :
 * </pre>
 */
public class TcpClient {

    private Scanner mScanner;

    public TcpClient() {
        mScanner = new Scanner(System.in);
        mScanner.useDelimiter("\n");
    }

    /**
     * 配置socket
     * 准备IO 流，
     * 主线程写，子线程读
     *
     */
    public void start() {
        try {
            Socket socket = new Socket("172.18.1.59", 9090);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            final BufferedReader br = new BufferedReader(new InputStreamReader(is));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            /*
                实现：
                通过 reader，
                在任何时候 能够读到 Server端 发来的数据
                通过 writer，
                在任何时候 能够向 Server端 去写数据
             */
            //在等待客户端 发送消息过来的话，这里是需要阻塞的，
            // 阻塞的时候又没有办法向客户端发送数据，所以读写独立的话，肯定是要起线程的

            //起一个线程，专门用于
            // 读Server 端 发来的数据，数据一过来就读然后输出,
            // 输出服务端发送的数据
            new Thread() {
                @Override
                public void run() {

                    try {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                    }
                }
            }.start();

            //给Server端 发送数据
            while (true) {
                //next() 是阻塞的，不断地读控制面板,有数据就会通过bufferWriter，
                // 即outputStream 写给Server
                String msg = mScanner.next();
                bw.write(msg);
                bw.newLine();
                bw.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TcpClient().start();
    }
}
