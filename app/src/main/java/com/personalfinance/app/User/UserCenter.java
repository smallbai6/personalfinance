package com.personalfinance.app.User;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.MainActivity;
import com.personalfinance.app.R;

public class UserCenter extends AppCompatActivity  implements View.OnClickListener{
    private TextView userCenter_back,logout,close_account,userCenter_name;
    SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    Intent intent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usercenter);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        userCenter_back=(TextView)findViewById(R.id.userCenter_back);
        logout=(TextView)findViewById(R.id.logout);
        close_account=(TextView)findViewById(R.id.close_account);
        userCenter_name=(TextView)findViewById(R.id.usercenter_username);
        Cursor cursor = db.query("userinfo", null, "User_Login=?",
                new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            userCenter_name.setText(cursor.getString(cursor.getColumnIndex("User_Name")));
        }else{//没有登录用户时用户名就为请立即登录
            userCenter_name.setText("请立即登录");
        }
        userCenter_back.setOnClickListener(this);
        logout.setOnClickListener(this);
        close_account.setOnClickListener(this);
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.userCenter_back://返回主页面
                finish();
                break;
            case R.id.logout://退出登录
                ContentValues values=new ContentValues();
                values.put("User_Login",0);
                db.update("userinfo",values,"User_Login=?",new String[]{"1"});
                intent = new Intent(UserCenter.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.close_account://注销账号
                //删除用户
                db.delete("userinfo","User_Login=?",new String[]{"1"});
                intent = new Intent(UserCenter.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}

