package com.personalfinance.app;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.personalfinance.app.CS_Data.Data_ZIP;
import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.FinanceProductActivty;
import com.personalfinance.app.Main.MainListAdapter;
import com.personalfinance.app.Main.MainListClass;
import com.personalfinance.app.Sqlite.SQLiteDatabaseHelper;
import com.personalfinance.app.User.LoginActivity;
import com.personalfinance.app.User.UserCenter;
import com.personalfinance.app.Util.HttpUtil;
import com.personalfinance.app.Util.PictureFormatUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /*
     *数据库建立
     */
    private SQLiteDatabaseHelper dbHelper;
    //final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private SQLiteDatabase db;
    private Cursor cursor;
    /*
     *用户名和用户头像
     */
    private String Username,UserNumber;//用户名和用户编号
    private Drawable Userheadportrait;
    /*
     *drawerlayout
     */
    private DrawerLayout mDrawerLayout;
    private View userheaderView;
    private TextView draweruserrname;
    private CircleImageView drawerheadportrait;
    private ImageView SyncData;

    /*
    按键
     */
    private RelativeLayout main_opendrawer;
    private ImageView main_opendraweriv;

    private TextView tallybutton, detailbutton, budgetbutton, statisticalbutton,financebutton;
    private Drawable drawable;
    private Intent intent;
    /*
    本日月季年
     */
    private String[] ysmd_name = new String[]{"本日", "本月", "本季", "本年"};//0,1,2,3
    private ListView ysmd_listview;
    private List<MainListClass> ysmd_list = new ArrayList<>();
    private ArrayAdapter<MainListClass> ysmd_adapter;
    /*
    操作UI返回主线程
     */
    private final static int Set_YSMD = 1;
    private final static int ysmd_listview_click = 2;
    private final static int Sync=3;
    private final static int failed=4;
    private final static int success=5;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Set_YSMD:
                    ysmd_adapter.notifyDataSetChanged();
                    break;
                case ysmd_listview_click:
                    int position = msg.getData().getInt("position");
                    long[] time = msg.getData().getLongArray("time");
                    intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("Username", Username);
                    intent.putExtra("start_time", time[0]);
                    intent.putExtra("end_time", time[1]);
                    intent.putExtra("timetype", position);
                    startActivityForResult(intent, 1);//返回之后进行列表的更新
                    break;
                case Sync:
                    db.close();
                    JSONArray jsonArray = (JSONArray) msg.obj;
                    Log.d("TAG1", "进行数据同步:  "+jsonArray.toString());
                    Data_sync(jsonArray);
                  break;
                case success:
                    Toast.makeText(MainActivity.this,"数据同步成功",Toast.LENGTH_SHORT).show();
                    break;
                case failed:
                    Log.d("TAG1","备份失败，请检查网络");
                    Toast.makeText(MainActivity.this,"备份失败，请检查网络",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new SQLiteDatabaseHelper(this, "personal.db", null, 1);
        db = dbHelper.getWritableDatabase();
        //用户名和头像获取
        Get_User();
        Init_Drawerlayout();
        Init_MainButton();
        Init_YSMD();
        Set_YSMD();
    }

    private void Get_User() {
        try {
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            cursor = db.query("userinfo", null, "User_Login=?",
                    new String[]{"1"}, null, null, null);
            if (cursor.moveToFirst()) {

                Username = cursor.getString(cursor.getColumnIndex("User_Name"));
                UserNumber=cursor.getString(cursor.getColumnIndex("User_Number"));
                byte[] blob = cursor.getBlob(cursor.getColumnIndexOrThrow("Head_Portrait"));
                Userheadportrait = PictureFormatUtil.Bytes2Drawable(getResources(), blob);
            } else {

                Username="请立即登录";
                UserNumber="";
                Userheadportrait = ContextCompat.getDrawable(this,R.mipmap.defaultheadportrait);
            }
        } catch (Exception e) {
        } finally {
            cursor.close();
            db.close();
        }

    }
    /*
    初始化DrawerLayout相关的控件
     */
    private void Init_Drawerlayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);//侧滑菜单
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        userheaderView = navView.getHeaderView(0);
        draweruserrname = (TextView) userheaderView.findViewById(R.id.drawer_username);//用户名
        draweruserrname.setOnClickListener(this);
        drawerheadportrait=(CircleImageView)userheaderView.findViewById(R.id.drawer_headportrait);//用户头像
        drawerheadportrait.setOnClickListener(this);
        SyncData=(ImageView)userheaderView.findViewById(R.id.drawer_SyncData);//同步数据
        SyncData.setOnClickListener(this);
        //用户名已获得
        draweruserrname.setText(Username);
        drawerheadportrait.setImageDrawable(Userheadportrait);
        Log.d("TAG", "Init_Drawerla");
        Toast.makeText(MainActivity.this, "用户名为" + Username, Toast.LENGTH_SHORT).show();

    }

    /*
    初始化主界面中的按键
     */
    private void Init_MainButton() {
        main_opendrawer = (RelativeLayout) findViewById(R.id.main_opendrawer);
        main_opendraweriv=(ImageView)findViewById(R.id.main_opendraweriv);
        main_opendraweriv.setImageDrawable(Userheadportrait);
        main_opendrawer.setOnClickListener(this);

        tallybutton = (Button) findViewById(R.id.maintally_button);//记账
        tallybutton.setOnClickListener(this);

        detailbutton = (TextView) findViewById(R.id.maindetail_button);//流水
        drawable = getResources().getDrawable(R.mipmap.liushuitubiao);
        drawable.setBounds(0, 0, 75, 75);
        detailbutton.setCompoundDrawables(null, drawable, null, null);
        detailbutton.setOnClickListener(this);

        budgetbutton = (TextView) findViewById(R.id.mainbudget_button);//预算
        drawable = getResources().getDrawable(R.mipmap.yusuantubiao);
        drawable.setBounds(0, 0, 75, 75);
        budgetbutton.setCompoundDrawables(null, drawable, null, null);
        budgetbutton.setOnClickListener(this);

        statisticalbutton = (TextView) findViewById(R.id.mainstatistical_button);//统计
        drawable = getResources().getDrawable(R.mipmap.tongjitubiao);
        drawable.setBounds(0, 0, 75, 75);
        statisticalbutton.setCompoundDrawables(null, drawable, null, null);
        statisticalbutton.setOnClickListener(this);


        financebutton=(TextView)findViewById(R.id.mainfinance_button);//理财
        drawable=getResources().getDrawable(R.mipmap.licaitubiao);
        drawable.setBounds(0,0,75,75);
        financebutton.setCompoundDrawables(null,drawable,null,null);
        financebutton.setOnClickListener(this);
    }

    /*
    初始化ysmd的列表适配和列表选项的监听
     */
    private void Init_YSMD() {
        ysmd_listview = (ListView) findViewById(R.id.main_center_listview);//(R.id.textlist_View);
        ysmd_adapter = new MainListAdapter(MainActivity.this, R.layout.main_center_type, ysmd_list);
        ysmd_listview.setAdapter(ysmd_adapter);
        ysmd_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //0本日 1本月 2本季 3本年
                final int pt = position;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("TAG","ysmd_ListView");
                        long[] time = new long[2];
                        if (pt == 0) {
                            time = StartEndTime.GetDay();//按日算
                        } else if (pt == 1) {
                            time = StartEndTime.GetMonth();//按月算
                        } else if (pt == 2) {
                            time = StartEndTime.GetSeason();//按季算
                        } else if (pt == 3) {
                            time = StartEndTime.GetYear();//按年算
                        }
                        Message message = new Message();
                        message.what = ysmd_listview_click;
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", pt);
                        bundle.putLongArray("time", time);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                }).start();
            }
        });
    }

    private void Set_YSMD() {

        Log.d("TAG", "set_ysmd");
        ysmd_list.clear();
        long start_time = 0, end_time = 0;
        long[] time;
        String time_String = "";
        for (int i = 0; i < ysmd_name.length; i++) {
            switch (i) {
                case 0:
                    time = StartEndTime.GetDay();
                    start_time = time[0];
                    end_time = time[1];
                    time_String = DetailList.LongToString(start_time).substring(5, 11);
                    break;
                case 1:
                    time = StartEndTime.GetMonth();
                    start_time = time[0];
                    end_time = time[1];
                    time_String = DetailList.LongToString(start_time).substring(5, 11) +
                            " - " + DetailList.LongToString(end_time).substring(5, 11);
                    break;
                case 2:
                    time = StartEndTime.GetSeason();
                    start_time = time[0];
                    end_time = time[1];
                    time_String = DetailList.LongToString(start_time).substring(5, 11) +
                            " - " + DetailList.LongToString(end_time).substring(5, 11);
                    break;
                case 3:
                    time = StartEndTime.GetYear();
                    start_time = time[0];
                    end_time = time[1];
                    time_String = DetailList.LongToString(start_time).substring(0, 5);
                    break;
                default:
                    break;
            }
            DetailList detailList = new DetailList(Username, start_time, end_time);
            String[] expend_incomemoney = detailList.Get_IandE();
            MainListClass mainListClass = new MainListClass(ysmd_name[i], time_String, expend_incomemoney[0], expend_incomemoney[1]);
            ysmd_list.add(mainListClass);
        }
        handler.sendEmptyMessage(Set_YSMD);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_opendrawer://drawerlayout侧滑菜单显示
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.drawer_username://用户名
            case R.id.drawer_headportrait://用户头像
           // case R.id.userlayout_header://用户头
                //进入用户中心
                mDrawerLayout.closeDrawers();
                if (draweruserrname.getText().toString().equals("请立即登录")) {//没有登录跳转到登录界面
                    intent = new Intent(MainActivity.this, LoginActivity.class);

                } else {//跳转到用户中心
                   intent = new Intent(MainActivity.this, UserCenter.class);
                    intent.putExtra("Username", Username);
                    intent.putExtra("Headportrait", PictureFormatUtil.Drawable2Bytes(Userheadportrait));

                }
                startActivityForResult(intent, 3);//返回之后进行列表的更新
                break;
            case R.id.drawer_SyncData://同步数据
               if(Username.equals("请立即登录")){
                   Toast.makeText(MainActivity.this,"未登录，不能同步！",Toast.LENGTH_SHORT).show();
               }else{//弹出对话框
                   Sync_showDialog();
               }
               break;
            case R.id.maintally_button://进入记账
                intent = new Intent(MainActivity.this, TallyActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("HuoDong","MainActivity.java");
                startActivityForResult(intent, 2);//返回之后进行列表的更新

                break;
            case R.id.maindetail_button://进入流水 输入进去start_time end_time
                long[] time = StartEndTime.GetYear();//按年算
                intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("start_time", time[0]);
                intent.putExtra("end_time", time[1]);
                intent.putExtra("timetype", 3);
                startActivityForResult(intent, 1);//返回之后进行列表的更新
                break;
            case R.id.mainbudget_button://进入预算
                intent = new Intent(MainActivity.this, BudgetActivity.class);
                intent.putExtra("Username", Username);
                startActivity(intent);//不需要进行列表刷新，在预算中只能更改预算
                break;
            case R.id.mainstatistical_button://进入统计
                intent = new Intent(MainActivity.this, StatisticalActivity.class);
                intent.putExtra("Username", Username);
                startActivityForResult(intent, 1);//返回之后进行列表的更新
                break;
            case R.id.mainfinance_button://进入理财
                intent=new Intent(MainActivity.this, FinanceProductActivty.class);
                intent.putExtra("UserNumber",UserNumber);
                startActivity(intent);
                break;
        }
    }

    private void Sync_showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("温馨提示");
        builder.setMessage("你确定要同步数据么？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               SyncData();
                 }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void SyncData(){
        new Thread(new Runnable() {//进行同步操作
            @Override
            public void run() {
                Message message = new Message();
                message.what = 3;
                try {
                    db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    message.obj = Data_ZIP.Data_Sync(db, Username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("TAG1", "同步线程启动");
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
                handler.sendEmptyMessage(failed);
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
                        if (resultCode.equals("200")) {//备份成功
                            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                            ContentValues values = new ContentValues();
                            values.put("Time", jsonObject.getLong("Time"));
                            db.update("userinfo", values, "User_Name=?",
                                    new String[]{Username});
                            handler.sendEmptyMessage(success);
                        }
                    } catch (JSONException e) {
                        Log.d("TAG1", "出现错误");
                        e.printStackTrace();
                    }
                } else {
                    Log.d("TAG1", "返回responseText为空  " + resultCode);
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1://进行刷新
                Set_YSMD();
                break;
            case 2://编辑数据
                    Set_YSMD();
                break;
            case 3://用户
                Get_User();
                Init_Drawerlayout();
                Init_MainButton();
                Init_YSMD();
                Set_YSMD();
                break;
            default:
                break;
        }
    }
}