package com.personalfinance.app.Finance;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.Class.Product;
import com.personalfinance.app.R;
import com.personalfinance.app.Time_Type.CaculatorPop;
import com.personalfinance.app.User.loadDialogUtils;
import com.personalfinance.app.Util.DataFormatUtil;
import com.personalfinance.app.Util.HttpUtil;
import com.personalfinance.app.Util.PictureFormatUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BuyActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;
    private SQLiteDatabase db;
    private Dialog mDialog;
    private String User_Number, Product_Number;

    private Drawable drawable;
    private CaculatorPop mCaculatorPop;
    private ImageView Picture;
    private TextView Product_Name, Back;
    private ImageView Backimage;
    private EditText Money;
    private RelativeLayout Sure;

    private String Purchase_Amount = "";
    private String NetWork_Code = "500";
    private boolean flag = true;

    private final static int success = 1;
    private final static int fail = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case success:
                    Toast.makeText(BuyActivity.this, "买入成功", Toast.LENGTH_SHORT).show();
                    loadDialogUtils.closeDialog(mDialog);
                    break;
                case fail:
                    Toast.makeText(BuyActivity.this, "买入失败", Toast.LENGTH_SHORT).show();
                    loadDialogUtils.closeDialog(mDialog);
                    break;

            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy);//买入界面
        intent = getIntent();
        User_Number = intent.getStringExtra("User_Number");
        Product_Number = intent.getStringExtra("Product_Number");

        Init();
        //从理财产品表中获得数值
        getData();

    }

    private void Init() {
        Back = (TextView) findViewById(R.id.buy_back);
        Backimage=(ImageView)findViewById(R.id.buy_backimageview);
        //drawable = getResources().getDrawable(R.mipmap.zuojiantou);
       // drawable.setBounds(0, 0, 40, 40);
       // Back.setCompoundDrawables(drawable, null, null, null);
        Back.setOnClickListener(this);
        Backimage.setOnClickListener(this);
        Picture = (ImageView) findViewById(R.id.buy_picture);
        Product_Name = (TextView) findViewById(R.id.buy_productname);
        Money = (EditText) findViewById(R.id.buy_money);
        Money.setShowSoftInputOnFocus(false);
        Money.setOnClickListener(this);
        Sure = (RelativeLayout) findViewById(R.id.buy_sure);
        Sure.setOnClickListener(this);
    }

    private void getData() {
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("financeproduct", null, "Product_Number=?",
                new String[]{Product_Number}, null, null, null);
        if (cursor.moveToFirst()) {
            drawable = PictureFormatUtil.Bytes2Drawable(getResources(), cursor.getBlob(cursor.getColumnIndex("Picture")));
            Picture.setImageDrawable(drawable);
            Product_Name.setText(cursor.getString(cursor.getColumnIndex("Product_Name")));
            Purchase_Amount = cursor.getString(cursor.getColumnIndex("Purchase_Amount"));
            Money.setHint("最低买入" + Purchase_Amount + "元");
        }
        db.close();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy_backimageview:
            case R.id.buy_back://返回
                finish();
                break;
            case R.id.buy_money://价格输入
                mCaculatorPop = new CaculatorPop(BuyActivity.this, Sure);
                mCaculatorPop.setOnCaculatorSetListener(new CaculatorPop.OnCaculatorSetListener() {
                    @Override
                    public void OnCaculatorSet(int sort, String date) {
                        if (sort == 1) {
                            Money.setText(date);
                        }
                    }
                });
                break;
            case R.id.buy_sure://进行网络请求
                if (!User_Number.equals("")) {

                    if (Money.getText().toString().equals("")) {
                        Toast.makeText(BuyActivity.this, "输入金额不能为空", Toast.LENGTH_SHORT).show();
                    } else {

                        if (Double.valueOf(Money.getText().toString()) < Double.valueOf(Purchase_Amount)) {
                            Toast.makeText(BuyActivity.this, "购买金额不能低于起购金额！", Toast.LENGTH_SHORT).show();
                        } else {
                            if (FinanceProductActivty.isNetWork(this)) {
                                //有网络
                                Request();
                            } else {
                                //没有网络
                                Toast.makeText(BuyActivity.this, "请连接网络", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(BuyActivity.this, "请登录", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void Request() {
        LoadingDialog();//网络请求
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("User_Number", User_Number);
            jsonObject.put("Product_Number", Product_Number);
            jsonObject.put("Money", Money.getText().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), mediaType);
        String address = AppNetConfig.FinanceBuy;
        Log.d("liangjialing", "request1");
        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                NetWork_Code = "500";
                flag = false;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                //成功响应，接收数据
                int isRegister = -1;
                String responseText = response.body().string();
                //Log.d("liangjialing", responseText);
                String resultCode = "500";

                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        Log.d("liangjialing", "!TextUtils");
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                        if (resultCode.equals("200")) {//
                            //重新加载holdproduct表和recordproduct表
                            Log.d("liangjialing", "200");
                            Log.d("liangjialing", jsonObject.getJSONArray("recordproduct").toString());
                            WriteToRecordproduct(jsonObject.getJSONArray("recordproduct"));
                            Money.setText("");
                        } else {
                            Log.d("liang", "shujufanhuishibai");
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

    private void DeleteRecordproduct() {//删除数据库中内容
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        db.delete("recordproduct", "User_Number=?", new String[]{User_Number});
        db.close();
    }

    private void WriteToRecordproduct(JSONArray jsonArray) {
        try {
            DeleteRecordproduct();
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("User_Number", User_Number);
                values.put("Product_Number", jsonObject.getString("Product_Number"));
                values.put("Order_Number", jsonObject.getString("Order_Number"));
                values.put("Buy_Sale", jsonObject.getString("Buy_Sale"));
                values.put("BMoney_SQuotient", jsonObject.getString("BMoney_SQuotient"));
                values.put("Sure_Status", jsonObject.getString("Sure_Status"));
                values.put("BQuotient_SMoney", jsonObject.getString("BQuotient_SMoney"));
                values.put("Time", jsonObject.getLong("Time"));
                values.put("Sure_Time", jsonObject.getLong("Sure_Time"));
                db.insert("recordproduct", null, values);
                Log.d("liangjialing", "recordproduct插入");
            }
            db.close();
        } catch (JSONException e) {
            Log.d("liangjialing", "数据库出现错误");
            e.printStackTrace();
        }

    }

    private void LoadingDialog() {
        mDialog = loadDialogUtils.createLoadingDialog(BuyActivity.this, "加载中...");
        //开启一个线程进行等待
        new Thread(new Runnable() {
            @Override
            public void run() {
                //开启等待界面
                while (flag) {
                }//等待网络请求的返回信息
                Log.d("liangjialing", "flag= " + flag + " NetWork_Code=" + NetWork_Code);

                flag = true;
                if (NetWork_Code.equals("200")) {
                    handler.sendEmptyMessage(success);
                } else {
                    handler.sendEmptyMessage(fail);
                }
            }
        }).start();
    }
}