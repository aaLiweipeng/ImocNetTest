package com.lwp.sample.biz;

import android.os.Handler;
import android.os.Looper;

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
public class UdpClientBiz {

    /**
     * 指定Server的 ip 和 port
     */
    private String mServerIp = "172.18.1.59";
    private int mServerPort = 7778;
    private InetAddress mServerAddress;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    /**
     * 通信用的Socket
     */
    private DatagramSocket mSocket;

    //构造方法中初始化
    public UdpClientBiz() {
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

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    /*
        需求：客户端接收Server端返回的数据，并展示在控件上
        实现：send 方法绑定一个接口
        ps：这里的回调机制实现其实还有一种写法，
        就是另外单独再起一个setListener方法来绑定Listener ，
        但是这样做不太符合这里具体的场景——每个 服务端 return 回来的数据
        都是跟每个 客户端 send 出去的数据相关联对应的；
        单独使用setListener 的方式，看不到这个关联的逻辑，
        所以这里直接把Listener 作为sendMsg 的必要形参，形成关联逻辑
        以及绑定关系——必须先 sendMsg 之后才能 returnMsg（receiveMsg）
     */
    public interface onMsgReturnedListener{
        void onMsgReturned(String msg);
//        /*
//            Handle Exception
//            如果是异步的方法调用：可以把Exception 通过 Listener 给回调出去
//            如果是同步的方法调用：尽可能不要在方法中进行try catch，
//            最好是将其throw 出去，
//            或者catch 之后 封装下错误类型再将其throw 出去，
//            即一定要让调用者能知道这个异常；
//
//            这里是异步调用
//         */

        void onError(Exception ex);
    }


    public void sendMsg(final String msg, final onMsgReturnedListener listener) {
        new Thread() {
            @Override
            public void run() {
                try {
                    //信息转型
                    byte[] clientMsgBytes = msg.getBytes();
            /*
               封装数据包，传入数据数组以及服务端地址、端口号
               发送数据
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
                    final String serverMsg = new String(data, 0, data.length);//把接收到的字节数据转换成String

                    /*
                        以上是对 Server端 信息的发送和接收，写在sendMsg 方法体中，名副其实

                        以下是对接收数据的处理，通过回调处理
                     */

                    //这里是子线程，
                    // 但是 Handler 已同 MainLooper 进行绑定，
                    // 则利用这个handle 去更新UI，等同于切回主线程更新UI
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //数据借助回调外传
                            //“切回了”主线程，在调用的时候，接收数据之后才能更新UI
                            listener.onMsgReturned(serverMsg);
                        }
                    });

                } catch (final Exception e) {

                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //异常回调
                            listener.onError(e);
                        }
                    });

                }
            }
        }.start();

    }

    public void onDestroy() {
        if(mSocket != null){
            mSocket.close();
        }

    }
}
