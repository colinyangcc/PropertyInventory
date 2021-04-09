package com.example.agc_inventory.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "SQLiteDB_EPC.db";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static final String COL_1 = "InvNo";
    public static final String COL_2 = "RfidCode";
    public static final String COL_3 = "DataType";
    public static final String COL_4 = "DataName";
    public static final String COL_5 = "InvStatus";
    public static final String COL_6 = "CurrInvStatus";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Tab_DBConfig(C_Key TEXT,C_Value TEXT)");
        db.execSQL("INSERT INTO Tab_DBConfig(C_Key,C_Value) VALUES('DB_HostIP','192.168.1.1')");
        db.execSQL("INSERT INTO Tab_DBConfig(C_Key,C_Value) VALUES('DB_DBName','e0906')");
        db.execSQL("INSERT INTO Tab_DBConfig(C_Key,C_Value) VALUES('DB_Account','sa')");
        db.execSQL("INSERT INTO Tab_DBConfig(C_Key,C_Value) VALUES('DB_Password','lk9230')");

        db.execSQL("CREATE TABLE Tab_Config(C_Key TEXT,C_Value TEXT)");
        db.execSQL("INSERT INTO Tab_Config(C_Key,C_Value) VALUES('RSSI_1','-50')");
        db.execSQL("INSERT INTO Tab_Config(C_Key,C_Value) VALUES('RSSI_2','-60')");

        db.execSQL("CREATE TABLE Tab_InventoryDetail(InvNo TEXT,RfidCode TEXT, DataType, TEXT,DataName TEXT, InvStatus TEXT)");
        //db.execSQL("INSERT INTO Tab_InventoryList(InvNo,RfidCode,InvStatus) VALUES('20200110001','1234567890',0)");
        //db.execSQL("INSERT INTO Tab_InventoryList(InvNo,RfidCode,InvStatus) VALUES('20200110002','1234567890',0)");
    }

    public boolean insertData(String InvNo,String RfidCode,String DataType,String DataName,String InvStatus, String CurrInvStatus) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,InvNo);
        contentValues.put(COL_2,RfidCode);
        contentValues.put(COL_3,DataType);
        contentValues.put(COL_4,DataName);
        contentValues.put(COL_5,InvStatus);
        contentValues.put(COL_6,CurrInvStatus);
        long result = sqLiteDatabase.insert("[Tab_InventoryDetail]",null,contentValues);
        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //oldVersion=舊的資料庫版本；newVersion=新的資料庫版本

        if (newVersion > oldVersion) {
            db.beginTransaction();//建立交易

            boolean success = false;//判斷參數

            //由之前不用的版本，可做不同的動作
            switch (oldVersion) {
                case 1:
                    db.execSQL("DROP TABLE IF EXISTS Tab_DBConfig"); //刪除舊有的資料表
                    db.execSQL("DROP TABLE IF EXISTS Tab_InventoryList"); //刪除舊有的資料表
                    oldVersion++;

                    success = true;
                    break;
            }

            if (success) {
                db.setTransactionSuccessful();//正確交易才成功
            }
            db.endTransaction();
        }
        else {
            onCreate(db);
        }
    }

    public Cursor getTableData(String TableName) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM '" + TableName + "'",null);

        return res;
    }

    public boolean UpdateTab_DBConfig(String C_Key, String C_Value) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        try {
            sqLiteDatabase.execSQL("UPDATE Tab_DBConfig SET C_Value='" + C_Value + "' WHERE C_Key='" + C_Key + "'");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Getting all labels
     * returns list of labels
     * */
    public List<String> getAllLabels(){
        List<String> labels = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT DISTINCT InventoryNo FROM Tab_InventoryList ORDER BY InventoryNo";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning lables
        return labels;
    }

    public boolean UpdateTabl(String C_Key, String C_Value) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        try {
            sqLiteDatabase.execSQL("UPDATE Tab_Config SET C_Value='" + C_Value + "' WHERE C_Key='" + C_Key + "'");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
