package com.personalfinance.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.personalfinance.app.Main.MainListAdapter;
import com.personalfinance.app.Main.MainListClass;
import com.personalfinance.app.Sqlite.SQLiteDatabaseHelper;
import com.personalfinance.app.User.RegisterLogin;
import com.personalfinance.app.User.UserCenter;
import com.personalfinance.app.Util.PictureFormatUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /*
     *数据库建立
     */
    private SQLiteDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    /*
     *用户名和用户头像
     */
    private String Username = "";
    private Drawable Userheadportrait;
    /*
     *drawerlayout
     */
    private DrawerLayout mDrawerLayout;
    private View userheaderView;
    private RelativeLayout userheaderlayout;
    private TextView draweruserrname;
    private CircleImageView drawerheadportrait;

    /*
    按键
     */
    private RelativeLayout main_opendrawer;
    private ImageView main_opendraweriv;

    //  private Button tallybutton,detailbutton,budgetbutton,statisticalbutton;
    private TextView tallybutton, detailbutton, budgetbutton, statisticalbutton;
    private Drawable drawable;
    private Intent intent;
    /*
    本日月季年
     */
    private String[] ysmd_name = new String[]{"本日", "本月", "本季", "本年"};//0,1,2,3
    private ListView ysmd_listview;
    private List<MainListClass> ysmd_list = new ArrayList<>();
    private ArrayAdapter<MainListClass> ysmd_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new SQLiteDatabaseHelper(this, "personal.db", null, 1);
        db = dbHelper.getWritableDatabase();
        //用户名和头像获取
        cursor = db.query("userinfo", null, "User_Login=?",
                new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            Username = cursor.getString(cursor.getColumnIndex("User_Name"));
            // iv.setImageDrawable(getDrawable().get(0));
            byte[] blob=cursor.getBlob(cursor.getColumnIndexOrThrow("Head_Portrait"));
            Userheadportrait= PictureFormatUtil.Bytes2Drawable(getResources(),blob);
        } else {
            //获取默认用户名和用户头像
            cursor = db.query("userinfo", null, "User_Login=?",
                    new String[]{"0"}, null, null, null);
            if(cursor.moveToFirst()){
                Username = cursor.getString(cursor.getColumnIndex("User_Name"));
                // iv.setImageDrawable(getDrawable().get(0));
                byte[] blob=cursor.getBlob(cursor.getColumnIndexOrThrow("Head_Portrait"));
                Userheadportrait= PictureFormatUtil.Bytes2Drawable(getResources(),blob);
            }
        }
        Init_Drawerlayout();
        Init_MainButton();
        Init_YSMD();
        Set_YSMD();
    }

    /*
    初始化DrawerLayout相关的控件
     */
    private void Init_Drawerlayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);//侧滑菜单
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        userheaderView = navView.getHeaderView(0);
        userheaderlayout = (RelativeLayout) userheaderView.findViewById(R.id.userlayout_header);//用户
        userheaderlayout.setOnClickListener(this);
        draweruserrname = (TextView) userheaderView.findViewById(R.id.drawer_username);//用户名
        drawerheadportrait=(CircleImageView)userheaderView.findViewById(R.id.drawer_headportrait);//用户头像
        //用户名已获得
        draweruserrname.setText(Username);
        drawerheadportrait.setImageDrawable(Userheadportrait);
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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, ysmd_list.get(position).getName(), Toast.LENGTH_SHORT).show();
                //0本日 1本月 2本季 3本年
                long[] time = new long[2];
                if (position == 0) {
                    time = StartEndTime.GetDay();//按日算
                } else if (position == 1) {
                    time = StartEndTime.GetMonth();//按月算
                } else if (position == 2) {
                    time = StartEndTime.GetSeason();//按季算
                } else if (position == 3) {
                    time = StartEndTime.GetYear();//按年算
                }
                intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("start_time", time[0]);
                intent.putExtra("end_time", time[1]);
                intent.putExtra("timetype", position);
                startActivityForResult(intent, 1);//返回之后进行列表的更新
            }
        });
    }

    /*
    设置ysmd列表的内容并进行更新适配器
     */
    private void Set_YSMD() {
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
        ysmd_adapter.notifyDataSetChanged();
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_opendrawer://drawerlayout侧滑菜单显示
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.userlayout_header://用户头
                Toast.makeText(MainActivity.this, "点击用户头像部分", Toast.LENGTH_SHORT).show();
                //进入用户中心
                mDrawerLayout.closeDrawers();
                if (draweruserrname.getText().toString().equals("请立即登录")) {//没有登录跳转到登录界面
                    intent = new Intent(MainActivity.this, RegisterLogin.class);
                    //startActivityForResult(intent, 1);
                    startActivity(intent);
                } else {//跳转到用户中心中
                   intent = new Intent(MainActivity.this, UserCenter.class);
                    startActivity(intent);
                }
                // finish();
                break;
            case R.id.maintally_button://进入记账
                Toast.makeText(MainActivity.this, "进入记账中", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, TallyActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("HuoDong","MainActivity.java");
                startActivityForResult(intent, 2);//返回之后进行列表的更新

                break;
            case R.id.maindetail_button://进入流水 输入进去start_time end_time
                Toast.makeText(MainActivity.this, "进入流水中", Toast.LENGTH_SHORT).show();
                long[] time = StartEndTime.GetYear();//按年算
                intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("start_time", time[0]);
                intent.putExtra("end_time", time[1]);
                intent.putExtra("timetype", 3);
                startActivityForResult(intent, 1);//返回之后进行列表的更新
                //startActivity(intent);
                //finish();
                break;
            case R.id.mainbudget_button://进入预算
                Toast.makeText(MainActivity.this, "进入预算中", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, BudgetActivity.class);
                intent.putExtra("Username", Username);
                startActivity(intent);//不需要进行列表刷新，在预算中只能更改预算
                // finish();
                break;
            case R.id.mainstatistical_button://进入统计
                Toast.makeText(MainActivity.this, "进入统计中", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, StatisticalActivity.class);
                intent.putExtra("Username", Username);
                startActivityForResult(intent, 1);//返回之后进行列表的更新
                //finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1://进行刷新
                Set_YSMD();
                break;
            case 2://编辑数据
                int issave = data.getIntExtra("issave", -1);
                if (!((resultCode == RESULT_OK) && (issave == 0))) {
                    Set_YSMD();
                }
                break;
            default:
                break;
        }
    }
}