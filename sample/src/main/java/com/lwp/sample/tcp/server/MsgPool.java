package com.lwp.sample.tcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <pre>
 *     author : 李蔚蓬（简书_凌川江雪）
 *     time   : 2019/10/30 17:45
 *     desc   :每一个Client发送过来的消息，
 *             都会被加入到队列当中去，
 *             队列中默认有一个子线程，
 *             专门从队列中，死循环，不断去取数据，
 *             取到数据就做相关处理，比如分发给其他的socket；
 * </pre>
 */
public class MsgPool {

    private static MsgPool mInstance = new MsgPool();

//    /*
//        这里默认消息是String类型，
//        或者可以自行封装一个Model 类，存储更详细的信息
//
//        block n.块； 街区；障碍物，阻碍
//        顾名思义，这是一个阻塞的队列，当有消息过来时，就把消息发送给这个队列，
//        这边会起一个线程专门从队列里面去取消息，
//        如果队列中没有消息，就会阻塞在原地
//     */

    private LinkedBlockingQueue<String> mQueue = new LinkedBlockingQueue<>();

    public static MsgPool getInstance() {
        return mInstance;
    }

    private MsgPool() {
    }

    //这是一个阻塞的队列，
    // 当有消息过来时，即客户端接收到消息时，
    // 就把消息发送（添加）到这个队列中
    //现在所有的客户端都可以发送消息到这个队列中

    public void sendMsg(String msg) {
        try {
            mQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //要一早就调用本方法，
    // 启动这个读取消息的线程，在后台不断运行
    public void start() {
        //开启一个线程去读队列的数据
        new Thread() {
            @Override
            public void run() {
                //无限循环读取信息
                while (true) {
                    try {
                        //取出并移除队头；没有消息时，take()是阻塞的
                        String msg = mQueue.take();
                        notifyMsgComing(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    //被观察者方法，遍历所有已注册的观察者，一次性通知更新
    private void notifyMsgComing(String msg) {
        for (MsgComingListener listener : mListeners) {
            listener.onMsgComing(msg);
        }
    }

    //观察者接口
    public interface MsgComingListener {
        void onMsgComing(String msg);//更新方法
    }

    //被观察者，存放观察者
    private List<MsgComingListener> mListeners = new ArrayList<>();

    //被观察者方法，添加观察者到列表
    public void addMsgComingListener(MsgComingListener listener) {
        mListeners.add(listener);
    }
}
