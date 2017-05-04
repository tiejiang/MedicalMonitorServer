package com.example.medicalmonitorserver.ui.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.medicalmonitorserver.R;
import com.example.medicalmonitorserver.common.CCPAppManager;
import com.example.medicalmonitorserver.core.ClientUser;
import com.example.medicalmonitorserver.ui.helper.IMChattingHelper;
import com.example.medicalmonitorserver.ui.helper.SDKCoreHelper;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class testBlueTooth extends Activity implements IMChattingHelper.OnMessageReportCallback{
	static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private UUID uuid ;
	private static final String TAG = "BluetoothTest";
	private static final boolean STATE_CONNECTED = true;

	private Button btnSearch, btnDis, btnExit;
	private ToggleButton tbtnSwitch;

	private ListView lvBTDevices;
	private ArrayAdapter<String> adtDevices;
	private List<String> lstDevices = new ArrayList<String>();
	private BluetoothAdapter btAdapt;

	public static BluetoothSocket socket = null;
	public static BluetoothSocket btSocket;
	public static AcceptThread serverThread;



	private String mobile = "20170418";
	String pass = "";
	String appKey = "8aaf070858cd982e0158e21ff0000cee";
	String token = "ca8bdec6e6ed3cc369b8122a1c19306d";
	ECInitParams.LoginAuthType mLoginAuthType = ECInitParams.LoginAuthType.NORMAL_AUTH;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//save app key/ID and contact number etc. and init rong-lian-yun SDK
		ClientUser clientUser = new ClientUser(mobile);
		clientUser.setAppKey(appKey);
		clientUser.setAppToken(token);
		clientUser.setLoginAuthType(mLoginAuthType);
		clientUser.setPassword(pass);
		CCPAppManager.setClientUser(clientUser);
		SDKCoreHelper.init(testBlueTooth.this, ECInitParams.LoginMode.FORCE_LOGIN);
		IMChattingHelper.setOnMessageReportCallback(testBlueTooth.this);

		// Button 设置
		btnSearch = (Button) this.findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(new ClickEvent());

		btnExit = (Button) this.findViewById(R.id.btnExit);
		btnExit.setOnClickListener(new ClickEvent());

		btnDis = (Button) this.findViewById(R.id.btnDis);
		btnDis.setOnClickListener(new ClickEvent());

		// ToogleButton设置
		tbtnSwitch = (ToggleButton) this.findViewById(R.id.tbtnSwitch);
		tbtnSwitch.setOnClickListener(new ClickEvent());

		// ListView及其数据源 适配器
		lvBTDevices = (ListView) this.findViewById(R.id.lvDevices);
		adtDevices = new ArrayAdapter<String>(testBlueTooth.this,
				android.R.layout.simple_list_item_1, lstDevices);
		lvBTDevices.setAdapter(adtDevices);
		lvBTDevices.setOnItemClickListener(new ItemClickEvent());

		btAdapt = BluetoothAdapter.getDefaultAdapter();// 初始化本机蓝牙功能
		uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		if(btAdapt == null){
			Log.e(TAG, "No BlueToothDevice!");
			finish();
			return;
		}
		else{
			if (btAdapt.getState() == BluetoothAdapter.STATE_OFF)// 读取蓝牙状态并显示
			{
				tbtnSwitch.setChecked(true);
				Toast.makeText(testBlueTooth.this, "蓝牙尚未打开,服务端需先打开蓝牙", Toast.LENGTH_LONG).show();
			}
			else if (btAdapt.getState() == BluetoothAdapter.STATE_ON){
				tbtnSwitch.setChecked(false);
				//服务端监听
				serverThread=new AcceptThread();
				serverThread.start();
			}
			// 注册Receiver来获取蓝牙设备相关的结果
			IntentFilter intent = new IntentFilter();
			intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
			intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
			intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			registerReceiver(searchDevices, intent);
		}
	}
	private void manageConnectedSocket() {
		//setTitle("检测到蓝牙接入！");
		btSocket=socket;
		//打开控制继电器实例
		Intent intent = new Intent();
		intent.setClass(testBlueTooth.this, RelayControl.class);
		startActivity(intent);
	}
	private BroadcastReceiver searchDevices = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle b = intent.getExtras();
			Object[] lstName = b.keySet().toArray();
			// 显示所有收到的消息及其细节
			for (int i = 0; i < lstName.length; i++) {
				String keyName = lstName[i].toString();
				Log.e(keyName, String.valueOf(b.get(keyName)));
			}
			//搜索设备时，取得设备的MAC地址
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String str= device.getName() + "|" + device.getAddress();
				if (lstDevices.indexOf(str) == -1)// 防止重复添加
					lstDevices.add(str); // 获取设备名称和mac地址
				adtDevices.notifyDataSetChanged();
			}
		}
	};

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(searchDevices);
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
		serverThread.cancel();
		serverThread.destroy();
	}

	class ItemClickEvent implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			String str = lstDevices.get(arg2);
			String[] values = str.split("\\|");
			String address=values[1];
			Log.e("address",values[1]);
			uuid = UUID.fromString(SPP_UUID);
			Log.e("uuid",uuid.toString());
			BluetoothDevice btDev = btAdapt.getRemoteDevice(address);//"00:11:00:18:05:45"
			//int sdk = Integer.parseInt(Build.VERSION.SDK);
			/*
			int sdk = Integer.parseInt(Build.VERSION.SDK);
			if (sdk >= 10) {
			     try {
					btSocket = btDev.createInsecureRfcommSocketToServiceRecord(uuid);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, " High: Connection failed.", e);
				}
			} else {
			      try {
					btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "Low: Connection failed.", e);
				}
			}*/
			Method m;
			try {
				m = btDev.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
				btSocket = (BluetoothSocket) m.invoke(btDev, Integer.valueOf(1));
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//				 try {
//						btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						Log.e(TAG, "Low: Connection failed.", e);
//					}


//				btSocket = InsecureBluetooth.listenUsingRfcommWithServiceRecord(btAdapt, "", uuid, true);
//				btSocket = InsecureBluetooth.createRfcommSocketToServiceRecord(btDev, uuid, true);

			btAdapt.cancelDiscovery();
			try {
				//btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
				btSocket.connect();
				Log.e(TAG, " BT connection established, data transfer link open.");

				Toast.makeText(testBlueTooth.this, "连接成功,进入控制界面", Toast.LENGTH_SHORT).show();
				//setTitle("连接成功");

				//打开控制继电器实例
				Intent intent = new Intent();
				intent.setClass(testBlueTooth.this, RelayControl.class);
				startActivity(intent);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, " Connection failed.", e);
				//Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT);
				setTitle("连接失败..");
			}
		}
	}

	private	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == btnSearch)// 搜索蓝牙设备，在BroadcastReceiver显示结果
			{
				if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {// 如果蓝牙还没开启
					Toast.makeText(testBlueTooth.this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
					return;
				}
				setTitle("本机蓝牙地址：" + btAdapt.getAddress());
				lstDevices.clear();
				btAdapt.startDiscovery();
			} else if (v == tbtnSwitch) {// 本机蓝牙启动/关闭
				if (tbtnSwitch.isChecked() == false)
				{
					btAdapt.enable();
					try {
						Thread.sleep(5 * 1000);//延时5s
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //暂停1秒钟
					//服务端监听
					serverThread=new AcceptThread();
					serverThread.start();
					Toast.makeText(testBlueTooth.this, "服务端监听已打开", Toast.LENGTH_SHORT).show();
				}
				else if (tbtnSwitch.isChecked() == true)
					btAdapt.disable();
			} else if (v == btnDis)// 本机可以被搜索
			{
				Intent discoverableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(
						BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
			} else if (v == btnExit) {
				try {
					if (btSocket != null)
						btSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				testBlueTooth.this.finish();
				//test code begin
//				Intent intent = new Intent();
//				intent.setClass(testBlueTooth.this, RelayControl.class);
//				startActivity(intent);
				//test code end
			}
		}
	}

	class AcceptThread extends Thread {
		private final BluetoothServerSocket serverSocket;
		public AcceptThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp=null;
			try {
				//tmp = btAdapt.listenUsingRfcommWithServiceRecord("MyBluetoothApp", uuid);

				Log.e(TAG, "++BluetoothServerSocket established!++");
				Method listenMethod = btAdapt.getClass().getMethod("listenUsingRfcommOn", new Class[]{int.class});
				tmp = ( BluetoothServerSocket) listenMethod.invoke(btAdapt, Integer.valueOf( 1));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverSocket=tmp;
		}

		public void run() {
			// Keep listening until exception occurs or a socket is returned
			//mState!=STATE_CONNECTED
			while(true) {
				try {
					socket = serverSocket.accept();
					Log.e(TAG, "++BluetoothSocket established! DataLink open.++");
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					manageConnectedSocket();
					try {
						serverSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				serverSocket.close();
			} catch (IOException e) { }
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
		msg.setTo("81407102"); // attenionthis number is not the login number! / modified by tiejiang
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