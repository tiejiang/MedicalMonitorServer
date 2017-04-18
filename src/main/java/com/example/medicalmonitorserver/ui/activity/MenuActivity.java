package com.example.medicalmonitorserver.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.medicalmonitorserver.R;
import com.example.medicalmonitorserver.common.CCPAppManager;
import com.example.medicalmonitorserver.core.ClientUser;
import com.example.medicalmonitorserver.ui.helper.IMChattingHelper;
import com.example.medicalmonitorserver.ui.helper.SDKCoreHelper;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;

import java.util.List;

/**
 * Created by Administrator on 2016/11/21.
 */

/**
 * 加入蓝牙后废弃次文件（以”testbluetooh.java“ 作为启动的activity）
 *
 * **/
public class MenuActivity extends Activity implements IMChattingHelper.OnMessageReportCallback, View.OnClickListener {

    private Button mButtonMonitor;
    private Button mButtonRobotDistribute;
    private Button mButtonDisplay;
    private String nickName = "18665889098";
    private String contactID = "18665889098";

    private String mobile = "15919939276";
    String pass = "";
    String appKey = "8aaf070858cd982e0158e21ff0000cee";
    String token = "ca8bdec6e6ed3cc369b8122a1c19306d";
    ECInitParams.LoginAuthType mLoginAuthType = ECInitParams.LoginAuthType.NORMAL_AUTH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_menu);

        //save app key/ID and contact number etc. and init rong-lian-yun SDK
        ClientUser clientUser = new ClientUser(mobile);
        clientUser.setAppKey(appKey);
        clientUser.setAppToken(token);
        clientUser.setLoginAuthType(mLoginAuthType);
        clientUser.setPassword(pass);
        CCPAppManager.setClientUser(clientUser);
        SDKCoreHelper.init(MenuActivity.this, ECInitParams.LoginMode.FORCE_LOGIN);
        IMChattingHelper.setOnMessageReportCallback(MenuActivity.this);

        mButtonMonitor = (Button)findViewById(R.id.btn_monitor);
        mButtonRobotDistribute = (Button)findViewById(R.id.btn_remote_control);
        mButtonDisplay = (Button)findViewById(R.id.btn_audio);

        mButtonMonitor.setOnClickListener(this);
        mButtonRobotDistribute.setOnClickListener(this);
        mButtonDisplay.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_monitor:    //VOIP  video
                Toast.makeText(MenuActivity.this,"monitor",Toast.LENGTH_SHORT).show();
                CCPAppManager.callVoIPAction(MenuActivity.this, ECVoIPCallManager.CallType.VIDEO,
                        nickName, contactID,false);
                finish();
                break;
            case R.id.btn_remote_control:    //VOIP IM
                Toast.makeText(MenuActivity.this,"robot_remote_control",Toast.LENGTH_SHORT).show();
                Intent mRemoteCtrIntent = new Intent();
                mRemoteCtrIntent.setClass(MenuActivity.this,RemoteControlCommandActivity.class);
                startActivity(mRemoteCtrIntent);
                break;
            case R.id.btn_audio:    //VOIP audio
                Toast.makeText(MenuActivity.this,"btn_audio",Toast.LENGTH_SHORT).show();
                CCPAppManager.callVoIPAction(MenuActivity.this, ECVoIPCallManager.CallType.VOICE,
                        nickName, contactID,false);
                finish();
                break;

        }
    }
    @Override
    public void onMessageReport(ECError error, ECMessage message) {

    }

    @Override
    public void onPushMessage(String sessionId, List<ECMessage> msgs) {
        int msgsSize = msgs.size();
        String message = " ";
        for (int i = 0; i < msgsSize; i++){
            message = ((ECTextMessageBody) msgs.get(i).getBody()).getMessage();
            Log.d("TIEJIANG", "[MainActivity-onPushMessage]" + "i :" + i + ", message = " + message);// add by tiejiang
        }

        Log.d("TIEJIANG", "[MainActivity-onPushMessage]" + ",sessionId :" + sessionId);// add by tiejiang
//mReceiveEditText.setText(message);
        handleSendTextMessage(message + "callback");
    }
    /**
     * 处理文本发送方法事件通知
     * @param text
     */
    public static void handleSendTextMessage(CharSequence text) {
        if(text == null) {
            return ;
        }
        if(text.toString().trim().length() <= 0) {
//canotSendEmptyMessage();
            return ;
        }
// 组建一个待发送的ECMessage
        ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
// 设置消息接收者
//msg.setTo(mRecipients);
        msg.setTo("18665889098"); // attenionthis number is not the login number! / modified by tiejiang
        ECTextMessageBody msgBody=null;
        Boolean isBQMMMessage=false;
        String emojiNames = null;
//if(text.toString().contains(CCPChattingFooter2.TXT_MSGTYPE)&& text.toString().contains(CCPChattingFooter2.MSG_DATA)){
//try {
//JSONObject jsonObject = new JSONObject(text.toString());
//String emojiType=jsonObject.getString(CCPChattingFooter2.TXT_MSGTYPE);
//if(emojiType.equals(CCPChattingFooter2.EMOJITYPE) || emojiType.equals(CCPChattingFooter2.FACETYPE)){//说明是含有BQMM的表情
//isBQMMMessage=true;
//emojiNames=jsonObject.getString(CCPChattingFooter2.EMOJI_TEXT);
//}
//} catch (JSONException e) {
//e.printStackTrace();
//}
//}
        if (isBQMMMessage) {
            msgBody = new ECTextMessageBody(emojiNames);
            msg.setBody(msgBody);
            msg.setUserData(text.toString());
        } else {
// 创建一个文本消息体，并添加到消息对象中
            msgBody = new ECTextMessageBody(text.toString());
            msg.setBody(msgBody);
            Log.d("TIEJIANG", "[RemoteControlCommandActivity]-handleSendTextMessage" + ", txt = " + text);// add by tiejiang
        }

//String[] at = mChattingFooter.getAtSomeBody();
//msgBody.setAtMembers(at);
//mChattingFooter.clearSomeBody();
        try {
// 发送消息，该函数见上
            long rowId = -1;
//if(mCustomerService) {
//rowId = CustomerServiceHelper.sendMCMessage(msg);
//} else {
            Log.d("TIEJIANG", "[RemoteControlCommandActivity]-SendECMessage");// add by tiejiang
            rowId = IMChattingHelper.sendECMessage(msg);

//}
// 通知列表刷新
//msg.setId(rowId);
//notifyIMessageListView(msg);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TIEJIANG", "[RemoteControlCommandActivity]-send failed");// add by tiejiang
        }
    }
}
