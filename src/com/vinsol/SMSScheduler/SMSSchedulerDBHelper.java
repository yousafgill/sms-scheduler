package com.vinsol.SMSScheduler;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vinsol.SMSScheduler.ContactAppManager.ContactInfo;

public class SMSSchedulerDBHelper extends SQLiteOpenHelper {

	//database details
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "sms_scheduler.db";
    
	//table name
	private static final String MESSAGE_TABLE_NAME = "message";
    private static final String CONTACT_TABLE_NAME = "Contact";
 
    //MESSAGE_TABLE columns name
    private static final String MESSAGE_TABLE_COLUMN_ID = "_id";
    private static final String MESSAGE_TABLE_COLUMN_SCHEDULED_TIME = "scheduled_time";
    private static final String MESSAGE_TABLE_COLUMN_MESSAGE_BODY = "message_body";
    private static final String MESSAGE_TABLE_COLUMN_STATUS = "status";
    
    //CONTACT_TABLE columns name
    private static final String CONTACT_TABLE_COLUMN_ID = "_id";
    private static final String CONTACT_TABLE_MESSAGE_ID = "message_id";
    private static final String CONTACT_TABLE_COLUMN_CONTACT_NUMBER = "contact_number";
    private static final String CONTACT_TABLE_COLUMN_CONTACT_NAME = "name";
    
    
    //create SMS table String
    private static final String CREATE_SMS_TABLE =
                "CREATE TABLE " + MESSAGE_TABLE_NAME + " (" 
                + MESSAGE_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MESSAGE_TABLE_COLUMN_SCHEDULED_TIME + " TEXT, "
                + MESSAGE_TABLE_COLUMN_MESSAGE_BODY + " TEXT, "
                + MESSAGE_TABLE_COLUMN_STATUS + " TEXT"
                + ");";
    
    //create contact table string
    private static final String CREATE_CONTACT_TABLE =
		        "CREATE TABLE " + CONTACT_TABLE_NAME + " (" 
		        + CONTACT_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		        + CONTACT_TABLE_MESSAGE_ID + " TEXT, "
		        + CONTACT_TABLE_COLUMN_CONTACT_NUMBER + " TEXT, " 
		        + CONTACT_TABLE_COLUMN_CONTACT_NAME + " TEXT" 
		        + ");";
    
    SQLiteDatabase SMSSchedulerDBObject;

    /**=====================================================================
     * constructor
     * @param context
     *======================================================================*/
    SMSSchedulerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**=====================================================================
     * method onCreate
     *======================================================================*/
    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.v("in SMSSchedulerDBHelper -> in OnCreate", " before execSQL");
        db.execSQL(CREATE_SMS_TABLE);
        db.execSQL(CREATE_CONTACT_TABLE);
        Log.v("in SMSSchedulerDBHelper -> in OnCreate", " after execSQL");
    }//end method onCreate
    
    /**=====================================================================
     * method onUpgrade
     *======================================================================*/
    @Override
    public void onUpgrade(SQLiteDatabase  db, int oldVersion, int newVersion){
    }//end method onUpgrade
    
    /**=====================================================================
     * method checkOrCreateDB
     *======================================================================*/
    public boolean checkOrCreateDB(){
    
    	try{
    		SMSSchedulerDBObject = getReadableDatabase();
    		SMSSchedulerDBObject.close();
    		return true;
    	}catch(SQLiteException sqle){
    		return false;
    	}
    }//end method checkOrCreateDB
    
    /**=====================================================================
     * method add message
     *======================================================================*/
    public long addMessage(String message, String scheduledTime) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    	
    	ContentValues messageValues = new ContentValues();
        messageValues.put(MESSAGE_TABLE_COLUMN_SCHEDULED_TIME, scheduledTime);
        messageValues.put(MESSAGE_TABLE_COLUMN_MESSAGE_BODY, message);
        messageValues.put(MESSAGE_TABLE_COLUMN_STATUS, Constant.STATUS_SCHEDULED);
        
        long resultRow = -1;
        
        try {
        	resultRow = SMSSchedulerDBObject.insertOrThrow(MESSAGE_TABLE_NAME, null, messageValues);
        }catch(SQLException sqle) {
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> addMessage -> in catch", "SQLException has occurred" + sqle);
        }finally {
        	SMSSchedulerDBObject.close();
        }
    	return resultRow;
    }//end method addSMS
    
    /**=====================================================================
     * method retrieveMessages
     *======================================================================*/
    public ArrayList<Message> retrieveMessages() {
    	  
    	SMSSchedulerDBObject = getReadableDatabase();

		Cursor messagesCursor = SMSSchedulerDBObject.query(MESSAGE_TABLE_NAME, null, null, null, null, null, null);
       	
		ArrayList<Message> messagesList = new ArrayList<Message>();
		
		if (messagesCursor.moveToFirst()) {
			do {
				Message messageObject = new Message();
				messageObject.id = messagesCursor.getInt(0);
				messageObject.scheduledTimeInMilliSecond = messagesCursor.getLong(1);
				messageObject.messageBody = messagesCursor.getString(2);
				messageObject.status = messagesCursor.getInt(3);
				
				messagesList.add(messageObject);
			} while (messagesCursor.moveToNext());
		}else {
			messagesList = null;
		}
		if (messagesCursor != null && !messagesCursor.isClosed()) {
			messagesCursor.close();
		}
		
		SMSSchedulerDBObject.close();
		
		return messagesList;

    }//end method retrieveMessages
    
    
    /**=====================================================================
     * method delete message
     *======================================================================*/
    public boolean deleteSMS(String idOfMessage) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    	
    	int affectedRow = 0;
        
        try {
        	affectedRow = SMSSchedulerDBObject.delete(MESSAGE_TABLE_NAME, "_id=" + idOfMessage , null);
        	Log.v("row id of created record = ", "" + affectedRow);
        }catch(SQLException sqle) {
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> deleteSMS -> in catch", "SQLException has occurred" + sqle);
        }finally {
        	SMSSchedulerDBObject.close();
        }
        
        if(affectedRow == 0) {
        	return false;
        }else{
        	return true;
        }
    }//end method deleteSMS
    
    /**=====================================================================
     * method add contacts
     *======================================================================*/
    public void addContacts(long messageID, ArrayList<ContactInfo> contactInfoList) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    
    	for(int i=0; i<contactInfoList.size(); i++) {
    		
    		String contactNumber = contactInfoList.get(i).getPhoneNumber();
    		String displayName = contactInfoList.get(i).getDisplayName();
    		
    		ContentValues contactValues = new ContentValues();
            
    		
    		contactValues.put(CONTACT_TABLE_MESSAGE_ID, messageID);
            contactValues.put(CONTACT_TABLE_COLUMN_CONTACT_NUMBER, contactNumber);
            contactValues.put(CONTACT_TABLE_COLUMN_CONTACT_NAME, displayName);
            
            try {
            	SMSSchedulerDBObject.insertOrThrow(CONTACT_TABLE_NAME, null, contactValues);
            }catch(SQLException sqle) {
            	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> addContacts -> in catch", "SQLException has occurred" + sqle);
            }
    	}//end for
    	
    	SMSSchedulerDBObject.close();
    	
    }//end method addContacts
    
}//end class SMSSchedulerDBHelper

