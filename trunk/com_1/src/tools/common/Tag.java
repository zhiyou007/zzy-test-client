package tools.common;

public class Tag {
	public static String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
	public static String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
	public static String SMS_RECEIVER = "android.provider.Telephony.SMS_RECEIVED";
	//自定义
	public static String XINTIAO = "android.intent.action.XINTIAO";
	public static final String SMS_SEND_ACTIOIN = "SEND_ACTIOIN";  
	public static final String SMS_DELIVERED_ACTION = "DELIVERED_ACTION";
	public static final String SENDTOSERVER = "android.sendtoserver";
	public static final String START = "action.start";
	
	//public static String HOST = "azcs.f3322.org";
	public static String HOST = "192.168.254.108";
	//public static String HOST = "192.168.1.105";
	public static int PORT = 8890;
	public static int SOCKET_TIMEOUT = 5000;
	//public static final String KEY = "386d16f5";
}
