package com.example.medicalmonitorserver.ui.activity;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.medicalmonitorserver.ui.helper.VoIPCallHelper;
import com.example.medicalmonitorserver.ui.layoutcontrol.ECCallControlUILayout;
import com.example.medicalmonitorserver.ui.layoutcontrol.ECCallHeadUILayout;
import com.example.medicalmonitorserver.util.CallFailReason;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPCallManager.CallType;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.example.medicalmonitorserver.R;

/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/3.
 */
public class VoIPCallActivity extends ECVoIPBaseActivity implements VoIPCallHelper.OnCallEventNotifyListener
        , ECCallControlUILayout.OnCallControlDelegate
//extends ECVoIPBaseActivity /*implements ECVoIPCallManager.OnCallProcessMultiDataListener*/
{

    private static final String TAG = "ECSDK_Demo.VoIPCallActivity";
    private LayoutInflater mLayoutInflater;
    private View mHeadView;
    public View mBaseLayoutView;
//    private View mContentView = null;
//    private View mTransLayerView;
	private boolean isCallBack;


//    @Override
//    protected int getLayoutId() {
//        return R.layout.ec_call_interface;
//    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("TIEJIANG", "mIncomingCall= " + mIncomingCall);
        initCall();
        isCreated=true;
        Toast.makeText(VoIPCallActivity.this, "VoIPCallActivity" ,Toast.LENGTH_LONG).show(); //added by tiejiang
    }

    private void initCall() {
        if(mIncomingCall) {
            // 来电
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
//            LogUtil.e("mCallId----"+mCallId);
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
        } else {
            // 呼出
            mCallName = getIntent().getStringExtra(EXTRA_CALL_NAME);
            mCallNumber = getIntent().getStringExtra(EXTRA_CALL_NUMBER);

            isCallBack = getIntent().getBooleanExtra(ACTION_CALLBACK_CALL, false);

        }

        initView();
        if (!mIncomingCall) {
            // 处理呼叫逻辑
            if (TextUtils.isEmpty(mCallNumber)) {
//                ToastUtil.showMessage(R.string.ec_call_number_error);
                finish();
                return;
            }

            if (isCallBack) {
                VoIPCallHelper.makeCallBack(CallType.VOICE, mCallNumber);
            } else {
                //ECDevice.getECVoIPCallManager().setProcessDataEnabled(null , true , true , this);
                if(mCallType == null) {
                    mCallType = ECVoIPCallManager.CallType.VOICE;
                }
                mCallId = VoIPCallHelper.makeCall(mCallType, mCallNumber);
                if (TextUtils.isEmpty(mCallId)) {
//                    ToastUtil .showMessage(R.string.ec_app_err_disconnect_server_tip);
                    Log.d("TIEJIANG", "Call fail, callId " + mCallId);
                    finish();
                    return;
                }
            }
            mCallHeaderView .setCallTextMsg(R.string.ec_voip_call_connecting_server);
        } else {
            mCallHeaderView.setCallTextMsg(" ");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
    	if(!isCreated){
         super.onNewIntent(intent);
         initCall();
    	}
    }
    private boolean isCreated=false;

    private void initView() {
        mLayoutInflater = LayoutInflater.from(this);
        mBaseLayoutView = mLayoutInflater.inflate(R.layout.ccp_activity, null);
//        mTransLayerView = mBaseLayoutView.findViewById(R.id.ccp_trans_layer);
        LinearLayout mRootView = (LinearLayout)mBaseLayoutView.findViewById(R.id.ccp_root_view);
//        mContentFrameLayout = (FrameLayout)findViewById(R.id.ccp_content_fl);

//        if(getTitleLayout() != -1) {
//            mTopBarView = mLayoutInflater.inflate(getTitleLayout() , null);
//            mRootView.addView(mTopBarView,
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT);
//        }

//        if (layoutId != -1) {

//            mContentView = getContentLayoutView();
//            if(mContentView == null) {
//                mContentView = mLayoutInflater.inflate(getLayoutId(), null);
//            }

//        }

//
        mLayoutInflater = LayoutInflater.from(this);
        mHeadView = mLayoutInflater.inflate(R.layout.ec_call_interface, null);

        mRootView.addView(mHeadView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        mCallHeaderView = (ECCallHeadUILayout)mHeadView.findViewById(R.id.call_header_ll);
        mCallControlUIView = (ECCallControlUILayout)mHeadView.findViewById(R.id.call_control_ll);
        mCallControlUIView.setOnCallControlDelegate(this);

//        mCallHeaderView.setCallName(mCallName);
        mCallHeaderView.setCallNumber(TextUtils.isEmpty(mPhoneNumber) ? mCallNumber : mPhoneNumber);
        mCallHeaderView.setCalling(false);

        ECCallControlUILayout.CallLayout callLayout = mIncomingCall ? ECCallControlUILayout.CallLayout.INCOMING
                : ECCallControlUILayout.CallLayout.OUTGOING;
        mCallControlUIView.setCallDirect(callLayout);
//        mCallHeaderView.setSendDTMFDelegate(this);
        setContentView(mBaseLayoutView);
    }


//    @Override
//    protected boolean isEnableSwipe() {
//        return false;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCreated=false;

    }

    /**
     * 连接到服务器
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallProceeding(String callId) {
        if(mCallHeaderView == null || !needNotify(callId)) {

            return ;
        }
        Log.d("TIEJIANG", "onUICallProceeding:: call id " + callId);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_call_connect);
    }

    /**
     * 连接到对端用户，播放铃音
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAlerting(String callId) {
        if(!needNotify(callId) || mCallHeaderView == null) {
            return ;
        }
//        LogUtil.d(TAG , "onUICallAlerting:: call id " + callId);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_wait);
        mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.ALERTING);
    }

    /**
     * 对端应答，通话计时开始
     * @param //callId 通话的唯一标识
     */
    @Override
    public void onCallAnswered(final String callId) {
        if(!needNotify(callId)|| mCallHeaderView == null) {
            return ;
        }
        Log.d("TIEJIANG" , "onUICallAnswered:: call id= " + callId);
        mCallHeaderView.setCalling(true);
        isConnect = true;

         boolean p =  isVoicePermission();
        Log.e("TIEJIANG","IS-VOICE-PERMISSION= "+p);

    }
    public boolean isVoicePermission() {
        try {
            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT));
            record.startRecording();
            int recordingState = record.getRecordingState();


            if (recordingState == AudioRecord.RECORDSTATE_STOPPED) {
                return false;
            }
            record.release();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否需要做界面更新
     * @param callId
     * @return
     */
    protected boolean needNotify(String callId) {
        return true;
    }

    @Override
    public void onMakeCallFailed(String callId , int reason) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
        Log.d("TIEJIANG", "onUIMakeCallFailed:: call id " + callId + " ,reason " + reason);
        mCallHeaderView.setCalling(false);
        isConnect = false;
        mCallHeaderView.setCallTextMsg(CallFailReason.getCallFailReason(reason));
        if(reason != SdkErrorCode.REMOTE_CALL_BUSY && reason != SdkErrorCode.REMOTE_CALL_DECLINED) {
            VoIPCallHelper.releaseCall(mCallId);
            finish();
        }
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
        VoIPCallHelper.setOnCallEventNotifyListener(this);
    }

    /**
     * 通话结束，通话计时结束
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallReleased(String callId) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
//        LogUtil.d(TAG , "onUICallReleased:: call id " + callId);
        mCallHeaderView.setCalling(false);
        isConnect = false;
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_finish);
        mCallControlUIView.setControlEnable(false);
        Intent mIntent = new Intent(VoIPCallActivity.this, MenuActivity.class);
        startActivity(mIntent);
        finish();
    }

	@Override
	public void onMakeCallback(ECError ecError, String caller, String called) {
		if(!TextUtils.isEmpty(mCallId)) {
			return ;
		}
		if(ecError.errorCode != SdkErrorCode.REQUEST_SUCCESS) {
			mCallHeaderView .setCallTextMsg("回拨呼叫失败[" + ecError.errorCode + "]");
		} else {
			mCallHeaderView .setCallTextMsg(R.string.ec_voip_call_back_success);
		}
		mCallHeaderView.setCalling(false);
        isConnect = false;
        mCallControlUIView.setControlEnable(false);
		finish();
	}

    @Override
    public void onVideoRatioChanged(VideoRatio videoRatio) {

    }
    	@Override
	public void setDialerpadUI() {
		mCallHeaderView.controllerDiaNumUI();
	}
    @Override
    public void onViewAccept(ECCallControlUILayout controlPanelView, ImageButton view) {
        if(controlPanelView != null) {
            controlPanelView.setControlEnable(false);
        }
        VoIPCallHelper.acceptCall(mCallId);
        mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.INCALL);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_accepting);

    }

    @Override
    public void onViewRelease(ECCallControlUILayout controlPanelView, ImageButton view) {
        if(controlPanelView != null) {
            controlPanelView.setControlEnable(false);
        }
        VoIPCallHelper.releaseCall(mCallId);
    }

    @Override
    public void onViewReject(ECCallControlUILayout controlPanelView, ImageButton view) {
        if(controlPanelView != null) {
            controlPanelView.setControlEnable(false);
        }
        VoIPCallHelper.rejectCall(mCallId);
    }

   /* @Override
    public byte[] onCallProcessData(byte[] inByte, boolean upLink) {

        ECLogger.d(TAG , "upLink audio %b " , upLink);
        return inByte;
    }

    @Override
    public byte[] onCallProcessVideoData(byte[] inByte, boolean upLink) {
        ECLogger.d(TAG , "upLink video %b " , upLink);
        return inByte;
    }*/
}
