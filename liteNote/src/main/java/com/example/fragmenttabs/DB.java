package com.example.fragmenttabs;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB   
{

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;
    private static String DATABASE_TABLE_PREFIX = "notesTable";
    private static String DATABASE_TABLE;
    private Context mContext = null;
    private DatabaseHelper dbHelper ;
    private static SQLiteDatabase sqldb;
    private static String mTableNum;
	private static int mTabCount = 5; //first time

    /** Constructor */
    public DB(Context context) {
        this.mContext = context;
    }

    //set table number
    public static void setTableNumber(String tblNum)
    {
    	mTableNum = tblNum;
    	System.out.println("DB / _setTableNumber mTableNum=" + mTableNum);
    }    
   
    public DB open() throws SQLException 
    {
        dbHelper = new DatabaseHelper(mContext);      
        DATABASE_TABLE = DATABASE_TABLE_PREFIX.concat(mTableNum);
        
        // will call DatabaseHelper.onCreate()first time when database is not created yet
        sqldb = dbHelper.getWritableDatabase(); //?????????????????
        return this;  
    }

    public void close() {
        dbHelper.close(); 
    }
    
    
    private static class DatabaseHelper extends SQLiteOpenHelper
    {  
        public DatabaseHelper(Context context) 
        {  
            super(context, DATABASE_NAME , null, DATABASE_VERSION);
        }

        @Override
        //Called when the database is created ONLY for the first time.
        public void onCreate(SQLiteDatabase db)
        {   
        	// table for Tab
        	DATABASE_TABLE = "TAB_INFO";
            String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
    				"(tab_id INTEGER PRIMARY KEY,tab_name TEXT, tab_created INTEGER);";
            db.execSQL(DATABASE_CREATE);  
            
        	// tables for notes
        	for(int i=1;i<=mTabCount;i++)
        	{
	        	DATABASE_TABLE = DATABASE_TABLE_PREFIX.concat(String.valueOf(i));//"notesTable1";
	            DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
	    				"(_id INTEGER PRIMARY KEY,note TEXT,marking INTEGER, created INTEGER);";
	            db.execSQL(DATABASE_CREATE);         
        	}
            //back to default
//            DATABASE_TABLE = "notesTable1";
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        { 
            System.out.println("DB / _onUpgrade / DATABASE_TABLE = " + DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS"+DATABASE_TABLE); 
     	    onCreate(db);
        }
    }
    
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NOTE = "note";
    public static final String KEY_MARKING = "marking";
    public static final String KEY_CREATED = "created";
    
    public static final String KEY_TAB_ID = "tab_id";
    public static final String KEY_TAB_NAME = "tab_name";
    public static final String KEY_TAB_CREATED = "tab_created";
    
    // table columns
    String[] strCols = new String[] {
          KEY_ROWID,
          KEY_NOTE,
          KEY_MARKING,
          KEY_CREATED
      };
    
    // for tab
    String[] strColsTab = new String[] {
            KEY_TAB_ID,
            KEY_TAB_NAME,
            KEY_TAB_CREATED
        };

    // select all
    public Cursor getAll() {
        return sqldb.query(DATABASE_TABLE, 
             strCols,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    // select all
    public Cursor getAllTab() {
        return sqldb.query("TAB_INFO", 
             strColsTab,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    // insert tab
    public long insertTab(String DB_table,String tabName) 
    { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_TAB_NAME, tabName);
        args.put(KEY_TAB_CREATED, now.getTime());
        return sqldb.insert(DB_table, null, args);  
    }    
    
    // delete tab
    public long deleteTabInfo(String DB_table,int tabId) 
    { 
//        ContentValues args = new ContentValues(); 
        return sqldb.delete(DB_table, KEY_TAB_ID + "='" + tabId +"'", null);  
    }
    
    // add an entry
    public long insert(String noteName) { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_NOTE, noteName);   
        args.put(KEY_CREATED, now.getTime());
        args.put(KEY_MARKING,0);
        return sqldb.insert(DATABASE_TABLE, null, args);  
    }
    
    public boolean delete(long rowId) {  
        return sqldb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    //query single tab info
    public Cursor get(String DB_table, long tabId) throws SQLException 
    {  
        Cursor mCursor = sqldb.query(true,
        							DB_table,
					                new String[] {KEY_TAB_ID,
        										  KEY_TAB_NAME,
        										  KEY_TAB_CREATED},
					                KEY_TAB_ID + "=" + tabId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    
    //query single entry
    public Cursor get(long rowId) throws SQLException 
    {  
        Cursor mCursor = sqldb.query(true,
					                DATABASE_TABLE,
					                new String[] {KEY_ROWID,
        										  KEY_NOTE,
        										  KEY_MARKING,
        										  KEY_CREATED},
					                KEY_ROWID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    
    //update tab info
    public boolean updateTab(String DB_table, long tabId, String tabName) { 
        ContentValues args = new ContentValues();
        Date now = new Date(); 
        args.put(KEY_TAB_NAME, tabName);
        args.put(KEY_TAB_CREATED, now.getTime());
        return sqldb.update(DB_table, args, KEY_TAB_ID + "=" + tabId, null) > 0;
    }

    //update
    public boolean update(long rowId, String note, long marking) { 
        ContentValues args = new ContentValues();
        Date now = new Date(); 
        args.put(KEY_NOTE, note);
        args.put(KEY_MARKING, marking);
        args.put(KEY_CREATED, now.getTime());
        return sqldb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    //insert new table
    public void insertNewTable(int tableId)
    {   
    	{
    		//format "notesTable1"
        	DATABASE_TABLE = DATABASE_TABLE_PREFIX.concat(String.valueOf(tableId));
            String DATABASE_INSERT = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
    				"(_id INTEGER PRIMARY KEY,note TEXT,marking INTEGER, created INTEGER);";
            sqldb.execSQL(DATABASE_INSERT);         
    	}
    }
    
    //delete table
    public void dropTable(int tableId)
    {   
    	{
    		//format "notesTable1"
        	DATABASE_TABLE = DATABASE_TABLE_PREFIX.concat(String.valueOf(tableId));
            String DATABASE_DROP = "DROP TABLE IF EXISTS " + DATABASE_TABLE + ";";
            sqldb.execSQL(DATABASE_DROP);         
    	}
    }

	public int getMaxId() {
		Cursor cursor = this.getAll();
		int total = cursor.getColumnCount();
		int iMax =1;
		int iTemp = 1;
		for(int i=0;i< total;i++)
		{
			cursor.moveToPosition(i);
			iTemp = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
			iMax = (iTemp >= iMax)? iTemp: iMax;
		}
		return iMax;
	}
}