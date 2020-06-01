package com.personalfinance.app.Finance;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
import com.personalfinance.app.Finance.FinanceFragment.LoadFailFragment;
import com.personalfinance.app.Finance.FinanceFragment.ProductFragment;
import com.personalfinance.app.R;
import com.personalfinance.app.User.loadDialogUtils;
import com.personalfinance.app.Util.HttpUtil;

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

/**
 * 理财产品界面
 */
public class FinanceProductActivty extends AppCompatActivity implements View.OnClickListener{
    private SQLiteDatabase db;
    private ProductFragment productFragment;//=new ProductFragment();//理财产品显示
    private LoadFailFragment loadFailFragment;//= new LoadFailFragment();//加载失败
    private Dialog mDialog;
    private FragmentTransaction ft;
    private String NetWork_Code = "500";
    private boolean flag = true;

    private Drawable drawable;
    private Intent intent;
    private ImageView backimage;
    private TextView back,have;
    private String UserNumber;

    private final static int success = 1;
    private final static int fail = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case success:
                    Toast.makeText(FinanceProductActivty.this,"数据加载成功",Toast.LENGTH_SHORT).show();
                    setSelect(0);
                    Log.d("liangjialing", "handler数据加载成功");
                    loadDialogUtils.closeDialog(mDialog);
                    //显示产品

                      break;
                case fail:
                    Toast.makeText(FinanceProductActivty.this,"数据加载失败",Toast.LENGTH_SHORT).show();
                    //先判断数据库中是否有数据
                    if (ishaveDatabase()) {
                        //Log.d("liangjialing", "数据库中有");
                        setSelect(0);//直接从数据库中显示
                    } else {
                      //  Log.d("liangjialing", "数据库中没有");
                        setSelect(1);
                    }
                   // setSelect(1);
                    // Log.d("liangjialing", "handler数据加载失败");
                    loadDialogUtils.closeDialog(mDialog);
                    //显示加载失败界面
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {//初始化
        super.onCreate(savedInstanceState);
        setContentView(R.layout.financeproductfragment);//理财产品界面的 FrameLayout布局
        intent=getIntent();
        UserNumber=intent.getStringExtra("UserNumber");
        back=(TextView)findViewById(R.id.financeproductfragment_back);
        backimage=(ImageView)findViewById(R.id.financeproductfragment_backimageview) ;
        //drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        //drawable.setBounds(0, 0, 40, 40);
       // back.setCompoundDrawables(drawable, null, null, null);
        back.setOnClickListener(this);
        backimage.setOnClickListener(this);
        have=(TextView)findViewById(R.id.financeproductfragment_hold);
        have.setOnClickListener(this);
        //首先判断是否联网
        Init();
    }

    private void Init() {
        if (isNetWork(this)) {
            Log.d("liangjialing", "网络请求");
            //进行网络请求
             setSelect(0);
            Requestproduct();
        } else {
            //先判断数据库中是否有数据
            Toast.makeText(FinanceProductActivty.this,"请连接网络",Toast.LENGTH_SHORT).show();

            if (ishaveDatabase()) {
                Log.d("liangjialing", "数据库中有");
                setSelect(0);//直接从数据库中显示
            } else {

                Log.d("liangjialing", "数据库中没有");
                setSelect(1);
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.financeproductfragment_backimageview:
            case R.id.financeproductfragment_back://退出界面
               // Log.d("liangjialing","back");
                finish();
                break;
            case R.id.financeproductfragment_hold://持有界面的进入按钮
                //进行页面跳转
                Log.d("liangjialing","点击持有按键 "+UserNumber);
                Intent intent=new Intent(FinanceProductActivty.this,FinanceHoldActivity.class);
                intent.putExtra("User_Number",UserNumber);
                startActivity(intent);
                break;
        }
    }

    private void setSelect(int i) {

        FragmentManager fm = getSupportFragmentManager();//fragment管理器
        ft = fm.beginTransaction();//开始进行管理
       // hideFragment();
        switch (i) {
            case 0://正常
                productFragment = new ProductFragment();
                Bundle bundle = new Bundle();
                bundle.putString("UserNumber",UserNumber);
                productFragment.setArguments(bundle);//数据传递到fragment中

                ft.replace(R.id.financeproductfragment_framelayout,productFragment).commit();
                break;
            case 1://失败
                loadFailFragment = new LoadFailFragment();
                ft.replace(R.id.financeproductfragment_framelayout,loadFailFragment).commit();
                loadFailFragment.setOnfragmentClick(new LoadFailFragment.OnfragmentClick() {
                    @Override
                    public void onClick(View view) {
                        Log.d("liangjialing","a");
                        Init();
                    }
                });
                break;
            default:
                break;

        }
    }


    public static boolean isNetWork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (null == network) {
            Log.d("TAGa", "network==null");
            return false;
        }
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (null == capabilities) {
            Log.d("TAGa", "capaablities==null");
            return false;
        }
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private boolean ishaveDatabase() {
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("financeproduct", null, null,
                null, null, null, null);
        if (cursor.moveToFirst()) {
            db.close();
            return true;
        }
        db.close();
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
                        Log.d("liangjialing", "!TextUtilsa");
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                        if (resultCode.equals("200")) {
                            WriteToDatabase(jsonObject.getJSONArray("data"));
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

    private void DeleteToDatabase(){//删除数据库中内容
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        db.delete("financeproduct",null,null);
        db.close();
    }
    private void WriteToDatabase(JSONArray jsonArray) {//将获取的数据写入到数据库中
        try {
            DeleteToDatabase();
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
        mDialog = loadDialogUtils.createLoadingDialog(FinanceProductActivty.this, "加载中...");
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


