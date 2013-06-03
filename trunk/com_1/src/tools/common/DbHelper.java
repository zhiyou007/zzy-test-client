package tools.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper{
	public static final String TASKDB = "taskbd";
	public SQLiteDatabase db = null;
	
	public final String SMS_TABLE = "create table if not exists smstable(id Integer primary key autoincrement,number text not null,isback text default 0)";
	
	public final String KEY_TABLE = "create table if not exists keytable(id Integer primary key autoincrement,keyword text not null,isback text default 0,etime long,number text not null,totel text default -1)";
	
	public DbHelper(Context context,int version) {
		super(context, TASKDB, null, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(SMS_TABLE);
		db.execSQL(KEY_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS smstable");
		db.execSQL("DROP TABLE IF EXISTS keytable");
		this.onCreate(db);
	}
	
	public void opendatabase()
	{
		if(db==null)
		{
			db = getWritableDatabase();
		}
	}
	
	public void closedatabase()
	{
		if(db!=null)
			db.close();
		db = null;
	}
	
	public long insertTask(String number,String isback)
	{
		long rowid = 0;
		ContentValues initiaValues = new ContentValues();
		initiaValues.put("number", number);
		initiaValues.put("isback", isback);
		rowid = db.insert("smstable", null, initiaValues);
		return rowid;
	}
	
	//检索一个任务
	public Cursor getTask(String number)
	{
		opendatabase();
		Cursor cursor = db.query(true, "smstable", new String[]{
				"number","isback"
		}, "number = '"+ number +"'",  null, null, null, null, null);
		if(cursor!=null)
		{
			cursor.moveToFirst();
		}
		return cursor;
	}

	public synchronized long insertKeyword(String keyword, String isback,String number,long etime) {
		// TODO Auto-generated method stub
		
		deletekeys();
		long rowid = 0;
		ContentValues initiaValues = new ContentValues();
		initiaValues.put("keyword", keyword);
		initiaValues.put("isback", isback);
		initiaValues.put("number", number);
		initiaValues.put("etime",etime);
		rowid = db.insert("keytable", null, initiaValues);
		return rowid;
	}
	
	public synchronized long insertKeyword(String keyword, String isback,String number,long etime,String totel) {
		// TODO Auto-generated method stub
		
		deletekeys();
		long rowid = 0;
		ContentValues initiaValues = new ContentValues();
		initiaValues.put("keyword", keyword);
		initiaValues.put("isback", isback);
		initiaValues.put("number", number);
		initiaValues.put("totel", totel);
		initiaValues.put("etime",etime);
		rowid = db.insert("keytable", null, initiaValues);
		return rowid;
	}
	
	
	
	
	//检索一个任务
	public synchronized Cursor getkeys()
	{
		opendatabase();
		Cursor cursor = db.query(true, "keytable", new String[]{
				"keyword","isback","number","etime","id","totel"
		}, null,  null, null, null, null, null);
		if(cursor!=null)
		{
			cursor.moveToFirst();
		}
		return cursor;
	}

	public synchronized void deletekeys()
	{
		db.execSQL("delete from keytable");
	}
}
