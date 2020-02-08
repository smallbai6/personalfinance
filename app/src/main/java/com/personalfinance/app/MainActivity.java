package com.personalfinance.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.personalfinance.app.sqlite.SQLiteDatabaseHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /*
     *数据库建立
     */
    private SQLiteDatabaseHelper dbHelper;
    SQLiteDatabase db;
    /*
     *用户信息；
     */
    private String username = "";
    /*
     *drawerlayout
     */
    private DrawerLayout mDrawerLayout;
    private View userheaderView;
    private RelativeLayout userheaderlayout;
    private TextView draweruserrname;

    private Button drawerbutton;

    private Button tallybutton,detailbutton;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new SQLiteDatabaseHelper(this, "personal.db", null, 1);
        db = dbHelper.getWritableDatabase();
        //Intent intent = new Intent(MainActivity.this, TallyActivity.class);
        //startActivity(intent);
        // finish();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        drawerbutton = (Button) findViewById(R.id.drawer_button);
        drawerbutton.setOnClickListener(this);
        userheaderView = navView.getHeaderView(0);
        userheaderlayout = (RelativeLayout) userheaderView.findViewById(R.id.userlayout_header);
        userheaderlayout.setOnClickListener(this);
        tallybutton = (Button) findViewById(R.id.maintally_button);
        tallybutton.setOnClickListener(this);
        detailbutton=(Button)findViewById(R.id.maindetail_button);
        detailbutton.setOnClickListener(this);



        draweruserrname = (TextView) userheaderView.findViewById(R.id.drawer_username);
        //用户名和头像获取
        Cursor cursor = db.query("userinfo", null, "User_Login=?", new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            username=cursor.getString(cursor.getColumnIndex("User_Name"));
        }else {//没有登录用户时用户名就为请立即登录
            username="请立即登录";
        }
        //用户名已获得
        draweruserrname.setText(username);
        Toast.makeText(MainActivity.this, "用户名为" + username, Toast.LENGTH_SHORT).show();

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drawer_button://drawerlayout
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.userlayout_header://用户头
                Toast.makeText(MainActivity.this, "点击用户头像部分", Toast.LENGTH_SHORT).show();
                //进入用户中心
                mDrawerLayout.closeDrawers();
                if (draweruserrname.getText().toString() == "请立即登录") {//没有登录跳转到登录界面
                    intent = new Intent(MainActivity.this, RegisterLogin.class);
                    //startActivityForResult(intent, 1);
                    startActivity(intent);
                } else {//跳转到用户中心中
                   intent = new Intent(MainActivity.this, UserCenter.class);
                    startActivity(intent);
                }
                break;
            case R.id.maintally_button://进入记账
                Toast.makeText(MainActivity.this, "进入记账中", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, TallyActivity.class);
                startActivity(intent);
                break;
            case R.id.maindetail_button://进入流水
                Toast.makeText(MainActivity.this, "进入流水中", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
                break;

        }
    }



}