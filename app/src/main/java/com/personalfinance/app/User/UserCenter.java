package com.personalfinance.app.User;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.CS_Data.Data_ZIP;
import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.MainActivity;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.HttpUtil;
import com.personalfinance.app.Util.PictureFormatUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserCenter extends AppCompatActivity  implements View.OnClickListener{

    SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private String Username;
    private Drawable Userheadportrait;
    private Intent intent;
    private Cursor cursor;

    private TextView userCenter_back, close_account, userCenter_name;
    private RelativeLayout logout, userCenter_headportraitR;
    private ImageView userCenter_headportrait;

    private Dialog mDialog;
    private String NetWork_Code;
    private final static int Close_success = 1;
    private final static int Close_fail = 2;
    private final static int Close_havent = 3;
    private final static int Logout_Datasync = 4;
    private final static int Exit_Activity =5;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Close_success:
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(UserCenter.this, "注销成功", Toast.LENGTH_SHORT).show();
                    intent = new Intent(UserCenter.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case Close_fail:
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(UserCenter.this, "注销失败", Toast.LENGTH_SHORT).show();
                    break;
                case Close_havent:
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(UserCenter.this, "注销失败,不存在该用户", Toast.LENGTH_SHORT).show();
                    break;
                case Logout_Datasync://数据进行同步
                    JSONArray jsonArray = (JSONArray) msg.obj;
                   // Log.d("TAG1", "handler进行数据同步");
                    Data_sync(jsonArray);
                    break;
                case Exit_Activity://退出该活动
                   // Log.d("TAG1", "本地更改登录状态完毕");
                    intent = new Intent(UserCenter.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usercenter);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);

        intent = getIntent();
        Username = intent.getStringExtra("Username");
        Userheadportrait = PictureFormatUtil.Bytes2Drawable(getResources(), intent.getByteArrayExtra("Headportrait"));


        userCenter_back=(TextView)findViewById(R.id.userCenter_back);
        userCenter_name = (TextView) findViewById(R.id.usercenter_username);
        userCenter_headportrait = (ImageView) findViewById(R.id.userCenter_headportrait);
        userCenter_headportraitR = (RelativeLayout) findViewById(R.id.userCenter_headportraitR);
        logout = (RelativeLayout) findViewById(R.id.logout);
        close_account = (TextView) findViewById(R.id.close_account);

        userCenter_name.setText(Username);
        userCenter_headportrait.setImageDrawable(Userheadportrait);
        userCenter_back.setOnClickListener(this);
        userCenter_headportraitR.setOnClickListener(this);
        logout.setOnClickListener(this);
        close_account.setOnClickListener(this);
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.userCenter_back://返回主页面
                intent = new Intent(UserCenter.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.logout://退出登录
                setLogout();
                break;
            case R.id.close_account://注销账号
                //删除用户
                //联网进行用户注销，将服务器用户注销
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("温馨提示");
                builder.setMessage("你确定要注销用户么？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setClose_account();
                    }
                });
                builder.setNegativeButton("手滑了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.userCenter_headportraitR://进行用户头像更改
                //用户头像更改，如果联网则进行同步，反之等备份时同步
                setHead_Portrait();
                break;
        }
    }

    private void setLogout() {
        //退出登录
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put("User_Login", 0);
                db.update("userinfo", values, "User_Name=?", new String[]{Username});
               // Log.d("TAG1", "本地更改登录状态");
            }
        }).start();

         new Thread(new Runnable() {//进行同步操作
            @Override
            public void run() {
                Message message = new Message();
                message.what = Logout_Datasync;
                try {
                    message.obj = Data_ZIP.Data_Sync(db, Username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(message);
            }
        }).start();
    }

    private void Data_sync(JSONArray jsonArray) {
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.Companion.create(jsonArray.toString(), mediaType);
        String address = AppNetConfig.Data_syncCS;

        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d("TAG1", "退出登录,备份失败,没有网络");
                handler.sendEmptyMessage(Exit_Activity);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                //成功响应，接收数据
                int isRegister = -1;
                String responseText = response.body().string();
                String resultCode = "500";
                //byte[] bytesq = null;
                // Log.d("TAG1", "response     :" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        //Log.d("TAG1", "resultCode=a     " + resultCode);
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                       // Log.d("TAG1", "resultCode=b    " + resultCode);
                        if (resultCode.equals("200")) {//退出时备份成功
                            ContentValues values = new ContentValues();
                            values.put("Time", jsonObject.getLong("Time"));
                            db.update("userinfo", values, "User_Name=?",
                                    new String[]{Username});
                        }
                    } catch (JSONException e) {
                        Log.d("TAG1", "出现错误");
                        e.printStackTrace();
                    }
                }else{
                    Log.d("TAG1", "weilong  " + resultCode);
                }
                handler.sendEmptyMessage(Exit_Activity);
            }
        });
    }

    public void setClose_account() {
        LoadingDialog();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("User_Name", Username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), mediaType);
        String address = AppNetConfig.CloseAccount;
        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                // Toast.makeText(UserCenter.this, "请连接网络", Toast.LENGTH_SHORT).show();\
                Log.d("TAG", "注销失败，请连接网络");
                NetWork_Code = "500";
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                //成功响应，接收数据
                int isRegister = -1;
                String responseText = response.body().string();
                String resultCode = "500";
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                        //  Log.d("liangjialing",   resultCode);
                        if (resultCode.equals("200")) {//成功,本地也注销
                            db.delete("userinfo", "User_Name=?", new String[]{Username});
                        }
                        NetWork_Code = resultCode;
                    } catch (JSONException e) {
                        Log.d("liangjialing", "解析出错  ");
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    public void setHead_Portrait() {
    }

    public void LoadingDialog() {
        mDialog = loadDialogUtils.createLoadingDialog(UserCenter.this, "注销中...");
        //开启一个线程进行等待
        new Thread(new Runnable() {
            @Override
            public void run() {
                //开启等待界面
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (NetWork_Code.equals("200")) {
                    handler.sendEmptyMessage(Close_success);
                } else if (NetWork_Code.equals("201")) {
                    handler.sendEmptyMessage(Close_havent);
                } else if (NetWork_Code.equals("500")) {
                    handler.sendEmptyMessage(Close_fail);
                }
            }
        }).start();
    }
}

