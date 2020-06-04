package com.personalfinance.app.Finance;

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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.FinanceFragment.LoadFailFragment;
import com.personalfinance.app.Finance.FinanceFragment.NoHaveRecordFragment;
import com.personalfinance.app.Finance.FinanceFragment.RecordFragment;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.loadDialogUtils;
import com.personalfinance.app.Util.HttpUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 理财产品交易记录表,使用fragment方式实现
 */
public class FinanceRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    private Intent intent;
    private Dialog mDialog;
    private Drawable drawable;
    private String UserNumber, ProductNumber, Buy_Sale, Sure_Status;
    private FragmentTransaction ft;
    private RecordFragment recordFragment;
    private LoadFailFragment loadFailFragment;
    private NoHaveRecordFragment noHaveRecordFragment;


    private ImageView Back;
    private RelativeLayout All, Buy, Sale, UnderWay;
    private TextView TextAll, TextBuy, TextSale, TextUnderWay;
    private View ViewAll, ViewBuy, ViewSale, ViewUnderWay;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String NetWork_Code = "500";
    private boolean flag = true;
    private int show = 0;//初始默认按钮


    private int isDialogClose=1;
    private OkHttpClient client;
    private final static int success = 1;
    private final static int fail = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case success:
                    Toast.makeText(FinanceRecordActivity.this, "数据加载成功", Toast.LENGTH_SHORT).show();
                    //setSelect(0);
                    if (ishaveDatabase()) {
                        //有数据
                        setSelect(show);
                    } else {
                        setSelect(4);
                    }
                    Log.d("liangjialing", "handler数据加载成功");
                    isDialogClose=0;
                    loadDialogUtils.closeDialog(mDialog);
                    //显示产品
                    break;
                case fail:
                    Toast.makeText(FinanceRecordActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                    //先判断数据库中是否有数据
                    if (ishaveDatabase()) {
                        setSelect(show);//直接从数据库中显示
                    } else {
                        setSelect(5);
                    }
                    isDialogClose=0;
                    loadDialogUtils.closeDialog(mDialog);
                    Log.d("liangjialing", "handler数据加载失败");

                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {//初始化
        super.onCreate(savedInstanceState);
        setContentView(R.layout.financerecordfragment);//持有界面布局
        intent = getIntent();
        UserNumber = intent.getStringExtra("User_Number");//用户编号
        ProductNumber = intent.getStringExtra("Product_Number");//产品编号
        //Log.d("liangjialing", UserNumber + "   " + ProductNumber);
        Buy_Sale = "";
        Sure_Status = "";
        Back = (ImageView) findViewById(R.id.financerecordfragment_back);
        Back.setOnClickListener(this);

        All = (RelativeLayout) findViewById(R.id.financerecordfragment_all);
        All.setOnClickListener(this);
        TextAll = (TextView) findViewById(R.id.financerecordfragment_textall);
        ViewAll = (View) findViewById(R.id.financerecordfragment_viewall);
        Buy = (RelativeLayout) findViewById(R.id.financerecordfragment_buy);
        Buy.setOnClickListener(this);
        TextBuy = (TextView) findViewById(R.id.financerecordfragment_textbuy);
        ViewBuy = (View) findViewById(R.id.financerecordfragment_viewbuy);
        Sale = (RelativeLayout) findViewById(R.id.financerecordfragment_sale);
        Sale.setOnClickListener(this);
        TextSale = (TextView) findViewById(R.id.financerecordfragment_textsale);
        ViewSale = (View) findViewById(R.id.financerecordfragment_viewsale);
        UnderWay = (RelativeLayout) findViewById(R.id.financerecordfragment_underway);
        UnderWay.setOnClickListener(this);
        TextUnderWay = (TextView) findViewById(R.id.financerecordfragment_textunderway);
        ViewUnderWay = (View) findViewById(R.id.financerecordfragment_viewunderway);

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.financerecordfragment_swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.coloryellow));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //进行刷新
                Log.d("liangjialing","进行刷新");
                if (UserNumber.equals("")) {
                    Toast.makeText(FinanceRecordActivity.this, "请登录", Toast.LENGTH_SHORT).show();
                } else {
                    if (!ishaveFinanceproduct()) {
                        Requestproduct();
                    }

                    Init();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });



        if (UserNumber.equals("")) {
            setSelectColor(show);
            Toast.makeText(FinanceRecordActivity.this, "请登录", Toast.LENGTH_SHORT).show();
        } else {
            if (!ishaveFinanceproduct()) {
                Requestproduct();
            }

            Init();
        }

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.financerecordfragment_back:
                Log.d("liangjialing", "点击返回");
                finish();
                break;
            case R.id.financerecordfragment_all:
                Buy_Sale = "";
                Sure_Status = "";
                show = 0;
                setSelectColor(show);
                if (UserNumber.equals("")) {
                    Toast.makeText(FinanceRecordActivity.this, "请登录", Toast.LENGTH_SHORT).show();
                } else {
                    //Init();
                    InitDatabase();
                }

                break;
            case R.id.financerecordfragment_buy:
                Buy_Sale = "0";
                Sure_Status = "1";
                show = 1;
                setSelectColor(show);
                if (UserNumber.equals("")) {
                    Toast.makeText(FinanceRecordActivity.this, "请登录", Toast.LENGTH_SHORT).show();
                } else {
                    //Init();
                    InitDatabase();
                }

                break;
            case R.id.financerecordfragment_sale:
                Buy_Sale = "1";
                Sure_Status = "1";
                show = 2;
                setSelectColor(show);
                if (UserNumber.equals("")) {
                    Toast.makeText(FinanceRecordActivity.this, "请登录", Toast.LENGTH_SHORT).show();
                } else {
                    //Init();
                    InitDatabase();
                }
                break;
            case R.id.financerecordfragment_underway:
                Buy_Sale = "";
                Sure_Status = "0";
                show = 3;
                setSelectColor(show);
                if (UserNumber.equals("")) {
                    Toast.makeText(FinanceRecordActivity.this, "请登录", Toast.LENGTH_SHORT).show();
                } else {
                    //Init();
                    InitDatabase();
                }
                break;
            default:
                break;
        }
    }


    private void Init() {
        //进行初始化
        if (FinanceProductActivty.isNetWork(this)) {
            //进行网络请求
            setSelect(show);
            setSelectColor(show);
            Request();
        } else {
            InitDatabase();
        }
    }
    private void InitDatabase(){
        if (ishaveDatabase()) {
            setSelect(show);
            setSelectColor(show);
        } else {
            setSelect(4);
        }
    }

    private Bundle Data() {//传递数据
        Bundle bundle = new Bundle();
        bundle.putString("User_Number", UserNumber);
        bundle.putString("Product_Number", ProductNumber);
        bundle.putString("Buy_Sale", Buy_Sale);
        bundle.putString("Sure_Status", Sure_Status);
        return bundle;
    }

    private void setSelectColor(int i) {
        Log.d("liangjialing","setSelectColor");
        switch (i) {
            case 0://全部
                TextAll.setTextColor(getResources().getColor(R.color.coloryellow));
                ViewAll.setBackgroundColor(getResources().getColor(R.color.coloryellow));
                TextBuy.setTextColor(getResources().getColor(R.color.colorblack));
                ViewBuy.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextSale.setTextColor(getResources().getColor(R.color.colorblack));
                ViewSale.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextUnderWay.setTextColor(getResources().getColor(R.color.colorblack));
                ViewUnderWay.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                break;
            case 1://买入
                TextAll.setTextColor(getResources().getColor(R.color.colorblack));
                ViewAll.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextBuy.setTextColor(getResources().getColor(R.color.coloryellow));
                ViewBuy.setBackgroundColor(getResources().getColor(R.color.coloryellow));
                TextSale.setTextColor(getResources().getColor(R.color.colorblack));
                ViewSale.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextUnderWay.setTextColor(getResources().getColor(R.color.colorblack));
                ViewUnderWay.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                break;
            case 2://卖出
                TextAll.setTextColor(getResources().getColor(R.color.colorblack));
                ViewAll.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextBuy.setTextColor(getResources().getColor(R.color.colorblack));
                ViewBuy.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextSale.setTextColor(getResources().getColor(R.color.coloryellow));
                ViewSale.setBackgroundColor(getResources().getColor(R.color.coloryellow));
                TextUnderWay.setTextColor(getResources().getColor(R.color.colorblack));
                ViewUnderWay.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                break;
            case 3://进行中
                TextAll.setTextColor(getResources().getColor(R.color.colorblack));
                ViewAll.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextBuy.setTextColor(getResources().getColor(R.color.colorblack));
                ViewBuy.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextSale.setTextColor(getResources().getColor(R.color.colorblack));
                ViewSale.setBackgroundColor(getResources().getColor(R.color.colorwhite));
                TextUnderWay.setTextColor(getResources().getColor(R.color.coloryellow));
                ViewUnderWay.setBackgroundColor(getResources().getColor(R.color.coloryellow));
                break;
            default:
                break;
        }

    }

    private void setSelect(int i) {
        FragmentManager fm = getSupportFragmentManager();//fragment管理器
        ft = fm.beginTransaction();//开始进行管理

        switch (i) {
            case 0://全部
            case 1://买入
            case 2://卖出
            case 3://进行中
                Log.d("liangjialing", show + "");
                recordFragment = new RecordFragment();
                recordFragment.setArguments(Data());//数据传递到fragment中
                ft.replace(R.id.financerecordfragment_framelayout, recordFragment).commit();
                break;
            case 4://暂无交易记录
                noHaveRecordFragment = new NoHaveRecordFragment();
                ft.replace(R.id.financerecordfragment_framelayout, noHaveRecordFragment).commit();
                break;
            case 5://加载失败
                loadFailFragment = new LoadFailFragment();
                ft.replace(R.id.financerecordfragment_framelayout, loadFailFragment).commit();
                loadFailFragment.setOnfragmentClick(new LoadFailFragment.OnfragmentClick() {
                    @Override
                    public void onClick(View view) {
                        Init();
                    }
                });
                break;
            default:
                break;
        }
    }


    private void Request() {
        LoadingDialog();//网络请求
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("User_Number", UserNumber);
            jsonObject.put("Product_Number", ProductNumber);
            jsonObject.put("Buy_Sale", Buy_Sale);
            jsonObject.put("Sure_Status", Sure_Status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), mediaType);
        String address = AppNetConfig.FinanceRecord;
        Log.d("liangjialing", "request");
        client=HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
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
                        Log.d("liangjialing", "!TextUtilsrrecord");
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                        if (resultCode.equals("200")) {
                            WriteToDatabase(jsonObject.getJSONArray("data"));
                        } else {
                            Log.d("liang", "数据返回失败");
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

    private boolean ishaveDatabase() {
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = null;
        if (ProductNumber.equals("")) {
            if (Buy_Sale.equals("") && Sure_Status.equals("")) {//全部
                cursor = db.query("recordproduct", null, "User_Number=?",
                        new String[]{UserNumber}, null, null, null);
            } else if (Sure_Status.equals("1")) {//买入/卖出
                cursor = db.query("recordproduct", null, "User_Number=? AND Buy_Sale=? AND Sure_Status=?",
                        new String[]{UserNumber, Buy_Sale, Sure_Status}, null, null, null);
            } else if (Sure_Status.equals("0")) {//进行中
                cursor = db.query("recordproduct", null, "User_Number=? AND Sure_Status=?",
                        new String[]{UserNumber, Sure_Status}, null, null, null);
            }

        } else {
            if (Buy_Sale.equals("") && Sure_Status.equals("")) {//全部
                cursor = db.query("recordproduct", null, "User_Number=? AND Product_Number=?",
                        new String[]{UserNumber, ProductNumber}, null, null, null);
            } else if (Sure_Status.equals("1")) {//买入/卖出
                cursor = db.query("recordproduct", null, "User_Number=? AND Product_Number=? AND Buy_Sale=? AND Sure_Status=?",
                        new String[]{UserNumber, ProductNumber, Buy_Sale, Sure_Status}, null, null, null);
            } else if (Sure_Status.equals("0")) {//进行中
                cursor = db.query("recordproduct", null, "User_Number=? AND Product_Number=? AND Sure_Status=?",
                        new String[]{UserNumber, ProductNumber, Sure_Status}, null, null, null);
            }
        }
        if (cursor.moveToFirst()) {
            db.close();
            return true;
        }
        db.close();
        return false;
    }

    private void DeleteToDatabase() {
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        if (ProductNumber.equals("")) {
            //代表进行全部的交易记录查询
            if (Buy_Sale.equals("") && Sure_Status.equals("")) {//全部
                db.delete("recordproduct", "User_Number=?", new String[]{UserNumber});
            } else if (Sure_Status.equals("1")) {//买入/卖出
                db.delete("recordproduct", "User_Number=? AND Buy_Sale=? AND Sure_Status=?"
                        , new String[]{UserNumber, Buy_Sale, Sure_Status});
            } else if (Sure_Status.equals("0")) {//进行中
                db.delete("recordproduct", "User_Number=? AND Sure_Status=?"
                        , new String[]{UserNumber, Sure_Status});
            }
        } else {
            if (Buy_Sale.equals("") && Sure_Status.equals("")) {//全部
                db.delete("recordproduct", "User_Number=? AND Product_Number=?"
                        , new String[]{UserNumber, ProductNumber});
            } else if (Sure_Status.equals("1")) {//买入/卖出
                db.delete("recordproduct", "User_Number=? AND Product_Number=? AND Buy_Sale=? AND Sure_Status=?"
                        , new String[]{UserNumber, ProductNumber, Buy_Sale, Sure_Status});
            } else if (Sure_Status.equals("0")) {//进行中
                db.delete("recordproduct", "User_Number=? AND Product_Number=? AND Sure_Status=?"
                        , new String[]{UserNumber, ProductNumber, Sure_Status});
            }
        }
        db.close();

    }

    private void WriteToDatabase(JSONArray jsonArray) {//将获取的数据写入到数据库中,再写入之前进行数据库中内容的删除
        try {
            Log.d("liangjialing", "DeleteToDatabase");

            DeleteToDatabase();
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            Log.d("liangjialing", "WriteToDatabase");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("User_Number", UserNumber);
                values.put("Product_Number", jsonObject.getString("Product_Number"));
                values.put("Order_Number", jsonObject.getString("Order_Number"));
                values.put("Buy_Sale", jsonObject.getString("Buy_Sale"));
                values.put("BMoney_SQuotient", jsonObject.getString("BMoney_SQuotient"));
                values.put("Sure_Status", jsonObject.getString("Sure_Status"));
                values.put("BQuotient_SMoney", jsonObject.getString("BQuotient_SMoney"));
                values.put("Time", jsonObject.getLong("Time"));
                values.put("Sure_Time", jsonObject.getLong("Sure_Time"));
                db.insert("recordproduct", null, values);
            }
            db.close();
        } catch (JSONException e) {
            Log.d("liangjialing", "数据库出现错误");
            e.printStackTrace();
        }
    }


    private boolean ishaveFinanceproduct() {
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
        Log.d("liangjialing", "request");
        client=HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
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
                        Log.d("liangjialing", "!TextUtils");
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

    private void DeleteTofinanceDatabase() {//删除数据库中内容
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        db.delete("financeproduct", null, null);
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
        mDialog = loadDialogUtils.createLoadingDialog(FinanceRecordActivity.this, "加载中...");
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
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                int i=0;
                for (Call call: client.dispatcher().runningCalls()) {
                    i++;
                    client.dispatcher().cancelAll();
                }
                if(i==0){
                    if(isDialogClose==1){
                        finish();
                    }
                }
                isDialogClose=1;
            }
        });
    }
}
