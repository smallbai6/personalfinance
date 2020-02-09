package com.personalfinance.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Detail.DetailInfo;
import com.personalfinance.app.Detail.DetailSurplus;
import com.personalfinance.app.Detail.Node;
import com.personalfinance.app.Detail.TreeAdapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Cursor cursor;
    private String Username;
    private Intent intent;
    /*按键

     */
    private TextView backbutton, choosermd;
    /*
    年份和季月天选择
     */
    PopupWindow choosePopupWindow;
    private View contentView;
    private ListView chooselistView;
    private List<String> yrmdchooseList = new ArrayList<>();
    private ArrayAdapter<String> yrmdadapter;

    private String[] yearString = {"上一年", "下一年"};
    private String[] rmdString = {"季", "月", "日"};
    private Drawable drawable;//按键旁的图标显示
    private int choosetype;//选择类型是yearString 还是rmdString
    private ImageView choosey;//点击进行上一年 下一年选择
    private Calendar calendar = Calendar.getInstance();//获得当前时间
    private int showyear;//当前需要显示时间
    /*
    总列表和各个等级排序列表
     */
    private ListView listView;
    private List<Item> list = new ArrayList<>();
    private MyAdapter adapter;
    private List<DetailInfo> InfoList = new ArrayList<>();
    private List<DetailSurplus> day_dayList = new ArrayList<>();
    private List<DetailSurplus> day_monthList = new ArrayList<>();
    private List<DetailSurplus> month_monthList = new ArrayList<>();
    private List<DetailSurplus> season_seasonList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        listView = (ListView) findViewById(R.id.listview);//流水列表
        // seasonlist();

        backbutton = (TextView) findViewById(R.id.detail_back_button);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        backbutton.setCompoundDrawables(drawable, null, null, null);
        showyear = calendar.get(Calendar.YEAR);
        backbutton.setText(showyear + "年");

        choosermd = (TextView) findViewById(R.id.detail_choose_rmd);//选择季月
        drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
        drawable2tubiao();

        choosey = (ImageView) findViewById(R.id.detail_year_shengluetubiao);

        contentView = getLayoutInflater().inflate(R.layout.textlist, null);
        chooselistView = contentView.findViewById(R.id.textlist_View);
        yrmdadapter = new ArrayAdapter<>(DetailActivity.this, android.R.layout.simple_list_item_1, yrmdchooseList);
        chooselistView.setAdapter(yrmdadapter);
        InitPopupWindow();//初始化popupWindow


        chooselistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (choosetype == 0) {//点击的是年份
                    if (position == 0) {//上一年
                        showyear = showyear - 1;
                    } else if (position == 1) {//下一年
                        showyear = showyear + 1;
                    }
                    backbutton.setText(showyear + "年");
                } else if (choosetype == 1) {//点击的是季月日
                    choosermd.setText(yrmdchooseList.get(position));
                }
                choosePopupWindow.dismiss();
            }
        });
        choosePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (choosetype == 1) {
                    drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
                    drawable2tubiao();
                }
            }
        });
        backbutton.setOnClickListener(this);
        choosermd.setOnClickListener(this);
        choosey.setOnClickListener(this);

        cursor = db.query("userinfo", null, "User_Login=?", new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            Username = cursor.getString(cursor.getColumnIndex("User_Name"));
        } else {//没有登录用户时用户名就为请立即登录
            Username = "请立即登录";
        }
        // detail_list();
        // detailmonth_list();
        detailseason_list();
    }

    private void drawable2tubiao() {
        drawable.setBounds(0, 0, 30, 30);
        choosermd.setCompoundDrawables(null, null, drawable, null);
        choosermd.setCompoundDrawablePadding(10);
    }

    private void InitPopupWindow() {
        choosePopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        choosePopupWindow.setWidth(175);
        choosePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        choosePopupWindow.setOutsideTouchable(true);
        choosePopupWindow.setTouchable(true);
        choosePopupWindow.setFocusable(true);
    }

    private void Init2yearlist() {
        //列表内容配置
        yrmdchooseList.clear();
        for (int i = 0; i < yearString.length; i++) {
            yrmdchooseList.add(yearString[i]);
        }
        adapter.notifyDataSetChanged();
        chooselistView.setSelection(0);
    }

    private void Init2rmdlist() {
        //列表内容配置
        yrmdchooseList.clear();
        for (int i = 0; i < rmdString.length; i++) {
            yrmdchooseList.add(rmdString[i]);
        }
        adapter.notifyDataSetChanged();
        chooselistView.setSelection(0);
    }

    private void seasonlist() {
        list.clear();
        list.add(new Item(0, 0, 0, false, "3月", "2020", "5.00", "7.00", "2.00", ""));
        list.add(new Item(1, 0, 1, false, "3月", "2020", "5.00", "7.00", "2.00", ""));
        list.add(new Item(2, 1, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));
        list.add(new Item(3, 1, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));
        list.add(new Item(4, 0, 1, false, "3月", "2020", "5.00", "7.00", "2.00", ""));
        list.add(new Item(5, 4, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));
        list.add(new Item(6, 4, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));

        list.add(new Item(7, 0, 0, false, "3月", "2020", "5.00", "7.00", "2.00", ""));
        list.add(new Item(8, 7, 1, false, "3月", "2020", "5.00", "7.00", "2.00", ""));
        list.add(new Item(9, 8, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));
        list.add(new Item(10, 8, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));
        list.add(new Item(11, 7, 1, false, "3月", "2020", "5.00", "7.00", "2.00", ""));
        list.add(new Item(12, 11, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));
        list.add(new Item(13, 11, 2, false, "23", "周四", "早午晚餐", "13:23", "5.00", "meiyou"));
        adapter = new MyAdapter(list);
        adapter.setOnInnerItemClickListener(new TreeAdapter.OnInnerItemClickListener<Item>() {
            @Override
            public void onClick(Item node) {
                Toast.makeText(DetailActivity.this, "click: ", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInnerItemLongClickListener(new TreeAdapter.OnInnerItemLongClickListener<Item>() {
            @Override
            public void onLongClick(Item node) {
                Toast.makeText(DetailActivity.this, "long click: ", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setAdapter(adapter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_back_button://返回主活动
                intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.detail_year_shengluetubiao://点击上一年下一年的省略图标
                Init2yearlist();
                choosePopupWindow.showAsDropDown(choosey);
                choosetype = 0;
                break;
            case R.id.detail_choose_rmd://选择季月日
                Init2rmdlist();
                choosePopupWindow.showAsDropDown(choosermd);
                if (choosePopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
                    drawable2tubiao();
                }
                choosetype = 1;
                break;
        }
    }

    /*
    日详情列表
     */
    private void detail_DetailInfo(String currentyear) {
        Log.d("DetailActivity.liang", "进入detail_DetailInfo");
        //获得消费详情
        InfoList.clear();
        //支出信息
        cursor = db.query("expendinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(cursor.getColumnIndex("Expend_Time"));
                if (LongToString(time).substring(0, 4).equals(currentyear)) {
                    double money = cursor.getDouble(cursor.getColumnIndex("Expend_Money"));
                    String type = 0 + cursor.getString(cursor.getColumnIndex("Expend_Type"));
                    //long time = cursor.getLong(cursor.getColumnIndex("Expend_Time"));
                    String text = cursor.getString(cursor.getColumnIndex("Expend_Message"));
                    DetailInfo detailInfo = new DetailInfo(money, type, time, text);
                    InfoList.add(detailInfo);
                }
            } while (cursor.moveToNext());
        }
        //收入信息
        cursor = db.query("incomeinfo", null, "User_Name=?",
                new String[]{Username}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                double money = cursor.getDouble(cursor.getColumnIndex("Income_Money"));
                String type = 1 + cursor.getString(cursor.getColumnIndex("Income_Type"));
                long time = cursor.getLong(cursor.getColumnIndex("Income_Time"));
                String text = cursor.getString(cursor.getColumnIndex("Income_Message"));
                DetailInfo detailInfo = new DetailInfo(money, type, time, text);
                InfoList.add(detailInfo);
            } while (cursor.moveToNext());
        }
        Collections.sort(InfoList);

        for (DetailInfo detailInfo : InfoList) {
            Log.d("DetailActivity.liang", detailInfo.getMoney() + "  " + detailInfo.getType()
                    + "  " + detailInfo.getTime() + "   " + detailInfo.getText() + "      " + LongToString(detailInfo.getTime()));
        }
    }

    /*
    天中的天结余
     */
    private void detail_Detailday_day() {
        Log.d("DetailActivity.liang", "进入detail_Detailday_day");
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
                    String day = LongToString(InfoList.get(i).getTime()).substring(8, 11);
                    String date = LongToString(InfoList.get(i).getTime()).substring(0, 7);
                    double jieyu = incomemoney - expendmoney;
                    double shouru = incomemoney;
                    double zhichu = expendmoney;
                    long lasttime = InfoList.get(i).getTime();
                    DetailSurplus detailday_day = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                    day_dayList.add(detailday_day);
                }
            } else {//总结出一个
                String day = LongToString(InfoList.get(i - 1).getTime()).substring(8, 11);
                String date = LongToString(InfoList.get(i - 1).getTime()).substring(0, 7);
                double jieyu = incomemoney - expendmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = InfoList.get(i - 1).getTime();
                DetailSurplus detailday_day = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                day_dayList.add(detailday_day);
                incomemoney = 0;
                expendmoney = 0;
                i = i - 1;
                createnew = true;
            }
            // Log.d("liangaa","day=  "+day);
        }
        // Log.d("DetailActivity.liang", "1级流水");

        for (DetailSurplus detailSurplus : day_dayList) {
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }
    }

    /*
    天中的月结余
     */
    private void detail_Detailday_month() {
        Log.d("DetailActivity.liang", "进入detail_Detailday_month");
        day_monthList.clear();
        //获得月的结余
        double expendmoney = 0;
        double incomemoney = 0;
        double totalmoney = 0;
        String time;
        boolean createnew = true;
        for (int i = 0; i < day_dayList.size(); i++) {
            if (i == 0) {
                time = day_dayList.get(0).getDate().substring(5, 7);
            } else if (i != 0 && createnew) {
                time = day_dayList.get(i).getDate().substring(5, 7);
            } else {
                time = day_dayList.get(i - 1).getDate().substring(5, 7);
            }
            if (day_dayList.get(i).getDate().substring(5, 7).equals(time)) {//如果是同一月份
                expendmoney = expendmoney + day_dayList.get(i).getZhichu();
                incomemoney = incomemoney + day_dayList.get(i).getShouru();
                totalmoney = totalmoney + day_dayList.get(i).getJieyu();
                createnew = false;
                if (i == (day_dayList.size() - 1)) {
                    String day = day_dayList.get(i).getDate().substring(5, 7) + "月";//LongToString(day_dayList.get(i - 1).getTime()).substring(8, 11);
                    String date = day_dayList.get(i).getDate().substring(0, 4);
                    double jieyu = totalmoney;
                    double shouru = incomemoney;
                    double zhichu = expendmoney;
                    long lasttime = day_dayList.get(i).getTime();
                    DetailSurplus detailday_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                    day_monthList.add(detailday_month);
                }
            } else {//总结出一个
                String day = day_dayList.get(i - 1).getDate().substring(5, 7) + "月";//LongToString(day_dayList.get(i - 1).getTime()).substring(8, 11);
                String date = day_dayList.get(i - 1).getDate().substring(0, 4);
                double jieyu = totalmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = day_dayList.get(i - 1).getTime();
                DetailSurplus detailday_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                day_monthList.add(detailday_month);
                incomemoney = 0;
                expendmoney = 0;
                totalmoney = 0;
                i = i - 1;
                createnew = true;
            }
        }
        for (DetailSurplus detailSurplus : day_monthList) {
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }
    }

    /*
    月中月结余同时也是季中的月结余
     */
    private void detail_Detailmonth_month() {
        Log.d("DetailActivity.liang", "进入detail_Detailmonth_month");
        month_monthList.clear();

        //获得月的结余
        double expendmoney = 0;
        double incomemoney = 0;
        String time;
        boolean createnew = true;
        for (int i = 0; i < InfoList.size(); i++) {
            if (i == 0) {
                time = LongToString(InfoList.get(0).getTime()).substring(5, 7);
            } else if (i != 0 && createnew) {
                time = LongToString(InfoList.get(i).getTime()).substring(5, 7);
            } else {
                time = LongToString(InfoList.get(i - 1).getTime()).substring(5, 7);
            }
            if (LongToString(InfoList.get(i).getTime()).substring(5, 7).equals(time)) {//如果是同一月
                if (InfoList.get(i).getType().substring(0, 1).equals("0")) {//判断是为支出
                    expendmoney = expendmoney + InfoList.get(i).getMoney();
                } else if (InfoList.get(i).getType().substring(0, 1).equals("1")) {//判断是为收入
                    incomemoney = incomemoney + InfoList.get(i).getMoney();
                }
                createnew = false;
                if (i == (InfoList.size() - 1)) {
                    String day = LongToString(InfoList.get(i).getTime()).substring(5, 8);
                    String date = LongToString(InfoList.get(i).getTime()).substring(0, 4);
                    double jieyu = incomemoney - expendmoney;
                    double shouru = incomemoney;
                    double zhichu = expendmoney;
                    long lasttime = InfoList.get(i).getTime();
                    DetailSurplus detailmonth_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                    month_monthList.add(detailmonth_month);
                }
            } else {//总结出一个
                String day = LongToString(InfoList.get(i - 1).getTime()).substring(5, 8);
                String date = LongToString(InfoList.get(i - 1).getTime()).substring(0, 4);
                double jieyu = incomemoney - expendmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = InfoList.get(i - 1).getTime();
                DetailSurplus detailmonth_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                month_monthList.add(detailmonth_month);
                incomemoney = 0;
                expendmoney = 0;
                i = i - 1;
                createnew = true;
            }
            // Log.d("liangaa","day=  "+day);
        }

        for (DetailSurplus detailSurplus : month_monthList) {
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }
    }

    /*
    季中的季结余
     */
    private void detail_Detailseason_season() {//Integer.valueOf(month_monthList.get(0).getDay().substring(0,2))
        Log.d("DetailActivity.liang", "进入detail_Detailseason_season");
        season_seasonList.clear();
        double expendmoney = 0;
        double incomemoney = 0;
        double totalmoney = 0;
        int i = 0;//month_monthList列表的指针位置
        boolean change = false;
        int biaoshifu = 0;
        String day = "";
        // Log.d("DetailActivity.liang", "1");
        while (i < month_monthList.size()) {
            //Log.d("DetailActivity.liang", "2");
            switch (Integer.valueOf(month_monthList.get(i).getDay().substring(0, 2))) {
                case 12:
                case 11:
                case 10:
                    day = "4季";
                    biaoshifu = 4;
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        // i++;
                        change = true;
                        //break;
                    } else {
                        //i++;
                        // time = Integer.valueOf(month_monthList.get(i).getDay().substring(0, 2));
                        change = false;
                    }
                    i++;
                    break;
                case 9:
                case 8:
                case 7:
                    if (biaoshifu != 3) {
                        if (biaoshifu == 0) {
                            biaoshifu = 3;
                        } else {
                            biaoshifu = 3;
                            change = true;
                            break;
                        }
                    }
                    day = "3季";
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        // i++;
                        change = true;
                        //break;
                    } else {
                        // i++;
                        // time = Integer.valueOf(month_monthList.get(i).getDay().substring(0, 2));
                        change = false;
                    }
                    i++;
                    break;
                case 6:
                case 5:
                case 4:
                    if (biaoshifu != 2) {
                        if (biaoshifu == 0) {
                            biaoshifu = 2;
                        } else {
                            biaoshifu = 2;
                            change = true;
                            break;
                        }
                    }
                    day = "2季";
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        // i++;
                        change = true;
                        //break;
                    } else {
                        // i++;
                        // time = Integer.valueOf(month_monthList.get(i).getDay().substring(0, 2));
                        change = false;
                    }
                    i++;
                    break;
                case 3:
                case 2:
                case 1:
                    if (biaoshifu != 1) {
                        if (biaoshifu == 0) {
                            biaoshifu = 1;
                        } else {
                            biaoshifu = 1;
                            change = true;
                            break;
                        }
                    }
                    day = "1季";
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        // i++;
                        change = true;
                        //break;
                    } else {
                        //  i++;
                        // time = Integer.valueOf(month_monthList.get(i).getDay().substring(0, 2));
                        change = false;
                    }
                    i++;
                    break;

            }
            if (change == true) {
                // Log.d("DetailActivity.liang", "3");
                String date = month_monthList.get(i - 1).getDate();
                double jieyu = totalmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = month_monthList.get(i - 1).getTime();
                DetailSurplus detailseason_season = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                season_seasonList.add(detailseason_season);
                incomemoney = 0;
                expendmoney = 0;
                totalmoney = 0;
                //change = false;
            }
        }
        //Log.d("DetailActivity.liang", "季0级流水");
        for (DetailSurplus detailSurplus : season_seasonList) {
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }
        // Log.d("DetailActivity.liangji", "季0级流水");
    }

    /*
    季列表
     */
    private void detailseason_list() {
        Log.d("DetailActivity.liang", "进入detailseason_list");
        list.clear();
        detail_DetailInfo(String.valueOf(showyear));
        detail_Detailmonth_month();
        detail_Detailseason_season();
        //开始综合成list，实现流水列表
        int a, b = 0, c = 0;//a为季，b为月，c为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01, leveldivide12;//等级划分关联
        for (a = 0; a < season_seasonList.size(); a++) {
            list.add(new Item(i, 0, 0, false,
                    season_seasonList.get(a).getDay(),
                    season_seasonList.get(a).getDate(),
                    formatPrice(season_seasonList.get(a).getJieyu()),
                    formatPrice(season_seasonList.get(a).getShouru()),
                    formatPrice(season_seasonList.get(a).getZhichu()),
                    ""));
            //Log.d("DetailActivity.liang","i=  "+i);
            leveldivide01 = i;
            i++;
            int ji = Integer.valueOf(season_seasonList.get(a).getDay().substring(0, 1));//     .equals(day_dayList.get(b).getDate().substring(5, 7) + "月")
            //season_seasonList.get(a).getDay().equals(day_dayList.get(b).getDate().substring(5, 7) + "月")
            while ((Integer.valueOf(month_monthList.get(b).getDay().substring(0, 2)) <= (ji * 3))
                    && (Integer.valueOf(month_monthList.get(b).getDay().substring(0, 2)) >= (ji * 3 - 2))) {
                list.add(new Item(i, leveldivide01, 1, false,
                        month_monthList.get(b).getDay(),
                        month_monthList.get(b).getDate(),
                        formatPrice(month_monthList.get(b).getJieyu()),
                        formatPrice(month_monthList.get(b).getShouru()),
                        formatPrice(month_monthList.get(b).getZhichu()),
                        ""));
                leveldivide12 = i;
                i++;
                while (month_monthList.get(b).getDay().equals(LongToString(InfoList.get(c).getTime()).substring(5, 8))) {
                    list.add(new Item(i, leveldivide12, 2, false,
                            LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(16, 18),
                            InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(11, 16),
                            String.valueOf(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText()));
                    i++;
                    c++;
                    if (c >= InfoList.size()) {
                        break;
                    }
                }
                b++;
                if (b >= month_monthList.size()) {
                    break;
                }
            }
        }
        Log.d("DetailActivity.liang", "季0级流水");
        for (Item detailSurplus : list) {
            Log.d("DetailActivity.liang", detailSurplus.a + "  " + detailSurplus.b
                    + "  " + detailSurplus.c + "   " + detailSurplus.d + "      " +
                    detailSurplus.e + "     " + detailSurplus.f);
        }
        adapter = new MyAdapter(list);
        adapter.setOnInnerItemClickListener(new TreeAdapter.OnInnerItemClickListener<Item>() {
            @Override
            public void onClick(Item node) {
                Toast.makeText(DetailActivity.this, "click: ", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInnerItemLongClickListener(new TreeAdapter.OnInnerItemLongClickListener<Item>() {
            @Override
            public void onLongClick(Item node) {
                Toast.makeText(DetailActivity.this, "long click: ", Toast.LENGTH_SHORT).show();
            }
        });
        listView.setAdapter(adapter);
    }

    /*
    月列表
     */
    private void detailmonth_list() {
        Log.d("DetailActivity.liang", "进入detailmonth_list");
        list.clear();
        detail_DetailInfo(String.valueOf(showyear));
        detail_Detailmonth_month();
        //开始综合list，实现流水列表
        int a, b = 0, c = 0;//a为月，b为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01;//等级划分关联
        for (a = 0; a < month_monthList.size(); a++) {
            list.add(new Item(i, 0, 0, false,
                    month_monthList.get(a).getDay(),
                    month_monthList.get(a).getDate(),
                    formatPrice(month_monthList.get(a).getJieyu()),
                    formatPrice(month_monthList.get(a).getShouru()),
                    formatPrice(month_monthList.get(a).getZhichu()),
                    ""));
            //Log.d("DetailActivity.liang","i=  "+i);
            leveldivide01 = i;
            i++;
            //LongtoString(day_monthList.get(a).getTime()).substring(5,7)==LongtoString(day_dayList.get(b).getTime()).substring(5,7)
            while (month_monthList.get(a).getDay().equals(LongToString(InfoList.get(b).getTime()).substring(5, 8))) {
                list.add(new Item(i, leveldivide01, 1, false,
                        LongToString(InfoList.get(b).getTime()).substring(8, 10),
                        LongToString(InfoList.get(b).getTime()).substring(16, 18),
                        InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(11, 16),
                        String.valueOf(InfoList.get(b).getMoney()),
                        InfoList.get(b).getText()));
                //Log.d("DetailActivity.liang","i=  "+i);
                i++;
                b++;
                if (b >= InfoList.size()) {
                    break;
                }
            }
        }
        adapter = new MyAdapter(list);
        adapter.setOnInnerItemClickListener(new TreeAdapter.OnInnerItemClickListener<Item>() {
            @Override
            public void onClick(Item node) {
                Toast.makeText(DetailActivity.this, "click: ", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInnerItemLongClickListener(new TreeAdapter.OnInnerItemLongClickListener<Item>() {
            @Override
            public void onLongClick(Item node) {
                Toast.makeText(DetailActivity.this, "long click: ", Toast.LENGTH_SHORT).show();
            }
        });
        listView.setAdapter(adapter);
    }

    /*
    天列表
     */
    private void detailday_list() {
        Log.d("DetailActivity.liang", "进入detailday_list");
        list.clear();
        detail_DetailInfo(String.valueOf(showyear));
        detail_Detailday_day();
        detail_Detailday_month();
        //开始进行将三个day_monthList+day_dayList+InfoList进行综合，综合成list，实现流水列表

        int a, b = 0, c = 0;//a为月，b为日，c为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01, leveldivide12;//等级划分关联
        for (a = 0; a < day_monthList.size(); a++) {
            list.add(new Item(i, 0, 0, false,
                    day_monthList.get(a).getDay(),
                    day_monthList.get(a).getDate(),
                    formatPrice(day_monthList.get(a).getJieyu()),
                    formatPrice(day_monthList.get(a).getShouru()),
                    formatPrice(day_monthList.get(a).getZhichu()),
                    ""));
            //Log.d("DetailActivity.liang","i=  "+i);
            leveldivide01 = i;
            i++;
            //LongtoString(day_monthList.get(a).getTime()).substring(5,7)==LongtoString(day_dayList.get(b).getTime()).substring(5,7)
            while (day_monthList.get(a).getDay().equals(day_dayList.get(b).getDate().substring(5, 7) + "月")) {
                list.add(new Item(i, leveldivide01, 1, false,
                        day_dayList.get(b).getDay(),
                        day_dayList.get(b).getDate(),
                        formatPrice(day_dayList.get(b).getJieyu()),
                        formatPrice(day_dayList.get(b).getShouru()),
                        formatPrice(day_dayList.get(b).getZhichu()),
                        ""));
                //Log.d("DetailActivity.liang","i=  "+i);
                leveldivide12 = i;
                i++;
                while (LongToString(day_dayList.get(b).getTime()).substring(0, 11).equals(
                        LongToString(InfoList.get(c).getTime()).substring(0, 11))) {
                    list.add(new Item(i, leveldivide12, 2, false,
                            LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(16, 18),
                            InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(11, 16),
                            String.valueOf(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText()));
                    // Log.d("DetailActivity.liang","i=  "+i);
                    i++;
                    c++;
                    if (c >= InfoList.size()) {
                        break;
                    }
                }
                b++;
                if (b >= day_dayList.size()) {
                    break;
                }
            }
        }
        adapter = new MyAdapter(list);
        adapter.setOnInnerItemClickListener(new TreeAdapter.OnInnerItemClickListener<Item>() {
            @Override
            public void onClick(Item node) {
                Toast.makeText(DetailActivity.this, "click: ", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInnerItemLongClickListener(new TreeAdapter.OnInnerItemLongClickListener<Item>() {
            @Override
            public void onLongClick(Item node) {
                Toast.makeText(DetailActivity.this, "long click: ", Toast.LENGTH_SHORT).show();
            }
        });
        listView.setAdapter(adapter);
    }

    /*
    适配器
     */
    private class MyAdapter extends TreeAdapter<Item> {
        MyAdapter(List<Item> nodes) {
            super(nodes);
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        /**
         * 获取当前位置的条目类型
         */
        @Override
        public int getItemViewType(int position) {
            if (getItem(position).hasChild()) {
                return 1;
            }
            return 0;
        }

        @Override
        protected Holder<Item> getHolder(int position) {
            switch (getItemViewType(position)) {
                case 1:
                    return new Holder<Item>() {
                        private ImageView iv;
                        private TextView tva, tvb, tvc, tvd, tve;

                        @Override
                        protected void setData(Item node) {
                            iv.setVisibility(node.hasChild() ? View.VISIBLE : View.INVISIBLE);
                            iv.setBackgroundResource(node.isExpand ? R.mipmap.xiajiantou : R.mipmap.shangjiantou);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tva.getLayoutParams();
                            params.leftMargin = node.level * 20 + 10;
                            tva.setLayoutParams(params);
                            tva.setText(node.a);
                            tvb.setText(node.b);
                            tvc.setText(node.c);
                            tvd.setText(node.d);
                            tve.setText(node.e);
                        }

                        @Override
                        protected View createConvertView(int position) {
                            View view = View.inflate(DetailActivity.this, R.layout.detail_type_a, null);
                            iv = (ImageView) view.findViewById(R.id.a_detail_jiantou);
                            tva = (TextView) view.findViewById(R.id.a_detail_month);
                            tvb = (TextView) view.findViewById(R.id.a_detail_year);
                            tvc = (TextView) view.findViewById(R.id.a_detail_totalmoney);
                            tvd = (TextView) view.findViewById(R.id.a_detail_incomemoney);
                            tve = (TextView) view.findViewById(R.id.a_detail_expendmoney);
                            return view;
                        }
                    };
                default:
                    return new Holder<Item>() {
                        private TextView tva, tvb, tvc, tvd, tve, tvf;

                        @Override
                        protected void setData(Item node) {
                            tvf.setVisibility((node.f.equals("")) ? View.GONE : View.VISIBLE);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tva.getLayoutParams();
                            params.leftMargin = 60;
                            tva.setLayoutParams(params);
                            tva.setText(node.a);
                            tvb.setText(node.b);
                            if (node.c.substring(0, 1).equals("0")) {
                                tve.setTextColor(getResources().getColor(R.color.colorred));
                            } else {
                                tve.setTextColor(getResources().getColor(R.color.colorgreen));
                            }
                            tvc.setText(node.c.substring(1));
                            tvd.setText(node.d);

                            tve.setText(node.e);
                            tvf.setText(node.f);
                        }

                        @Override
                        protected View createConvertView(int position) {
                            View view = View.inflate(DetailActivity.this, R.layout.detail_type_b, null);
                            tva = (TextView) view.findViewById(R.id.b_detail_day);
                            tvb = (TextView) view.findViewById(R.id.b_detail_week);
                            tvc = (TextView) view.findViewById(R.id.b_detail_consumetype);
                            tvd = (TextView) view.findViewById(R.id.b_detail_time);
                            tve = (TextView) view.findViewById(R.id.b_detail_money);
                            tvf = (TextView) view.findViewById(R.id.b_detail_text);
                            return view;
                        }
                    };
            }
        }
    }

    private class Item extends Node<Item> {
        String a, b, c, d, e, f;

        Item(int id, int pId, int level, boolean isExpand, String a, String b, String c, String d, String e, String f) {
            super(id, pId, level, isExpand);
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private String LongToString(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy.MM月dd日HH:mmEE").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }

    public static String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(price);
        return format;
    }
}