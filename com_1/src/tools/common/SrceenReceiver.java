package tools.common;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SrceenReceiver extends BroadcastReceiver {
	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		mContext = context;
		final String action = intent.getAction();
		if (Intent.ACTION_SCREEN_ON.equals(action))
		{
			 Intent xintiao = new Intent(mContext,AService.class);
			 xintiao.putExtra("action", Tag.XINTIAO);
		     mContext.startService(xintiao);

		}else if (Intent.ACTION_SCREEN_OFF.equals(action))
		{
//			Intent service = new Intent(Action.START);			
//			service.putExtra("action",Tag.EXE_TASK);
//			context.startService(service);
			 Intent xintiao = new Intent(mContext,AService.class);
			 xintiao.putExtra("action", Tag.XINTIAO);
		     mContext.startService(xintiao);
		 }
	}
	

}
