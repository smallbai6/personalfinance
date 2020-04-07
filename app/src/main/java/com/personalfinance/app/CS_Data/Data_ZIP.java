package com.personalfinance.app.CS_Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Data_ZIP {

    public static JSONArray Data_Sync(SQLiteDatabase db, String Username) throws JSONException {//信息打包，进行同步
        JSONArray jsonArray = new JSONArray();
        Cursor cursor;
        //---------------userinfo--------------------------------
        cursor = db.query("userinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        JSONArray userinfo=new JSONArray();
        if (cursor.moveToFirst()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("User_Name", Username);
            byte[] bytes = cursor.getBlob(cursor.getColumnIndex("Head_Portrait"));
            jsonObject.put("Head_Portrait", Base64.encodeToString(bytes, Base64.NO_WRAP));
            jsonObject.put("Time",cursor.getLong(cursor.getColumnIndex("Time")));
            userinfo.put(jsonObject);
        }
        jsonArray.put(userinfo);
        jsonArray.put(Data_incomeinfo(db,Username));
        jsonArray.put(Data_expendinfo(db,Username));
        jsonArray.put(Data_incomebudget(db,Username));
        jsonArray.put(Data_expendbudget(db,Username));
        cursor.close();
        return jsonArray;
    }
    public static JSONArray Login_SendData(SQLiteDatabase db,String Username,String Password,int DataSync_status) throws JSONException {//信息打包，进行同步
        JSONArray jsonArray = new JSONArray();
        Cursor cursor;
        //---------------userinfo--------------------------------
        cursor = db.query("userinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        JSONArray userinfo=new JSONArray();
        if (cursor.moveToFirst()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("DataSync_status",DataSync_status);
            jsonObject.put("User_Name", Username);
            jsonObject.put("User_Password",Password);
            byte[] bytes = cursor.getBlob(cursor.getColumnIndex("Head_Portrait"));
            jsonObject.put("Head_Portrait", Base64.encodeToString(bytes, Base64.NO_WRAP));
            jsonObject.put("Time",cursor.getLong(cursor.getColumnIndex("Time")));
            //    jsonArray.put(jsonObject);
            userinfo.put(jsonObject);
        }
        jsonArray.put(userinfo);
        jsonArray.put(Data_incomeinfo(db,Username));
        jsonArray.put(Data_expendinfo(db,Username));
        jsonArray.put(Data_incomebudget(db,Username));
        jsonArray.put(Data_expendbudget(db,Username));
        cursor.close();
        return jsonArray;
    }

    public static JSONArray Data_incomeinfo(SQLiteDatabase db,String Username) throws JSONException {
        Cursor cursor = db.query("incomeinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        JSONArray incomeinfo = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("User_Name", Username);
                jsonObject.put("Money", cursor.getString(cursor.getColumnIndex("Money")));
                jsonObject.put("Type", cursor.getString(cursor.getColumnIndex("Type")));
                jsonObject.put("Time", cursor.getLong(cursor.getColumnIndex("Time")));
                jsonObject.put("Message", cursor.getString(cursor.getColumnIndex("Message")));
                incomeinfo.put(jsonObject);
            } while (cursor.moveToNext());
        }
        return incomeinfo;
    }
    public static JSONArray Data_expendinfo(SQLiteDatabase db,String Username) throws JSONException {
        Cursor cursor = db.query("expendinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        JSONArray expendinfo = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("User_Name", Username);
                jsonObject.put("Money", cursor.getString(cursor.getColumnIndex("Money")));
                jsonObject.put("Type", cursor.getString(cursor.getColumnIndex("Type")));
                jsonObject.put("Time", cursor.getLong(cursor.getColumnIndex("Time")));
                jsonObject.put("Message", cursor.getString(cursor.getColumnIndex("Message")));
                expendinfo.put(jsonObject);
            } while (cursor.moveToNext());
        }
        return expendinfo;
    }
    public static JSONArray Data_incomebudget(SQLiteDatabase db,String Username) throws JSONException {
       Cursor  cursor = db.query("incomebudget", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        JSONArray incomebudget = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("User_Name", Username);
                jsonObject.put("Type", cursor.getString(cursor.getColumnIndex("Type")));
                jsonObject.put("Money", cursor.getString(cursor.getColumnIndex("Money")));
                jsonObject.put("DMSY", cursor.getString(cursor.getColumnIndex("DMSY")));
                jsonObject.put("Time", cursor.getLong(cursor.getColumnIndex("Time")));
                incomebudget.put(jsonObject);
            } while (cursor.moveToNext());
        }
        return incomebudget;
    }
    public static JSONArray Data_expendbudget(SQLiteDatabase db,String Username) throws JSONException {
        Cursor  cursor = db.query("expendbudget", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        JSONArray expendbudget = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("User_Name", Username);
                jsonObject.put("Type", cursor.getString(cursor.getColumnIndex("Type")));
                jsonObject.put("Money", cursor.getString(cursor.getColumnIndex("Money")));
                jsonObject.put("DMSY", cursor.getString(cursor.getColumnIndex("DMSY")));
                jsonObject.put("Time", cursor.getLong(cursor.getColumnIndex("Time")));
                expendbudget.put(jsonObject);
            } while (cursor.moveToNext());
        }
        return expendbudget;
    }

    public static void Login_GetData(SQLiteDatabase db,String Username,JSONArray jsonArray) throws JSONException {
        //信息打包，进行同步
        //首先判断有无该用户

        Cursor cursor;
        //---------------userinfo--------------------------------
        cursor = db.query("userinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        if(cursor.moveToFirst()){//本地有  进行删除
            db.delete("userinfo","User_Name=?",new String[]{Username});
        }
        //进行数据插入
        //---------------userinfo--------------------------------
        JSONArray jsonArray1=jsonArray.getJSONArray(0);
        JSONObject jsonObject=jsonArray1.getJSONObject(0);
        ContentValues values=new ContentValues();

        values.put("User_Name", jsonObject.getString("User_Name"));
        //图片解码
        values.put("Head_Portrait", Base64.decode(jsonObject.getString("Head_Portrait"),Base64.NO_WRAP));
        values.put("Time", jsonObject.getLong("Time"));
        values.put("User_Login", 1);
        db.insert("userinfo", null, values);
        Log.d("TAG",jsonObject.getString("User_Name")+"   "
                        +Base64.decode(jsonObject.getString("Head_Portrait"),Base64.NO_WRAP)+"    "
                +jsonObject.getLong("Time"));
        //---------------incomeinfo-------------------------------------------------
        jsonArray1=jsonArray.getJSONArray(1);
        for(int i=0;i<jsonArray1.length();i++){
            JSONObject jsonObject1=jsonArray1.getJSONObject(i);
            ContentValues values1=new ContentValues();
            values1.put("User_Name", jsonObject1.getString("User_Name"));
            values1.put("Money", jsonObject1.getString("Money"));
            values1.put("Type", jsonObject1.getString("Type"));
            values1.put("Time", jsonObject1.getLong("Time"));
            values1.put("Message", jsonObject1.getString("Message"));
            db.insert("incomeinfo", null, values1);
            Log.d("TAG",jsonObject.getString("User_Name")+"   "
                    +jsonObject1.getString("Money")+"    "
                    +jsonObject1.getString("Type")+"     "
            +jsonObject1.getLong("Time"));
        }
       //---------------expendinfo-------------------------------------------------
        jsonArray1=jsonArray.getJSONArray(2);
        for(int i=0;i<jsonArray1.length();i++){
            JSONObject jsonObject1=jsonArray1.getJSONObject(i);
            ContentValues values1=new ContentValues();
            values1.put("User_Name", jsonObject1.getString("User_Name"));
            values1.put("Money", jsonObject1.getString("Money"));
            values1.put("Type", jsonObject1.getString("Type"));
            values1.put("Time", jsonObject1.getLong("Time"));
            values1.put("Message", jsonObject1.getString("Message"));
            db.insert("expendinfo", null, values1);
            Log.d("TAG",jsonObject.getString("User_Name")+"   "
                    +jsonObject1.getString("Money")+"    "
                    +jsonObject1.getString("Type")+"     "
                    +jsonObject1.getLong("Time"));
        }
        //---------------incomebudget-------------------------------------------------
        jsonArray1=jsonArray.getJSONArray(3);
        for(int i=0;i<jsonArray1.length();i++){
            JSONObject jsonObject1=jsonArray1.getJSONObject(i);
            ContentValues values1=new ContentValues();
            values1.put("User_Name", jsonObject1.getString("User_Name"));
            values1.put("Type", jsonObject1.getString("Type"));
            values1.put("Money", jsonObject1.getString("Money"));
            values1.put("DMSY", jsonObject1.getString("DMSY"));
            values1.put("Time", jsonObject1.getLong("Time"));
            db.insert("incomebudget", null, values1);
        }
       //---------------expendbudget-------------------------------------------------
        jsonArray1=jsonArray.getJSONArray(4);
        for(int i=0;i<jsonArray1.length();i++){
            JSONObject jsonObject1=jsonArray1.getJSONObject(i);
            ContentValues values1=new ContentValues();
            values1.put("User_Name", jsonObject1.getString("User_Name"));
            values1.put("Type", jsonObject1.getString("Type"));
            values1.put("Money", jsonObject1.getString("Money"));
            values1.put("DMSY", jsonObject1.getString("DMSY"));
            values1.put("Time", jsonObject1.getLong("Time"));
            db.insert("expendbudget", null, values1);
        }
        cursor.close();
    }
}