package com.personalfinance.app.User;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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



public class RegisterLogin extends AppCompatActivity implements View.OnClickListener {
    private Button login, register;
    private EditText rlname, rlpassword;
    private ImageView imageView;
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    Drawable drawable = null;
    private byte[] Picturebytes;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_login);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        login = (Button) findViewById(R.id.rl_login);
        register = (Button) findViewById(R.id.rl_register);
        rlname = (EditText) findViewById(R.id.rl_username);
        rlpassword = (EditText) findViewById(R.id.rl_password);
        imageView = (ImageView) findViewById(R.id.imageView);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        //开始添加信息
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
                    Toast.makeText(RegisterLogin.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
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
        if (rlpassword.length() < 6) {
            Toast.makeText(RegisterLogin.this, "密码至少需要六位数", Toast.LENGTH_SHORT).show();
        } else {
            //开始上传验证
            JSONArray jsonArray = new JSONArray();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("User_Name", rlname.getText().toString());
                jsonObject.put("User_Password", rlpassword.getText().toString());
                Picturebytes = PictureFormatUtil.Drawable2Bytes(ContextCompat.getDrawable(this, R.mipmap.backgroundpicture));
                jsonObject.put("Head_Portrait", Base64.encodeToString(Picturebytes, Base64.NO_WRAP));
                //Log.d("liangjialing",Base64.encodeToString(bytes,Base64.NO_WRAP));
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.Companion.create(jsonArray.toString(), mediaType);
            String address = AppNetConfig.Register;
            HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    Toast.makeText(RegisterLogin.this, "注册验证失败(服务器不能正确接收)", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    //成功响应，接收数据
                    int isRegister = -1;
                    String responseText = response.body().string();
                    byte[] bytesq = null;
                    Log.d("liangjialing", "response     :" + responseText);
                    if (!TextUtils.isEmpty(responseText)) {
                        try {//接收到服务器返回的json格式数据，进行解析
                            JSONArray jsonArray1 = new JSONArray(responseText);
                            JSONObject jsonObject = jsonArray1.getJSONObject(0);
                            String resultCode=jsonObject.getString("resultCode");
                            if(resultCode=="201"){//成功
                                //插入数据库
                                ContentValues values = new ContentValues();
                                values.put("User_Name", rlname.getText().toString());
                                values.put("User_Login", 1);
                                values.put("Head_Portrait", Picturebytes);
                                db.insert("userinfo", null, values);
                            }else if(resultCode=="202")//存在相同用户名或失败
                            {
                                //线程表示存在相同用户名或注册失败
                            }
                        } catch (JSONException e) {
                            Log.d("liangjialing", "解析出错  ");
                            e.printStackTrace();
                        }
                    }
                }
            });

        }

    }
}