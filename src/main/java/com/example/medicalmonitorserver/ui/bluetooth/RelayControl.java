package com.example.medicalmonitorserver.ui.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalmonitorserver.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * 启动荣联云 VoIP video的方法：
 * 启动video：
 * CCPAppManager.callVoIPAction(MenuActivity.this, ECVoIPCallManager.CallType.VIDEO, nickName, contactID,false);
 * 启动文本IM：
 * handleSendTextMessage(String string);
 *
 * client 端收取数据格式为：
 * eg： text="2.0,4.0,5.0,9.0,6.0,5.0,2.0,"
 *
 * **/

public class RelayControl extends Activity{
	public static boolean isRecording = false;// 线程控制标记
	private Button releaseCtrl,btBack,distance_display;
	private Button car_left, car_right, car_back;
	private OutputStream outStream = null;
	private EditText _txtRead;
	private ConnectedThread manageThread;
	private Handler mHandler;
	private String  encodeType ="GBK";
	private Vibrator mVibrator;
	private String nickName = "91407102";
	private String contactID = "91407102";
	private String readStr1 = "";

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.relaycontrol);
		//接收线程启动
		manageThread = new ConnectedThread();
		mHandler = new MyHandler();
		manageThread.Start();
		findMyView();
		setMyViewListener();
		setTitle("返回前需先关闭socket连接");
		//接收区不可见
		_txtRead.setCursorVisible(false);      //设置输入框中的光标不可见
		_txtRead.setFocusable(false);           //无焦点
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	private void findMyView() {
		releaseCtrl=(Button)findViewById(R.id.button1);
		btBack=(Button) findViewById(R.id.button2);
		_txtRead = (EditText) findViewById(R.id.etShow);
	}

	private void setMyViewListener() {
		releaseCtrl.setOnClickListener(new ClickEvent());
	}

	@Override
	public void onDestroy()
	{
		try {
			testBlueTooth.btSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onDestroy();
		mVibrator.cancel();
	}


	private	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == releaseCtrl)// 释放连接
			{
				try {
					testBlueTooth.btSocket.close();
					manageThread.Stop();
					//testBlueTooth.serverThread.cancel();
					//Toast.makeText(getApplicationContext(), "socket连接已关闭", Toast.LENGTH_SHORT);
					setTitle("socket连接已关闭");
				} catch (IOException e) {
					//Log .e(TAG,"ON RESUME: Unable to close socket during connection failure", e2);
					//Toast.makeText(getApplicationContext(), "关闭连接失败", Toast.LENGTH_SHORT);
					setTitle("关闭连接失败");
				}
			}else if (v == btBack) {// 返回
				RelayControl.this.finish();
			}
		}
	}
	public static void setEditTextEnable(TextView view,Boolean able){
		// view.setTextColor(R.color.read_only_color);   //设置只读时的文字颜色
		if (view instanceof EditText){
			view.setCursorVisible(able);      //设置输入框中的光标不可见
			view.setFocusable(able);           //无焦点
			view.setFocusableInTouchMode(able);     //触摸时也得不到焦点
		}
	}
	public void sendMessage(String message) {
		//控制模块
		try {
			outStream = testBlueTooth.btSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
			Toast.makeText(getApplicationContext(), " Output stream creation failed.", Toast.LENGTH_SHORT).show();
		}
		byte[] msgBuffer = null;
		try {
			msgBuffer = message.getBytes(encodeType);//编码
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("write", "Exception during write encoding GBK ", e1);
		}
		//while(true){
		try {
			outStream.write(msgBuffer);
			//Toast.makeText(getApplicationContext(), "发送数据中..", Toast.LENGTH_SHORT);
			setTitle("成功发送指令:"+message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Log.e(TAG, "ON RESUME: Exception during write.", e);
			Toast.makeText(getApplicationContext(), "发送数据失败", Toast.LENGTH_SHORT).show();
		}
	}
	class ConnectedThread extends Thread {
		private InputStream inStream = null;// 蓝牙数据输入流
		private long wait;
		private Thread thread;

		public ConnectedThread() {
			isRecording = false;
			this.wait=50;
			thread =new Thread(new ReadRunnable());
		}
		public void Stop() {
			isRecording = false;
		}
		public void Start() {
			isRecording = true;
			State aa = thread.getState();
			if(aa==State.NEW){
				thread.start();
			}else thread.resume();
		}
		private class ReadRunnable implements Runnable {
			public void run() {
				while (isRecording) {
					try {
						inStream = testBlueTooth.btSocket.getInputStream();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
						Toast.makeText(getApplicationContext(), " input stream creation failed.", Toast.LENGTH_SHORT).show();
					}
					//char[]dd= new  char[40];
					int length=20;
					byte[] temp = new byte[length];
					//String readStr="";
					//keep listening to InputStream while connected
					if (inStream!= null) {
						try{
							int len = inStream.read(temp,0,length-1);
							Log.e("available", String.valueOf(len));
							//setTitle("available"+len);
							if (len > 0) {
								byte[] btBuf = new byte[len];
								System.arraycopy(temp, 0, btBuf, 0, btBuf.length);
								String sendToUI = readStr1;
								//读编码
								readStr1 = new String(btBuf,encodeType);
								sendToUI = sendToUI + readStr1;
								Log.d("TIEJIANG", "sendToUI= " + sendToUI);
								if ( readStr1.contains("W") && readStr1.contains("X")){
									mHandler.obtainMessage(01,len,-1,sendToUI).sendToTarget();
									sendToUI = "";
								}else if(readStr1.trim().length() > 22) {
									sendToUI = "";
								}
							}
							Thread.sleep(wait);// 延时一定时间缓冲数据
						}catch (Exception e) {
							// TODO Auto-generated catch block
							mHandler.sendEmptyMessage(00);
						}
					}
				}
			}
		}
	}
	private class MyHandler extends Handler{
		@Override
		public void dispatchMessage(Message msg) {
			switch(msg.what){
				case 00:
					isRecording=false;
					_txtRead.setText("");
					_txtRead.setHint("socket连接已关闭");
					//_txtRead.setText("inStream establishment Failed!");
					break;

				case 01:
					//_txtRead.setText("");
					String info=(String) msg.obj;
					Log.d("TIEJIANG", "INFO= " + info);
					_txtRead.append(info);
//					AnalyzeData(info);
					if (info.contains("W") || info.contains("X") || info.contains("E")){
						//启动荣联云 VoIP video的方法：
						testBlueTooth.handleSendTextMessage(info);
						Log.d("TIEJIANG", "SEND TO CLIENT SUCCEED!");
				//		CCPAppManager.callVoIPAction(RelayControl.this, ECVoIPCallManager.CallType.VIDEO, nickName, contactID,false);
					}
					break;

				default:
					break;
			}
		}
		public void AnalyzeData(String data){

			String[] tempData = new String[3];
			String[] ArrayDistance = new String[3];
			int distance = 0;
			tempData = data.split(",");
			System.out.println("原始数据为："+data);
			System.out.println("数组的长度为："+tempData.length);
			System.out.println("数组长度为1时候-Data=" + data);
//    		if (data.equals("1")) {
//    			car_left.setBackgroundColor(Color.RED);
//				mVibrator.vibrate(pattern,2);
//				Log.d("EQUALS", "111");
//			}else if (data.equals("3")) {
//    			car_right.setBackgroundColor(Color.RED);
//				mVibrator.vibrate(pattern,2);
//				Log.d("EQUALS", "333");
//			}
//    		if (tempData.length>1) {
//    			ArrayDistance = tempData[1].split("\\."); //'.'为必须通过转义字符的方式才能够使用split方法
//				Log.d("distance=", ArrayDistance[0]);
//				distance = Integer.parseInt(ArrayDistance[0].trim());//注意去掉前后空格
//				distance_display.setText(distance+" cm");
//
//				Log.d("数组第二位的值：", tempData[1]);
//				if (distance>0 && distance<20) {
//					car_back.setBackgroundColor(Color.RED);
//					mVibrator.vibrate(pattern,2);   //重复两次上面的pattern 如果只想震动一次，index设为-1
//				}
//			}
		}
	}
}