package com.softtanck.framework.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.softtanck.framework.ChatAdapter;
import com.softtanck.framework.R;
import com.softtanck.framework.utils.LogUtils;

/**
 * @author : Tanck
 * @Description : TODO
 * @date Jan 16, 2015 5:20:57 PM
 */
public class ChatActivity extends BaseActivity {

    private ListView listView;

    private ChatAdapter adapter;

    @Override
    protected int getViewId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onActivityCreate() {
        listView = (ListView) findViewById(R.id.list);
        adapter = new ChatAdapter(this, "Tanck", 1);
        listView.setAdapter(adapter);

        initChat();
    }

    private void initChat() {
        // 注册接收消息广播
        NewMessageBroadcastReceiver receiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        // 设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
        intentFilter.setPriority(5);
        registerReceiver(receiver, intentFilter);
    }


    /**
     * 消息广播接收者
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String username = intent.getStringExtra("from");
            String msgid = intent.getStringExtra("msgid");
            // 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
            EMMessage message = EMChatManager.getInstance().getMessage(msgid);
            LogUtils.d("收到了消息:---->消息类型:" + message.getChatType() + "----消息内容:" + (TextMessageBody) message.getBody());
            adapter.notifyDataSetChanged();
            abortBroadcast();
        }
    }

    public void test(View view) {

        LogUtils.d("test");
        EMChatManager.getInstance().login("cqm", "422013", new EMCallBack() {
            @Override
            public void onSuccess() {
                LogUtils.d("登陆成功");
                TextMessageBody txtBody = new TextMessageBody("hello world");
                EMConversation conversation = EMChatManager.getInstance().getConversation("cqm");
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                // 设置消息body
                message.addBody(txtBody);
                // 设置要发给谁,用户username或者群聊groupid
                message.setReceipt("cqm");
                // 把messgage加到conversation中
                conversation.addMessage(message);
                LogUtils.d("name:"+conversation.getUserName());
                LogUtils.d("发送成功");
            }

            @Override
            public void onError(int i, String s) {
                LogUtils.d("登陆失败");
            }

            @Override
            public void onProgress(int i, String s) {
                LogUtils.d("登陆中...");
            }
        });
    }
}
