package com.personalfinance.app.Finance;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.FinanceFragment.HoldProductFragment;
import com.personalfinance.app.Finance.FinanceFragment.LoadFailFragment;
import com.personalfinance.app.Finance.FinanceFragment.NoHaveHoldFragment;
import com.personalfinance.app.R;
import com.personalfinance.app.User.loadDialogUtils;
import com.personalfinance.app.Util.HttpUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FinanceHoldActivity extends AppCompatActivity implements View.OnClickListener{
    private SQLiteDatabase db;
    private Intent intent;
    private Drawable drawable;
    private Dialog mDialog;
    private String UserNumber;
    private HoldProductFragment holdProductFragment;//持有产品显示
    private LoadFailFragment loadFailFragment;//加载失败，没有数据
    private NoHaveHoldFragment noHaveHoldFragment;//没有购买理财产品
    private FragmentTransaction ft;


    private ImageView BackImage;
    private TextView Back,Record;
    private TextView Money, Yesterday_income, Sum_income;


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
                    Log.d("liangjialing","success");
                    Toast.makeText(FinanceHoldActivity.this, "数据加载成功", Toast.LENGTH_SHORT).show();
                    //联网成功，判断是否有持有的理财产品
                    if(ishaveDatabase()){
                        setSelect(0);
                        ShowTop();
                    }
                    else{
                        setSelect(2);//显示未有产品持有
                    }
                    loadDialogUtils.closeDialog(mDialog);
                    //显示产品
                    break;
                case fail:
                    Toast.makeText(FinanceHoldActivity.this, "数据加载失败,连接网络", Toast.LENGTH_SHORT).show();
                    //先判断数据库中是否有数据
                    if (ishaveDatabase()) {
                        setSelect(0);//直接从数据库中显示
                        ShowTop();
                    } else {
                        setSelect(1);
                    }
                    loadDialogUtils.closeDialog(mDialog);
                    //显示加载失败界面
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {//初始化
        super.onCreate(savedInstanceState);
        setContentView(R.layout.financeholdfragment);//持有界面布局
        //先判断用户是否处于登录状态，未登录则不用进行网络请求
        intent = getIntent();
        UserNumber = intent.getStringExtra("User_Number");
        Log.d("liang", "User_Number= "+UserNumber);
        Back=(TextView)findViewById(R.id.financeholdfragment_back);
        BackImage=(ImageView)findViewById(R.id.financeholdfragment_backimageview);
        //drawable = getResources().getDrawable(R.mipmap.zuojiantou);
       // drawable.setBounds(0, 0, 40, 40);
       // Back.setCompoundDrawables(drawable, null, null, null);
        Back.setOnClickListener(this);
        BackImage.setOnClickListener(this);
        Record=(TextView)findViewById(R.id.financeholdfragment_record);
        Record.setOnClickListener(this);
        Money = (TextView) findViewById(R.id.financeholdfragment_money);
        Yesterday_income = (TextView) findViewById(R.id.financeholdfragment_yesterday);
        Sum_income = (TextView) findViewById(R.id.financeholdfragment_sum);
        if(!ishaveFinanceproduct()){
            //请求
            Log.d("liangjialing","理财产品表为空");
            Requestproduct();
        }
        if (UserNumber.equals("")) {
            //处于未登录状态，提醒用户登陆，同时显示
            //setSelect(2);//登录提示
            Toast.makeText(FinanceHoldActivity.this, "请登录", Toast.LENGTH_SHORT).show();
        } else {
            Init();
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.financeholdfragment_backimageview:
            case R.id.financeholdfragment_back://返回键
                finish();
                break;
            case R.id.financeholdfragment_record://交易记录
                Log.d("liangjialing","点击交易记录");
                intent=new Intent(FinanceHoldActivity.this,FinanceRecordActivity.class);
                //传递点信息
                intent.putExtra("User_Number",UserNumber);//用户编号
                intent.putExtra("Product_Number","");
                startActivity(intent);
                break;
            default:break;
        }
    }
    private void Init() {
        if (FinanceProductActivty.isNetWork(this)) {
            Log.d("liangjialing", "网络请求");
            //进行网络请求
            setSelect(0);//进行数据库的显示
           ShowTop();
            Request();
        } else {
            Toast.makeText(FinanceHoldActivity.this,"请连接网络",Toast.LENGTH_SHORT).show();
            //先判断数据库中是否有数据
            if (ishaveDatabase()) {
                Log.d("liangjialing", "数据库中有");
                setSelect(0);//直接从数据库中显示
                ShowTop();
            } else {
                Log.d("liangjialing", "数据库中没有");
                setSelect(1);
            }
        }
    }

    private void ShowTop() {
        //上方汇总显示
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        //查找持有理财产品表进行金额、昨日收益、持有收益汇总
        Double TotalMoney = 0.00;
        Double TotalYesterday = 0.00;
        Double TotalSum = 0.00;
        Cursor cursor = db.query("holdproduct", null, "User_Number=?"
                , new String[]{UserNumber}, null, null, null);
        Log.d("liangjialing","Showtop()");
        if (cursor.moveToFirst()) {
            do {
                TotalMoney = TotalMoney + Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                TotalYesterday = TotalYesterday + Double.valueOf(cursor.getString(cursor.getColumnIndex("Yesterday_income")));
                TotalSum = TotalSum + Double.valueOf(cursor.getString(cursor.getColumnIndex("Sum_income")));
            } while (cursor.moveToNext());
        }
        Money.setText(formatDouble(TotalMoney));
        Yesterday_income.setText(formatDouble(TotalYesterday));
        Sum_income.setText(formatDouble(TotalSum));
    }

    private void setSelect(int i) {

        FragmentManager fm = getSupportFragmentManager();//fragment管理器
        ft = fm.beginTransaction();//开始进行管理
        switch (i) {
            case 0://正常
                Log.d("liangjialing", "setSelect(0)");
                holdProductFragment = new HoldProductFragment();
                Bundle bundle = new Bundle();
                bundle.putString("User_Number", UserNumber);
                holdProductFragment.setArguments(bundle);//数据传递到fragment中
                ft.replace(R.id.financeholdfragment_framelayout, holdProductFragment).commit();
                break;
            case 1://失败
                loadFailFragment = new LoadFailFragment();
                ft.replace(R.id.financeholdfragment_framelayout, loadFailFragment).commit();
                loadFailFragment.setOnfragmentClick(new LoadFailFragment.OnfragmentClick() {
                    @Override
                    public void onClick(View view) {
                        Init();
                    }
                });
                break;
            case 2://没有持有的理财产品
                noHaveHoldFragment =new NoHaveHoldFragment();
                ft.replace(R.id.financeholdfragment_framelayout, noHaveHoldFragment).commit();
                break;
            default:
                break;

        }
    }


    private boolean ishaveDatabase() {//判断数据库中是否有数据
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("holdproduct", null, "User_Number=?",
                new String[]{UserNumber}, null, null, null);
        if (cursor.moveToFirst()) {
            db.close();
            return true;
        }
        db.close();
        return false;
    }

    private void Request() {
        LoadingDialog();//网络请求
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        final JSONObject jsonObject=new JSONObject();
        try{
            jsonObject.put("User_Number",UserNumber);
        }catch(Exception e){
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), mediaType);
        String address = AppNetConfig.FinanceHold;
        Log.d("liangjialing", "request");
        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //  e.printStackTrace();
                NetWork_Code = "500";
                flag = false;
                Log.d("liangjialing", "onFailure");
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
                        Log.d("liangjialing",jsonObject.toString());
                        resultCode = jsonObject.getString("resultCode");
                        if (resultCode.equals("200")) {
                            WriteToDatabase(jsonObject.getJSONArray("data"));
                        } else {
                            Log.d("liang", "shujufanhuishibai");
                        }
                        Log.d("liangjialing","network");
                        NetWork_Code = resultCode;
                        flag = false;
                    } catch (JSONException e) {
                        Log.d("liang", "jsonobject出错");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void DeleteToDatabase() {
        Log.d("liang", "删除持有理财产品表");
        //删除数据库中内容该用户持有的理财产品息
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        db.delete("holdproduct","User_Number=?", new String[]{UserNumber});
        db.close();
        Log.d("liang", "删除持有理财产品表成功");
    }

    private void WriteToDatabase(JSONArray jsonArray) {//将获取的数据写入到数据库中,再写入之前进行数据库中内容的删除
        try {
            DeleteToDatabase();
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            Log.d("liang", "开始插入数据库中");
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d("liang", "for(循环)");

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.d("liang", jsonObject.toString());
                ContentValues values = new ContentValues();
                values.put("User_Number", UserNumber);
              //  Log.d("liangjialing", "0 "+UserNumber);
                values.put("Product_Number", jsonObject.getString("Product_Number"));
               // Log.d("liangjialing", "1 "+jsonObject.getString("Product_Number"));
                values.put("Money", jsonObject.getString("Money"));
               // Log.d("liangjialing", "2 "+jsonObject.getString("Money"));
                values.put("Yesterday_income", jsonObject.getString("Yesterday_income"));
               // Log.d("liangjialing", "3 "+jsonObject.getString("Yesterday_income"));
                values.put("Sum_income", jsonObject.getString("Sum_income"));
               // Log.d("liangjialing", "4 "+jsonObject.getString("Sum_income"));
                values.put("Hold_Quotient", jsonObject.getString("Hold_Quotient"));
               // Log.d("liangjialing", "5 "+jsonObject.getString("Hold_Quotient"));
                values.put("Each_Amount", jsonObject.getString("Each_Amount"));
                //Log.d("liangjialing", "6 "+jsonObject.getString("Each_Amount"));
                db.insert("holdproduct", null, values);
              //  Log.d("liangjialing", "插入数据成功");
            }
            db.close();
        } catch (JSONException e) {
            Log.d("liangjialing", "数据库出现错误");
            e.printStackTrace();
        }
    }





    private boolean ishaveFinanceproduct(){
        //判断产品数据库是否为空，因为产品名称，公司名称和图标都要依据理财产品表
        //如果为空则进行请求，请求完毕后再进行查询
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("financeproduct", null, null,
                null, null, null, null);
        if (cursor.moveToFirst()) {//不为空
            db.close();
            return true;
        }
        db.close();//weikong
        return false;
    }
    private void Requestproduct() {
        LoadingDialog();//网络请求
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.Companion.create("", mediaType);
        String address = AppNetConfig.FinanceProduct;
        Log.d("liangjialing","request");
        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //  e.printStackTrace();
                NetWork_Code = "500";
                flag = false;
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                //成功响应，接收数据
                int isRegister = -1;
                String responseText = response.body().string();
                String resultCode = "500";

                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        Log.d("liangjialing", "!TextUtils financeproduct");
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                        if (resultCode.equals("200")) {
                            WriteFinanceToDatabase(jsonObject.getJSONArray("data"));
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
    private void DeleteTofinanceDatabase(){//删除数据库中内容
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        db.delete("financeproduct",null,null);
        db.close();
    }
    private void WriteFinanceToDatabase(JSONArray jsonArray) {//将获取的数据写入到数据库中
        try {
            DeleteTofinanceDatabase();
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("Product_Number", jsonObject.getString("Product_Number"));
                values.put("Product_Name", jsonObject.getString("Product_Name"));
                values.put("Company", jsonObject.getString("Company"));
                values.put("Yield", jsonObject.getString("Yield"));
                values.put("Each_Amount", jsonObject.getString("Each_Amount"));
                values.put("Purchase_Amount", jsonObject.getString("Purchase_Amount"));
                values.put("Introduct", jsonObject.getString("Introduct"));
                values.put("Picture", Base64.decode(jsonObject.getString("Picture"), Base64.NO_WRAP));
                db.insert("financeproduct", null, values);
            }
            db.close();
        } catch (JSONException e) {
            Log.d("liangjialing", "数据库出现错误");
            e.printStackTrace();
        }
    }

    private void LoadingDialog() {
        mDialog = loadDialogUtils.createLoadingDialog(FinanceHoldActivity.this, "加载中...");
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

    public static String formatDouble(double data) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(data);
        return format;
    }
}
