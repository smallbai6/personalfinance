package com.personalfinance.app.User;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.CS_Data.Data_ZIP;
import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.MainActivity;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.HttpUtil;

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


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Intent intent;
    private Cursor cursor;

    private TextView back, register;
    private EditText login_username, login_password;
    private RelativeLayout login_sure;

    private Dialog mDialog;
    private String NetWork_Code;

    private final static int Login_success = 1;
    private final static int Login_haveerror = 2;
    private final static int Login_fail = 3;
    private final static int NetWork = 4;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Login_success:
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case Login_haveerror:
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(LoginActivity.this, "用户名或密码输入错误", Toast.LENGTH_SHORT).show();
                    break;
                case Login_fail:
                    loadDialogUtils.closeDialog(mDialog);
                    Toast.makeText(LoginActivity.this, "登录失败，请连接网络", Toast.LENGTH_SHORT).show();
                    break;
                case NetWork://进行登录请求操作
                    JSONArray jsonArray = (JSONArray) msg.obj;
                    Network_Login(jsonArray);
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        back = (TextView) findViewById(R.id.login_back);
        register = (TextView) findViewById(R.id.login_toregister);
        login_username = (EditText) findViewById(R.id.login_username);
        login_password = (EditText) findViewById(R.id.login_password);
        login_sure = (RelativeLayout) findViewById(R.id.login_sure);
        back.setOnClickListener(this);
        register.setOnClickListener(this);
        login_sure.setOnClickListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_back://退出登录界面
                intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.login_toregister://进入注册界面
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.login_sure://点击进行登录操作
                if (login_username.getText().toString().isEmpty() || login_password.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请填写用户名或密码", Toast.LENGTH_SHORT).show();
                } else {
                    getlogin();
                }
                break;
        }
    }

    private void getlogin() {
        //开始上传验证
        LoadingDialog();
        //判断本地是否有该用户
        new Thread(new Runnable() {
            @Override
            public void run() {
                cursor = db.query("userinfo", null, "User_Name=?",
                        new String[]{login_username.getText().toString()}, null, null, null);
                JSONArray jsonArray = new JSONArray();
                if (cursor.moveToFirst()) {//有该用户，打包所有数据
                    //加一个标志位，判断是否有该用户 DataSync_status 1:需要打包数据 2：只需要验证用户
                    try {
                        jsonArray = Data_ZIP.Login_SendData(db, login_username.getText().toString(),
                                login_password.getText().toString(), 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {//没有该用户,打包用户名+密码
                    // JSONArray jsonArray=new JSONArray();
                    JSONArray user = new JSONArray();
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("DataSync_status", 2);
                        jsonObject.put("User_Name", login_username.getText().toString());
                        jsonObject.put("User_Password", login_password.getText().toString());
                        user.put(jsonObject);
                        jsonArray.put(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
                Message message = new Message();
                message.what = NetWork;
                message.obj = jsonArray;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void Network_Login(JSONArray jsonArray) {
        //进行登录请求
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.Companion.create(jsonArray.toString(), mediaType);
        String address = AppNetConfig.Login;
        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d("TAG1", "登录失败,请重试连接网络");
                NetWork_Code = "500";
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                // Log.d("TAG1", isMainThread() + "");
                //成功响应，接收数据
                int isRegister = -1;
                String resultCode = "500";
                String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText)) {
                    //  Log.d("TAG1","回调有值");
                    try {//接收到服务器返回的json格式数据，进行解析
                        JSONArray jsonArray = new JSONArray(responseText);
                        JSONArray jsonArray1 = jsonArray.getJSONArray(0);
                        JSONObject jsonObject = jsonArray1.getJSONObject(0);
                        resultCode = jsonObject.getString("resultCode");
                        //首先判断成功与否
                        if (resultCode.equals("200")) {//成功
                            //判断状态码
                            int status = jsonObject.getInt("status");
                            if (status == 1) {//返回数据包
                                Data_ZIP.Login_GetData(db, login_username.getText().toString(), jsonArray.getJSONArray(1));
                            } else if (status == 2) {//返回时时间戳  userinfo中时间更改
                                ContentValues values = new ContentValues();
                                values.put("User_Login", 1);
                                values.put("Time", jsonObject.getLong("Time"));
                                db.update("userinfo", values, "User_Name=?",
                                        new String[]{login_username.getText().toString()});
                            }
                        }/*else  if(resultCode.equals("201")){//错误

                        }else  if(resultCode.equals("500")){//失败

                        }*/
                        NetWork_Code = resultCode;
                    } catch (JSONException e) {
                        Log.d("liangjialing", "解析出错  ");
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    public void LoadingDialog() {
        mDialog = loadDialogUtils.createLoadingDialog(LoginActivity.this, "加载中...");
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
                    handler.sendEmptyMessage(Login_success);
                } else if (NetWork_Code.equals("201")) {
                    handler.sendEmptyMessage(Login_haveerror);
                } else if (NetWork_Code.equals("500")) {
                    handler.sendEmptyMessage(Login_fail);
                }
            }
        }).start();
    }

    public boolean isMainThread() {
        Log.d("TAG1", Looper.getMainLooper().getThread().getId() + "    " + Thread.currentThread().getId());
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }


}
