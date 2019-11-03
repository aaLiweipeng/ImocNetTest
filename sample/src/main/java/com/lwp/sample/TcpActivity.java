package com.lwp.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lwp.sample.biz.TcpClientBiz;
import com.lwp.sample.biz.UdpClientBiz;

public class TcpActivity extends AppCompatActivity {

    private EditText mEtMsg;
    private Button mBtnSend;
    private TextView mTvContent;

    private TcpClientBiz mTcpClientBiz = new TcpClientBiz();


    public Context getTcpActivityContext() {
        return getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mTcpClientBiz.setOnMsgComingListener(new TcpClientBiz.onMsgComingListener() {
            @Override
            public void onMsgComing(String msg) {
                appendMsgToContent("Server:" + msg);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void popToast() {
                Toast.makeText(TcpActivity.this, "初始化完成！！！！可以开始发送信息了！！！", Toast.LENGTH_SHORT).show();
            }
        });
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

                //发送后清除编辑框文本
                mEtMsg.setText("");

                //msg 负责发送数据，onMsgReturnedListener() 则负责处理对应的返回的信息
                mTcpClientBiz.sendMsg(msg);
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
        mTcpClientBiz.onDestroy();
    }
}
