package com.lwp.sample;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lwp.sample.biz.UdpClientBiz;

public class UdpActivity extends AppCompatActivity {

    private EditText mEtMsg;
    private Button mBtnSend;
    private TextView mTvContent;

    private UdpClientBiz mUdpClientBiz = new UdpClientBiz();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        mEtMsg = findViewById(R.id.id_et_msg);
        mBtnSend = findViewById(R.id.id_btn_send);
        mTvContent = findViewById(R.id.id_tv_content);

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mEtMsg.getText().toString();
                if (TextUtils.isEmpty(msg)) {
                    return;
                }

                appendMsgToContent("client:" + msg);

                //发送后清除编辑框文本
                mEtMsg.setText("");

                //msg 负责发送数据，onMsgReturnedListener() 则负责处理对应的返回的信息
                mUdpClientBiz.sendMsg(msg, new UdpClientBiz.onMsgReturnedListener() {
                    @Override
                    public void onMsgReturned(final String msg) {
                        //更新UI
                        appendMsgToContent("server:" + msg);

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                appendMsgToContent("server:" + msg);
//                            }
//                        });


                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
    }

    private void appendMsgToContent(String msg) {
        mTvContent.append(msg + "\n");
    }

    /*
        回收资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUdpClientBiz.onDestroy();
    }
}
