package com.lwp.sample.biz;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * <pre>
 *     author : 李蔚蓬（简书_凌川江雪）
 *     time   : 2019/10/31 15:36
 *     desc   : 定义接口，完成客户端的收发逻辑
 * </pre>
 */
public class TcpClientBiz {

    private Socket mSocket;
    private InputStream mIs;
    private OutputStream mOs;

    /**
     * Looper.getMainLooper()，将主线程中的 Looper 扔进去了，
     * 也就是说 handleMessage 会运行在主线程中，
     * ！！！！！！！！！！
     * 这样可以在主线程中更新 UI 而不用把 Handler 定义在主线程中。
     * ！！！！！！！！！！
     */
    private Handler mUiHandler = new Handler(Looper.getMainLooper());

//    /*
//        注意，因为UdpClient 的send 和 receive 是绑定的，
//        所以其 返回信息的处理接口 是作为 发送信息方法 的参数的，由此产生绑定逻辑
//
//        但是这里 TcpClient 就不是send 和 receive 一一绑定了，
//        其没有数量的对应关系，只是一个持续的 任意数据包数量的 全双工的连接，
//        无需Udp 的绑定逻辑， Listener 由此不使用跟send 方法绑定的逻辑，
//        使用单独set 的逻辑表达方式
//     */

    public interface onMsgComingListener {
        void onMsgComing(String msg);
        void onError(Exception ex);
        void popToast();
    }

    private onMsgComingListener mListener;

    public void setOnMsgComingListener(onMsgComingListener listener) {
        mListener = listener;
    }

    //------------------------------------------------------------------------

    public TcpClientBiz() {

//        //socket 的new 到 IO 流的获取 这几行代码是已经做了网络操作的，
//        // 所以必须开一个子线程去进行，！！！！
//        // 毕竟 TcpClientBiz() 在调用的时候肯定是在UI 线程进行的
//
//        /*
//            另外需要注意一点！！！
//            下面的socket 和 IO 流初始化是在子线程中进行的，
//            所以我们不知道什么时候会完成初始化，
//            因此在使用的时候是需要进行一个UI 交互提醒的，
//            比如loading 动画，启动页面时使用loading动画，初始化完成之后再取消loading 动画，
//
//         */
        new Thread() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket("172.18.1.59", 9090);//连接到 Server端
                    mIs = mSocket.getInputStream();
                    mOs = mSocket.getOutputStream();

                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.popToast();
                        }
                    });

                    //读到消息则 借用回调 回到MainActivity 进行UI 更新
                    readServerMsg();

                } catch (final IOException e) {

                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (mListener != null) {
                                mListener.onError(e);
                            }
                        }
                    });
                }
            }
        }.start();


    }

    /**
     * 一旦本类被实例化，马上启动
     * 不断阻塞等待Server端 信息
     * readLine() 没有消息时阻塞，
     * 一有消息，马上发给接口处理逻辑
     *
     * @throws IOException
     */
    private void readServerMsg() throws IOException {

        final BufferedReader br = new BufferedReader(new InputStreamReader(mIs));

        String line = null;

        while ((line = br.readLine()) != null) {

            final String finalLine = line;

            /*
                ！！！！！！！！！！！！！！！！
                基于主线程MainLooper 以及 回调机制
                在 业务类内部 调用 外部实现的处理逻辑方法
                ！！！！！！！！！！
             */
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {

                    //读到消息则 借用回调 回到MainActivity 进行UI 更新
                    if (mListener != null) {
                        mListener.onMsgComing(finalLine);
                    }
                }
            });

        }
    }

    /**
     * 把参数msg 写入BufferWriter（O流），发送给Server端,
     * 一般这个msg 消息 是EditText 中的内容，
     *
     * 调用时机：一般是EditText 右边的按钮被点击的时候
     *
     * 调用时，封装输出流，
     * 把参数msg 写入BufferWriter（O流），发送给Server端,
     *
     * 在要发送消息给Server 的时候调用
     * 发送的消息会在Server 端的 ClientTask 类中
     * 的run() 中的while ((line = br.readLine()) != null) 处被读取到，
     * 并通过 MsgPool.getInstance().sendMsg() 被添加到消息队列中
     *
     * @param msg  要发送的信息
     */
    public void sendMsg(final String msg) {

        //开一个线程去做输出，完成任务之后线程就自动回收
        new Thread(){
            @Override
            public void run() {
                try {
                    //一有消息过来，就封装输出流，写入并 发送信息到 Server端
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(mOs));
                    bw.write(msg);
                    bw.newLine();
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void onDestroy() {
        //！！！！
        // 独立地try...catch...的原因：
        // ！！！！
        // 如果把三个close 都放在同一个try 块里面
        // 那假如第一个close 出现了异常，
        // 后面两个close 即使没异常，
        // 也处理不了了，这显然是不符合条件的
        // ！！！！！

        try {
            if (mIs != null) {
                mIs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (mOs != null) {
                mOs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
