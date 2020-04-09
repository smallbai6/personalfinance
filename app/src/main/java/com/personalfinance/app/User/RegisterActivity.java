package com.personalfinance.app.User;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.MainActivity;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.HttpUtil;
import com.personalfinance.app.Util.PictureFormatUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    //final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Intent intent;

    private TextView back, login;
    private Drawable drawable;
    private EditText register_username, register_password, register_surepassword;
    private RelativeLayout register_sure;

    private byte[] Picturebytes;

    private Dialog mDialog;
    private String NetWork_Code = "500";

    private boolean flag = true;//设定线程等待界面时间
    private final static int Register_success = 1;
    private final static int Register_alreadyhave = 2;
    private final static int Register_fail = 3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Register_success:
                    //Log.d("liangjialing", "handler");
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case Register_alreadyhave:
                    // Log.d("liangjialing", "handler");
                    loadDialogUtils.closeDialog(mDialog);
                    //Log.d("TAG1", "handler Register_alreadyhave");
                    Toast.makeText(RegisterActivity.this, "用户名已存在，请重新输入", Toast.LENGTH_SHORT).show();
                    break;
                case Register_fail:
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(RegisterActivity.this, "注册失败,请连接网络", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        //db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        back = (TextView) findViewById(R.id.register_back);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 50, 50);
        back.setCompoundDrawables(drawable, null, null, null);
        back.setCompoundDrawablePadding(10);

        login = (TextView) findViewById(R.id.register_tologin);
        register_username = (EditText) findViewById(R.id.register_username);
        register_password = (EditText) findViewById(R.id.register_password);
        register_surepassword = (EditText) findViewById(R.id.register_surepassword);
        register_sure = (RelativeLayout) findViewById(R.id.register_sure);
        back.setOnClickListener(this);
        login.setOnClickListener(this);
        register_sure.setOnClickListener(this);
        //开始添加信息
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_back://返回
                //退出注册界面
                intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.register_tologin://到登陆界面
                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.register_sure://注册
                if (register_username.getText().toString().isEmpty() || register_password.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (register_surepassword.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!register_surepassword.getText().toString().equals(register_password.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "密码与确认密码不相同，请重新输入", Toast.LENGTH_SHORT).show();
                } else if (register_username.getText().toString().equals("请立即登录")) {
                    Toast.makeText(RegisterActivity.this, "用户名已存在，请重新输入", Toast.LENGTH_SHORT).show();
                } else {
                    getregister();
                }
                break;
        }
    }


    private void getregister() {
        if (register_password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "密码至少需要六位数", Toast.LENGTH_SHORT).show();
        } else {
            LoadingDialog();
            //开始上传验证
            // JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("User_Name", register_username.getText().toString());
                jsonObject.put("User_Password", register_password.getText().toString());
                Picturebytes = PictureFormatUtil.Drawable2Bytes(ContextCompat.getDrawable(this, R.mipmap.defaultheadportrait));
                jsonObject.put("Head_Portrait", Base64.encodeToString(Picturebytes, Base64.NO_WRAP));
                // jsonArray.put(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.d("liangjialing","数据打包好了");
            MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), mediaType);
            String address = AppNetConfig.Register;
            //Log.d("liangjialing","准备请求");
            HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    //Toast.makeText(RegisterActivity.this, "注册失败,请重试", Toast.LENGTH_SHORT).show();
                    //Log.d("liangjialing", "注册失败,请重试连接网络");
                    NetWork_Code = "500";
                    flag = false;
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    //成功响应，接收数据
                    int isRegister = -1;
                    String responseText = response.body().string();
                    Log.d("liangjialing",responseText);
                    String resultCode = "500";
                    if (!TextUtils.isEmpty(responseText)) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseText);
                            resultCode = jsonObject.getString("resultCode");
                            // Log.d("liangjialing",resultCode);
                            if (resultCode.equals("200")) {//成功
                                //插入数据库
                                db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                                ContentValues values = new ContentValues();
                                values.put("User_Name", register_username.getText().toString());
                                values.put("User_Login", 1);
                                values.put("Head_Portrait", Picturebytes);
                                values.put("Time", jsonObject.getLong("Time"));
                                db.insert("userinfo", null, values);
                                db.close();
                            } else if (resultCode.equals("201")) {//存在相同用户名
                            } else if (resultCode.equals("500")) {//注册失败，服务器繁忙

                            }
                            NetWork_Code = resultCode;
                            flag = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }

    }

    public void LoadingDialog() {
        mDialog = loadDialogUtils.createLoadingDialog(RegisterActivity.this, "注册中...");
        //开启一个线程进行等待
        new Thread(new Runnable() {
            @Override
            public void run() {
                //开启等待界面
                while (flag) {
                }//等待网络请求的返回信息
                flag = true;
                if (NetWork_Code.equals("200")) {
                    handler.sendEmptyMessage(Register_success);
                } else if (NetWork_Code.equals("201")) {
                    handler.sendEmptyMessage(Register_alreadyhave);
                } else if (NetWork_Code.equals("500")) {
                    handler.sendEmptyMessage(Register_fail);
                }

            }
        }).start();
    }
}