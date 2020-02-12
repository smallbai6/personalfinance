package com.personalfinance.app.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    public static final String create_userinfo = "CREATE TABLE userinfo ("
            + " User_Name varchar(45)  NOT NULL,"
            + " User_Password varchar(45) DEFAULT NULL,"
            + "User_Login int(11) NOT NULL,"
            + " PRIMARY KEY (User_Name))";
    public static final String create_incomtype = "CREATE TABLE incometype("
            + "IncomeType_Name varchar(45)  NOT NULL,"
            + "PRIMARY KEY (IncomeType_Name))";
    public static final String create_expendtype = "CREATE TABLE expendtype("
            + "ExpendType_Name varchar(45)  NOT NULL,"
            + "PRIMARY KEY (ExpendType_Name))";
    public static final String create_incomeinfo = "CREATE TABLE incomeinfo("
            + "User_Name varchar(45)  NOT NULL,"
            + "Income_Money varchar(45) NOT NULL,"
            + "Income_Type varchar(45)  NOT NULL,"
            + "Income_Time int(11) NOT NULL,"
            + "Income_Message text ,"
            + "CONSTRAINT incomeinfo_ibfk_1 FOREIGN KEY (User_Name) REFERENCES userinfo (User_Name) ON DELETE CASCADE ON UPDATE CASCADE,"
            + "CONSTRAINT incomeinfo_ibfk_2 FOREIGN KEY (Income_Type) REFERENCES incometype (IncomeType_Name) ON DELETE CASCADE ON UPDATE CASCADE"
            + ")";
    public static final String create_expendinfo = "CREATE TABLE expendinfo("
            + " User_Name varchar(45)  NOT NULL,"
            + " Expend_Money varchar(45) NOT NULL,"
            + " Expend_Type varchar(45)  NOT NULL,"
          //  + " Expend_Time varchar(45) NOT NULL,"
            + " Expend_Time int(11) NOT NULL,"
            + " Expend_Message text ,"
            + " CONSTRAINT expendinfo_ibfk_1 FOREIGN KEY (User_Name) REFERENCES userinfo (User_Name) ON DELETE CASCADE ON UPDATE CASCADE,"
            + " CONSTRAINT expendinfo_ibfk_2 FOREIGN KEY (Expend_Type) REFERENCES expendtype (ExpendType_Name) ON DELETE CASCADE ON UPDATE CASCADE"
            + ")";
    public static final String create_incomebudget = "CREATE TABLE incomebudget("
            + " User_Name varchar(45)  NOT NULL,"
            + " IncomeBudget_Type varchar(45)  NOT NULL,"
            + " IncomeBudget_Money varchar(45) DEFAULT NULL,"
            + " CONSTRAINT incomebudget_ibfk_1 FOREIGN KEY (User_Name) REFERENCES userinfo (User_Name) ON DELETE CASCADE ON UPDATE CASCADE,"
            + " CONSTRAINT incomebudget_ibfk_2 FOREIGN KEY (IncomeBudget_Type) REFERENCES incometype (IncomeType_Name) ON DELETE CASCADE ON UPDATE CASCADE"
            + ")";
    public static final String create_expendbudget = "CREATE TABLE expendbudget ("
            + " User_Name varchar(45)  NOT NULL,"
            + " ExpendBudget_Type varchar(45)  NOT NULL,"
            + " ExpendBudget_Money varchar(45) DEFAULT NULL,"
            + " CONSTRAINT expendbudget_ibfk_1 FOREIGN KEY (User_Name) REFERENCES userinfo (User_Name) ON DELETE CASCADE ON UPDATE CASCADE,"
            + " CONSTRAINT expendbudget_ibfk_2 FOREIGN KEY (ExpendBudget_Type) REFERENCES expendtype (ExpendType_Name) ON DELETE CASCADE ON UPDATE CASCADE"
            + ")";

    private Context mContext;

    public SQLiteDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_userinfo);
        db.execSQL(create_incomtype);
        db.execSQL(create_expendtype);
        db.execSQL(create_incomeinfo);
        db.execSQL(create_expendinfo);
        db.execSQL(create_incomebudget);
        db.execSQL(create_expendbudget);
        String[] incometype = {"职业收入", "其他收入"};
        String[] expendtype = {"食品酒水", "居家物业", "衣服饰品", "行车交通", "交流通讯"
                , "休闲娱乐", "学习进修", "人情往来", "医疗保健", "金融保险", "其他支出"};
        ContentValues values = new ContentValues();
        for (int i = 0; i < incometype.length; i++) {
            values.put("IncomeType_Name", incometype[i]);
            db.insert("incometype", null, values);
            values.clear();
        }
        for (int i = 0; i < expendtype.length; i++) {
            values.put("ExpendType_Name", expendtype[i]);
            db.insert("expendtype", null, values);
            values.clear();
        }
        values.put("User_Name","请立即登录");
        values.put("User_Login",0);
        db.insert("userinfo",null,values);
        Log.d("SQLiteDatabasemy", "a");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists userinfo");
        db.execSQL("drop table if exists incometype");
        db.execSQL("drop table if exists expendtype");
        db.execSQL("drop table if exists incomeinfo");
        db.execSQL("drop table if exists expendinfo");
        db.execSQL("drop table if exists incomebudget");
        db.execSQL("drop table if exists expendbudget");
        onCreate(db);
        Log.d("SQLiteDatabasemy", "b");
    }
}
