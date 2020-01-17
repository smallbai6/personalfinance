package com.personalfinance.app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterLogin extends AppCompatActivity implements View.OnClickListener {
    private Button login, register;
    private EditText rlname, rlpassword;
    SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_login);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        login = (Button) findViewById(R.id.rl_login);
        register = (Button) findViewById(R.id.rl_register);
        rlname = (EditText) findViewById(R.id.rl_username);
        rlpassword = (EditText) findViewById(R.id.rl_password);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_login://登录
                if (rlname.getText().toString().isEmpty() || rlpassword.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterLogin.this, "请填写用户名或密码", Toast.LENGTH_SHORT).show();
                } else {
                    getlogin();
                }
                break;
            case R.id.rl_register://注册
                if (rlname.getText().toString().isEmpty() || rlpassword.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterLogin.this, "请填写用户名或密码", Toast.LENGTH_SHORT).show();
                } else {
                    getregister();
                }
                break;
        }
    }

    private void getlogin() {
        //如果是登录,先检验是否存在用户名，再检验密码是否正确
        Cursor cursor = db.query("userinfo", null, "User_Name=?", new String[]{rlname.getText().toString()}, null, null, null);
        if (cursor.moveToFirst()) {
            if (rlpassword.getText().toString().equals(cursor.getString(cursor.getColumnIndex("User_Password")))) {
                //允许登录，更改用户名 所有行为依照用户名来行动
                //返回数据到mainactivity中
                ContentValues values = new ContentValues();
                values.put("User_Login", 1);
                db.update("userinfo", values, "User_Name=?", new String[]{rlname.getText().toString()});
                Intent intent = new Intent(RegisterLogin.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterLogin.this, "该用户密码错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(RegisterLogin.this, "不存在该用户名", Toast.LENGTH_SHORT).show();
        }
    }

    private void getregister() {
        //进行注册,将用户名和密码插入到数据库中
        //顺便直接登录
        Cursor cursor = db.query("userinfo", null, "User_Name=?", new String[]{rlname.getText().toString()}, null, null, null);
        if (cursor.moveToFirst()) {
            Toast.makeText(RegisterLogin.this, "该用户名已存在", Toast.LENGTH_SHORT).show();
        } else {
            //判断密码是否为六位数
            if (rlpassword.length() < 6) {
                Toast.makeText(RegisterLogin.this, "密码至少需要六位数", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put("User_Name", rlname.getText().toString());
                values.put("User_Password", rlpassword.getText().toString());
                values.put("User_Login", 1);
                db.insert("userinfo", null, values);
                Toast.makeText(RegisterLogin.this, "注册成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterLogin.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

    }
}