package com.personalfinance.app.DetailBulk;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Detail.DetailInfo;
import com.personalfinance.app.DetailActivity;
import com.personalfinance.app.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DetailEditorActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Cursor cursor;
    private String Username;
    private String currentyear;
    private Intent intent;
    /*
    列表
     */
    private List<DetailInfo> InfoList = new ArrayList<>();
    private List<String> day_dayList = new ArrayList<>();
    private ListView listView;
    private List<DENode> list = new ArrayList<>();
    private ListViewAdapter mAdapter;
    /*
    显示的textview和按键
     */
    private TextView allchoose;
    private TextView showchoosetotal, showtotalmoney;
    private ImageView backIV;
    private Button allchoosebutton, allnochoosebutton, deletebutton;
    private int isdelete = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_bulkeditor);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        intent = getIntent();
        Username = intent.getStringExtra("username");
        currentyear = intent.getStringExtra("year");
        listView = (ListView) findViewById(R.id.detail_bulkeditor_listview);
        detailmonth_list();
        mAdapter = new ListViewAdapter(listView, this, list,
                1, R.mipmap.shangjiantou, R.mipmap.xiajiantou);
        listView.setAdapter(mAdapter);
        allchoose = (TextView) findViewById(R.id.detail_bulkeditor_allchoose);
        allchoose.setOnClickListener(this);
        showchoosetotal = (TextView) findViewById(R.id.detail_bulkeditor_showchoosetotal);
        showtotalmoney = (TextView) findViewById(R.id.detail_bulkeditor_showtotalmoney);
        showchoosetotal.setText("未进行选择");
        showtotalmoney.setText("");
        backIV = (ImageView) findViewById(R.id.detail_bulkeditor_back);
        backIV.setOnClickListener(this);
        allchoosebutton = (Button) findViewById(R.id.detail_bulkeditor_allchoosebutton);
        allchoosebutton.setOnClickListener(this);
        allnochoosebutton = (Button) findViewById(R.id.detail_bulkeditor_allnochoosebutton);
        allnochoosebutton.setOnClickListener(this);
        deletebutton = (Button) findViewById(R.id.detail_bulkeditor_deletebutton);
        deletebutton.setOnClickListener(this);
        //选中状态监听
        mAdapter.setCheckedChangeListener(new OnTreeNodeCheckedChangeListener() {
            @Override
            public void onCheckChange(DENode node, int position, boolean isChecked) {
                showchoosetotal_totalmoney();
            }
        });
        mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
            @Override
            public void onClick(DENode node, int position) {
                showchoosetotal_totalmoney();
            }
        });
    }

    private void showchoosetotal_totalmoney() {
        Log.d("setcheck", "dianji");
        //获取所有选中节点
        int total = 0;
        double totalmoney = 0;
        List<DENode> selectedNode = mAdapter.getSelectedNode();
        if (selectedNode.size() == 0) {//选中的节点为空，都没有选中；
            showchoosetotal.setText("未进行选择");
            showtotalmoney.setText("");
        } else {
            for (DENode n : selectedNode) {
                if (n.isLeaf()) {//1级计入数内
                    DENodeData deNodeData = (DENodeData) n.getData();
                    total++;
                    if (deNodeData.getType().substring(0, 1).equals("0")) {
                        totalmoney -= Double.valueOf(deNodeData.getMoney());
                    } else {
                        totalmoney += Double.valueOf(deNodeData.getMoney());
                    }
                }
            }
            showchoosetotal.setText("已选择了" + total + "条");
            showtotalmoney.setText("合计: " + formatPrice(totalmoney));
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_bulkeditor_back:
                intent = new Intent(DetailEditorActivity.this, DetailActivity.class);
                intent.putExtra("isdelete", isdelete);
                setResult(RESULT_OK, intent);
                finish();
                // startActivity(intent);

                break;
            case R.id.detail_bulkeditor_allchoosebutton:
            case R.id.detail_bulkeditor_allchoose://全选
                List<DENode> allNode = mAdapter.getAllNodes();
                for (DENode node : allNode) {
                    mAdapter.setChecked(node, true);
                }
                showchoosetotal_totalmoney();
                break;
            case R.id.detail_bulkeditor_allnochoosebutton://全不选
                List<DENode> allNodec = mAdapter.getAllNodes();
                for (DENode node : allNodec) {
                    mAdapter.setChecked(node, false);
                }
                showchoosetotal_totalmoney();
                break;
            case R.id.detail_bulkeditor_deletebutton://删除
                isdelete++;
                delete();
                break;
        }
    }

    private void delete() {
        List<DENode> selectedNode = mAdapter.getSelectedNode();
        for (DENode node : selectedNode) {
            DENodeData deNodeData = (DENodeData) node.getData();
            if (node.isLeaf()) {
                //如果是叶子节点,进行删除
                String type = deNodeData.getType();
                String money = deNodeData.getMoney();
                String time = String.valueOf(deNodeData.getTime());
                if (type.substring(0, 1).equals("0")) {
                    //支出

                    db.delete("expendinfo", "User_Name=? AND Expend_Money=? " +
                                    "AND Expend_Type=? AND Expend_Time=?",
                            new String[]{Username, money, type.substring(1), time});
                } else if (type.substring(0, 1).equals("1")) {
                    db.delete("incomeinfo", "User_Name=? AND Income_Money=? " +
                                    "AND Income_Type=? AND Income_Time=? ",
                            new String[]{Username, money, type.substring(1), time});
                }
            }
        }
        //使用getVisibleNodes()删除allNode中的节点，更新显示的节点
        mAdapter.getVisibleNodes();
        showchoosetotal.setText("未进行选择");
        showtotalmoney.setText("");

    }


    private void detail_DetailInfo(String currentyear) {
        //获得消费详情
        InfoList.clear();
        //支出信息
        cursor = db.query("expendinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(cursor.getColumnIndex("Expend_Time"));
                if (LongToString(time).substring(0, 4).equals(currentyear)) {
                    String money = cursor.getString(cursor.getColumnIndex("Expend_Money"));
                    String type = 0 + cursor.getString(cursor.getColumnIndex("Expend_Type"));
                    String text = cursor.getString(cursor.getColumnIndex("Expend_Message"));
                    DetailInfo detailInfo = new DetailInfo(Double.valueOf(money), type, time, text);
                    InfoList.add(detailInfo);
                }
            } while (cursor.moveToNext());
        }
        //收入信息
        cursor = db.query("incomeinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(cursor.getColumnIndex("Income_Time"));
                if (LongToString(time).substring(0, 4).equals(currentyear)) {
                    String money = cursor.getString(cursor.getColumnIndex("Income_Money"));
                    String type = 1 + cursor.getString(cursor.getColumnIndex("Income_Type"));
                    String text = cursor.getString(cursor.getColumnIndex("Income_Message"));
                    DetailInfo detailInfo = new DetailInfo(Double.valueOf(money), type, time, text);
                    InfoList.add(detailInfo);
                }

            } while (cursor.moveToNext());
        }
        Collections.sort(InfoList);

        for (DetailInfo detailInfo : InfoList) {
            Log.d("DetailActivity.liang", detailInfo.getMoney() + "  " + detailInfo.getType()
                    + "  " + detailInfo.getTime() + "   " + detailInfo.getText() + "      " + LongToString(detailInfo.getTime()));
        }
    }

    private void detail_Detailday_day() {
        //Log.d("DetailActivity.liang", "进入detail_Detailday_day");
        day_dayList.clear();
        //获得天的结余
        double expendmoney = 0;
        double incomemoney = 0;
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
                if (InfoList.get(i).getType().substring(0, 1).equals("0")) {//判断是为支出
                    expendmoney = expendmoney + InfoList.get(i).getMoney();
                } else if (InfoList.get(i).getType().substring(0, 1).equals("1")) {//判断是为收入
                    incomemoney = incomemoney + InfoList.get(i).getMoney();
                }
                createnew = false;
                if (i == (InfoList.size() - 1)) {
                   /* String day = LongToString(InfoList.get(i).getTime()).substring(8, 11);
                    String date = LongToString(InfoList.get(i).getTime()).substring(0, 7);
                    double jieyu = incomemoney - expendmoney;
                    double shouru = incomemoney;
                    double zhichu = expendmoney;
                    long lasttime = InfoList.get(i).getTime();
                    DetailSurplus detailday_day = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                    day_dayList.add(detailday_day);*/
                    day_dayList.add(LongToString(InfoList.get(i).getTime()).substring(0, 11));
                }
            } else {//总结出一个
                /*String day = LongToString(InfoList.get(i - 1).getTime()).substring(8, 11);
                String date = LongToString(InfoList.get(i - 1).getTime()).substring(0, 7);
                double jieyu = incomemoney - expendmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = InfoList.get(i - 1).getTime();
                DetailSurplus detailday_day = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                day_dayList.add(detailday_day);*/
                day_dayList.add(LongToString(InfoList.get(i - 1).getTime()).substring(0, 11));
                incomemoney = 0;
                expendmoney = 0;
                i = i - 1;
                createnew = true;
            }
        }
        Log.d("DEAliang", "1级流水");
        for (String detailSurplus : day_dayList) {
            Log.d("DEAliang", detailSurplus);
        }
    }

    private void detailmonth_list() {
        Log.d("DEAliang", "进入detailmonth_list");
        list.clear();
        detail_DetailInfo("2020");
        detail_Detailday_day();
        //开始综合list，实现流水列表
        int a = 0, b = 0, c = 0;//a为月，b为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01;//等级划分关联
        DENodeData deNodeData;
        for (a = 0; a < day_dayList.size(); a++) {
            /*list.add(new Item(i, 0, 0, false,
                    day_dayList.get(a).getDay(),
                    day_dayList.get(a).getDate(),
                    formatPrice(day_dayList.get(a).getJieyu()),
                    formatPrice(day_dayList.get(a).getShouru()),
                    formatPrice(day_dayList.get(a).getZhichu()),
                    "",
                    day_dayList.get(a).getTime()));*/
            deNodeData = new DENodeData(day_dayList.get(a), "", "", 0);
            list.add(new DENode<DENodeData>(i + "", "-1", deNodeData, "0"));
            leveldivide01 = i;
            i++;
            // Log.d("aaaaad","InfoList.get(b).getTime() =  "+LongToString(InfoList.get(b).getTime()).substring(0,11));
            while (day_dayList.get(a).equals(LongToString(InfoList.get(b).getTime()).substring(0, 11))) {
                /*list.add(new Item(i, leveldivide01, 1, false,false,
                        InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(11, 16),
                        formatPrice(InfoList.get(b).getMoney())));*/
                //  Log.d("aaaaad","list");
                deNodeData = new DENodeData(InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(11, 16), formatPrice(InfoList.get(b).getMoney()),
                        InfoList.get(b).getTime());
                list.add(new DENode<DENodeData>(i + "", leveldivide01 + "", deNodeData, "1"));
                i++;
                b++;
                if (b >= InfoList.size()) {
                    break;
                }
            }
        }
    }


    private String LongToString(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy年MM月dd日HH:mmEE").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }

    public static String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(price);
        return format;
    }
}
