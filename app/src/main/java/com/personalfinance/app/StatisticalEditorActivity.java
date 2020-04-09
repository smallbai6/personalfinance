package com.personalfinance.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Detail.OnInnerItemClickListener;
import com.personalfinance.app.Sqlite.Info;
import com.personalfinance.app.Sqlite.Node;
import com.personalfinance.app.Sqlite.NodeData;
import com.personalfinance.app.Statistical.StatisticalAdapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StatisticalEditorActivity extends AppCompatActivity implements View.OnClickListener{
    private SQLiteDatabase db;
  //  final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Cursor cursor;
    private Intent intent;
    private Drawable drawable;
    /*
    返回键
     */
    private TextView backbutton;
    /*
    获取的用户名，收支类型，消费名称，开始时间，结束时间
     */
    private String[] ioreString = new String[]{"支出", "收入"};
    private String Username, iore, type;
    private long start_time, end_time;

    private ListView listView;
    private StatisticalAdapter mAdapter;
    private List<Node> list = new ArrayList<>();
    private List<Info> InfoList = new ArrayList<>();
    private List<String> TimeList = new ArrayList<>();

    private final static int list_adapter=1;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case list_adapter:
                    list_adapter();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistical_peditor);
       // db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        intent = getIntent();
        Username = intent.getStringExtra("Username");
        iore = intent.getStringExtra("iore");
        type = intent.getStringExtra("type_name");
        start_time = intent.getLongExtra("start", -1);
        end_time = intent.getLongExtra("end", -1);
        listView = (ListView) findViewById(R.id.statistical_pediotr_listview);
        backbutton=(TextView)findViewById(R.id.statistical_pediotr_back);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        backbutton.setCompoundDrawables(drawable, null, null, null);
        backbutton.setOnClickListener(this);
        backbutton.setText(type+"("+iore+")");
        Get_List();

    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.statistical_pediotr_back:
                intent=new Intent(StatisticalEditorActivity.this,StatisticalActivity.class);
                setResult(RESULT_OK);
                finish();
                break;
        }
}
    private void Get_InfoList() {
        InfoList.clear();
        int i = 0;
        try {
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            if (iore.equals(ioreString[0])) {
                i = 0;
                cursor = db.query("expendinfo", null, "User_Name=? AND Type=? ",
                        new String[]{Username, type}, null, null, null);
            } else if (iore.equals(ioreString[1])) {
                i = 1;
                cursor = db.query("incomeinfo", null,
                        "User_Name=? AND Type=?",
                        new String[]{Username, type}, null, null, null);
            }
            if (cursor.moveToFirst()) {
                do {
                    long time = cursor.getLong(cursor.getColumnIndex("Time"));
                    if ((time >= start_time) && (time <= end_time)) {
                        String money = cursor.getString(cursor.getColumnIndex("Money"));
                        String text = cursor.getString(cursor.getColumnIndex("Message"));
                        Info info = new Info(Double.valueOf(money), i + type, time, text);
                        InfoList.add(info);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            cursor.close();
            db.close();
        }
        Collections.sort(InfoList);//对列表进行排序，通过时间进行倒叙排序
    }

    private void Get_TimeList() {
        TimeList.clear();
        String time;
        boolean createnew = true;
        for (int i = 0; i < InfoList.size(); i++) {
            if (i == 0) {
                time = LongToString(InfoList.get(0).getTime()).substring(0, 11);
            } else if (i != 0 && createnew) {
                time = LongToString(InfoList.get(i).getTime()).substring(0, 11);
            } else {
                time = LongToString(InfoList.get(i - 1).getTime()).substring(0, 11);
            }
            if (LongToString(InfoList.get(i).getTime()).substring(0, 11).equals(time)) {//如果是同一天
                createnew = false;
                if (i == (InfoList.size() - 1)) {
                    TimeList.add(LongToString(InfoList.get(i).getTime()));
                }
            } else {//总结出一个
                //记录上一个的时间
                TimeList.add(LongToString(InfoList.get(i - 1).getTime()));
                i = i - 1;
                createnew = true;
            }

        }
      /*  for (String string : TimeList) {
            Log.d("liangjialing", string);
        }*/
    }

    private void Get_List() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int a, b = 0;
                int i = 0;//为指针在list中的位置
                int leveldivide01;//等级划分关联
                list.clear();
                Get_InfoList();
                Get_TimeList();
                NodeData nodeData;
                for (a = 0; a < TimeList.size(); a++) {
                    nodeData = new NodeData(TimeList.get(a), "", "", "", "", "", 0);
                    list.add(new Node<NodeData>(i + "", "-1", nodeData, "0"));
                    leveldivide01 = i;
                    i++;
                    while (TimeList.get(a).substring(0, 10).equals(LongToString(InfoList.get(b).getTime()).substring(0, 10))) {
                        nodeData = new NodeData(InfoList.get(b).getType(),
                                LongToString(InfoList.get(b).getTime()).substring(11, 16),
                                InfoList.get(b).getText(),
                                formatPrice(InfoList.get(b).getMoney()), "", "", InfoList.get(b).getTime());
                        list.add(new Node<NodeData>(i + "", leveldivide01 + "", nodeData, "1"));
                        i++;
                        b++;
                        if (b >= InfoList.size()) {
                            break;
                        }
                    }
                }
                handler.sendEmptyMessage(list_adapter);
                //list_adapter();
            }
        }).start();
    }

    private void list_adapter() {
        mAdapter = new StatisticalAdapter(listView, this, list,
                1, R.mipmap.shangjiantou, R.mipmap.xiajiantou);
        listView.setAdapter(mAdapter);
        mAdapter.setOnInnerItemClickListener(new OnInnerItemClickListener() {
            @Override
            public void onClick(Node node, int position) {
                NodeData nodeData = (NodeData) node.getData();
                Toast.makeText(StatisticalEditorActivity.this, "短点  " + nodeData.getC(), Toast.LENGTH_SHORT).show();
                intent = new Intent(StatisticalEditorActivity.this, TallyEditorActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("money", nodeData.getD());
                intent.putExtra("type", nodeData.getA());
                intent.putExtra("time", nodeData.getTime());
                intent.putExtra("message", nodeData.getC());
                intent.putExtra("HuoDong","StatisticcalEditorActivity.java");
                startActivityForResult(intent, 1);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1://重新刷新一遍
                if(resultCode==RESULT_OK){
                    Get_List();
                }
                break;
            default:
                break;
        }
    }
    /*
     *时间数据类型转换
     */
    private String LongToString(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间HH:mm:ss SSS
        String sDateTime = new SimpleDateFormat("yyyy.MM.dd HH:mm EE").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }

    public static String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(price);
        return format;
    }
}
