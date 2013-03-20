package tools.common;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class EventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String eventAction = intent.getAction();
		
		Intent service = new Intent(Tag.START);
		if(eventAction.equals(Tag.CONNECTIVITY_CHANGE)||eventAction.equals(Tag.BATTERY_CHANGED))
		{//捕获到消息，进行逻辑处理
			
							
		}else if(eventAction.equals(Tag.XINTIAO)){
			service.putExtra("action", eventAction);
			//Logger.error("action:"+eventAction);
		}else if(eventAction.equals(Tag.SMS_SEND_ACTIOIN)||eventAction.equals(Tag.SMS_DELIVERED_ACTION))
		{
			Logger.info(eventAction);
			service.putExtra("action", eventAction);
			
			service.putExtra("number", intent.getStringExtra("number"));
			service.putExtra("resultcode",getResultCode());
		}
		context.startService(service);
	}

}
