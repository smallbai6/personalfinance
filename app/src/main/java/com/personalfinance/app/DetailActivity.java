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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.personalfinance.app.Detail.DetailAdapter;
import com.personalfinance.app.Detail.DetailInfo;
import com.personalfinance.app.Detail.DetailNodeData;
import com.personalfinance.app.Detail.DetailSurplus;
import com.personalfinance.app.Detail.Detail_Activity.DetailEditorActivity;
import com.personalfinance.app.Detail.Detail_Activity.TallyEditorActivity;
import com.personalfinance.app.Detail.Node;
import com.personalfinance.app.Detail.OnInnerItemClickListener;
import com.personalfinance.app.Detail.OnInnerItemLongClickListener;

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
    /*
    按键
     */
    private TextView backbutton, choosermd;
    private ImageView addtally;
    /*
    年结余
     */
    private TextView detail_year_money, detail_year_incomemoney, detail_year_expendmoney;
    /*
    年份和季月天选择
     */
    PopupWindow choosePopupWindow;
    private View contentView;
    private ListView chooselistView;
    private List<String> yrmdchooseList = new ArrayList<>();
    private ArrayAdapter<String> yrmdadapter;

    private String[] yearString = {"上一年", "下一年","批量删除"};
    private String[] rmdString = {"季", "月", "日"};
    private Drawable drawable;//按键旁的图标显示
    private int choosetype;//选择类型是yearString 还是rmdString
    private ImageView choosey;//点击进行上一年 下一年选择
    private Calendar calendar = Calendar.getInstance();//获得当前时间
    private int showyear;//当前需要显示时间

    /*
    总列表和各个等级排序列表
     */
    private MyListView listView;
    //private List<Item> list = new ArrayList<>();
    private List<Node> list = new ArrayList<>();
    //private boolean[] listisExpand = new boolean[10000];
    // private MyAdapter adapter;
    private DetailAdapter adapter;
    private List<DetailInfo> InfoList = new ArrayList<>();
    private List<DetailSurplus> day_dayList = new ArrayList<>();
    private List<DetailSurplus> day_monthList = new ArrayList<>();
    private List<DetailSurplus> month_monthList = new ArrayList<>();
    private List<DetailSurplus> season_seasonList = new ArrayList<>();
    /*

     */
    DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        listView = (MyListView) findViewById(R.id.detail_listview);//流水列表

        backbutton = (TextView) findViewById(R.id.detail_back_button);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        backbutton.setCompoundDrawables(drawable, null, null, null);
        showyear = calendar.get(Calendar.YEAR);
        backbutton.setText(showyear + "年");

        choosermd = (TextView) findViewById(R.id.detail_choose_rmd);//选择季月
        choosermd.setText(rmdString[0]);
        drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
        drawable2tubiao();

        addtally = (ImageView) findViewById(R.id.detail_year_tianjia);

        choosey = (ImageView) findViewById(R.id.detail_year_shengluetubiao);
        detail_year_money = (TextView) findViewById(R.id.detail_year_money);
        detail_year_incomemoney = (TextView) findViewById(R.id.detail_year_incomemoney);
        detail_year_expendmoney = (TextView) findViewById(R.id.detail_year_expendmoney);

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
                    }else if(position==2){//批量删除
                        intent = new Intent(DetailActivity.this, DetailEditorActivity.class);
                        intent.putExtra("username", Username);
                        intent.putExtra("year", showyear);
                        startActivityForResult(intent, 3);
                    }
                    backbutton.setText(showyear + "年");

                } else if (choosetype == 1) {//点击的是季月日
                    choosermd.setText(yrmdchooseList.get(position));
                }
               /* for (int i = 0; i < list.size(); i++) {
                    listisExpand[i] = false;
                }*/
                //不管点击的是什么，都进行列表重建
                if (choosermd.getText().equals(rmdString[0])) {
                    detailseason_list();
                } else if (choosermd.getText().equals(rmdString[1])) {
                    detailmonth_list();
                } else if (choosermd.getText().equals(rmdString[2])) {
                    detailday_list();
                }
                choosePopupWindow.dismiss();
            }
        });
        choosePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (choosetype == 1) {
                    drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
                    drawable2tubiao();
                }
            }
        });
        backbutton.setOnClickListener(this);
        choosermd.setOnClickListener(this);
        choosey.setOnClickListener(this);
        addtally.setOnClickListener(this);

        cursor = db.query("userinfo", null, "User_Login=?", new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            Username = cursor.getString(cursor.getColumnIndex("User_Name"));
        } else {//没有登录用户时用户名就为请立即登录
            Username = "请立即登录";
        }
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

    private void Init2yrmdchooselist(int choosetype) {
        //列表内容配置
        yrmdchooseList.clear();
        if (choosetype == 0) {
            for (int i = 0; i < yearString.length; i++) {
                yrmdchooseList.add(yearString[i]);
            }
        } else if (choosetype == 1) {
            for (int i = 0; i < rmdString.length; i++) {
                yrmdchooseList.add(rmdString[i]);
            }
        }
        adapter.notifyDataSetChanged();
        chooselistView.setSelection(0);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_back_button://返回主活动
                intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.detail_year_shengluetubiao://点击上一年下一年的省略图标
                choosetype = 0;
                Init2yrmdchooselist(choosetype);
                choosePopupWindow.showAsDropDown(choosey);
                break;
            case R.id.detail_choose_rmd://选择季月日
                choosetype = 1;
                Init2yrmdchooselist(choosetype);
                choosePopupWindow.showAsDropDown(choosermd);
                if (choosePopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
                    drawable2tubiao();
                }
                break;

            case R.id.detail_year_tianjia://添加数据
                intent = new Intent(DetailActivity.this, TallyActivity.class);
                intent.putExtra("HuoDong", "DetailActivity.java");
                startActivityForResult(intent, 1);//requestCode=1为进入TallyActivity.class中
                //startActivity(intent);
                //finish();
        }
    }

    private void list_adapter() {
        adapter = new DetailAdapter(listView, this, list,
                2, R.mipmap.shangjiantou, R.mipmap.xiajiantou);
        listView.setAdapter(adapter);
         year_show();
        adapter.setOnInnerItemClickListener(new OnInnerItemClickListener() {
            @Override
            public void onClick(Node node, int position) {
                Log.d("onClick"," position    "+position);
                DetailNodeData detailNodeData = (DetailNodeData) node.getData();
                Toast.makeText(DetailActivity.this, "短点  " + detailNodeData.getC(), Toast.LENGTH_SHORT).show();
                intent = new Intent(DetailActivity.this, TallyEditorActivity.class);
                intent.putExtra("username", Username);
                intent.putExtra("money", detailNodeData.getE());
                intent.putExtra("type", detailNodeData.getC());
                intent.putExtra("time", detailNodeData.getTime());
                intent.putExtra("message", detailNodeData.getF());
                startActivityForResult(intent, 2);
            }
        });
        adapter.setOnInnerItemLongClickListener(new OnInnerItemLongClickListener() {
            @Override
            public void onClick(Node node, int position) {
                DetailNodeData detailNodeData = (DetailNodeData) node.getData();
                Toast.makeText(DetailActivity.this, "长点  " + detailNodeData.getC(), Toast.LENGTH_SHORT).show();
                intent = new Intent(DetailActivity.this, DetailEditorActivity.class);
                intent.putExtra("username", Username);
                intent.putExtra("year", showyear);
                startActivityForResult(intent, 3);
                //时间传去
            }
        });
    }
    private void year_show(){
        double totalyearmoney = 0;
        double totalyearexpendmoney = 0;
        double totalyeaerincomemoney = 0;
        for(Node node:list){
            if(node.isRootNode()){
                //为根节点
                DetailNodeData detailNodeData =(DetailNodeData)node.getData();
                totalyearmoney+=Double.valueOf(detailNodeData.getC());
                totalyeaerincomemoney+=Double.valueOf(detailNodeData.getD());
                totalyearexpendmoney+=Double.valueOf(detailNodeData.getE());
            }
        }
        detail_year_money.setText(formatPrice(totalyearmoney));
        detail_year_expendmoney.setText(formatPrice(totalyearexpendmoney));
        detail_year_incomemoney.setText(formatPrice(totalyeaerincomemoney));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1://添加数据
                int issave = data.getIntExtra("issave", -1);
                if ((resultCode == RESULT_OK) || (issave != 0)) {
                    if (choosermd.getText().equals(rmdString[0])) {//季
                        detailseason_list();
                    } else if (choosermd.getText().equals(rmdString[1])) {//月
                        detailmonth_list();
                    } else if (choosermd.getText().equals(rmdString[2])) {//日
                        detailday_list();
                    }
                    list_adapter();
                }
                break;
            case 2://编辑数据
                if (resultCode == RESULT_OK) {
                    if (choosermd.getText().equals(rmdString[0])) {//季
                        detailseason_list();
                    } else if (choosermd.getText().equals(rmdString[1])) {//月
                        detailmonth_list();
                    } else if (choosermd.getText().equals(rmdString[2])) {//日
                        detailday_list();
                    }
                    list_adapter();
                }
                break;
            case 3://批量编辑
                int isdelete = intent.getIntExtra("isdelete", -1);
                if (resultCode == RESULT_OK) {//返回了
                    if (isdelete != 0) {
                        if (choosermd.getText().equals(rmdString[0])) {//季
                            detailseason_list();
                        } else if (choosermd.getText().equals(rmdString[1])) {//月
                            detailmonth_list();
                        } else if (choosermd.getText().equals(rmdString[2])) {//日
                            detailday_list();
                        }
                        list_adapter();
                    }
                }
                break;
            default:
                break;
        }
    }
    /*
    日详情列表
     */
    private void detail_DetailInfo(String currentyear) {
        Log.d("DetailActivity.liang", "进入detail_DetailInfo currentyear==  " + currentyear);
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

       /* for (DetailSurplus detailSurplus : day_dayList) {
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }*/
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
        //总结出年
      /*  double totalyearmoney = 0;
        double totalyearexpendmoney = 0;
        double totalyeaerincomemoney = 0;
        for (DetailSurplus detailSurplus : day_monthList) {
            totalyearmoney = totalyearmoney + detailSurplus.getJieyu();
            totalyearexpendmoney = totalyearexpendmoney + detailSurplus.getZhichu();
            totalyeaerincomemoney = totalyeaerincomemoney + detailSurplus.getShouru();
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }
        detail_year_money.setText(formatPrice(totalyearmoney));
        detail_year_expendmoney.setText(formatPrice(totalyearexpendmoney));
        detail_year_incomemoney.setText(formatPrice(totalyeaerincomemoney));
        for (DetailSurplus detailSurplus : day_monthList) {
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }*/
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
        //总结出年
        /* double totalyearmoney = 0;
        double totalyearexpendmoney = 0;
        double totalyeaerincomemoney = 0;
        for (DetailSurplus detailSurplus : month_monthList) {
            totalyearmoney = totalyearmoney + detailSurplus.getJieyu();
            totalyearexpendmoney = totalyearexpendmoney + detailSurplus.getZhichu();
            totalyeaerincomemoney = totalyeaerincomemoney + detailSurplus.getShouru();
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }
        detail_year_money.setText(formatPrice(totalyearmoney));
        detail_year_expendmoney.setText(formatPrice(totalyearexpendmoney));
        detail_year_incomemoney.setText(formatPrice(totalyeaerincomemoney));
       for (DetailSurplus detailSurplus : month_monthList) {
            Log.d("DetailActivity.liang", detailSurplus.getDay() + "  " + detailSurplus.getDate()
                    + "  " + detailSurplus.getJieyu() + "   " + detailSurplus.getShouru() + "      " +
                    detailSurplus.getZhichu() + "     " + LongToString(detailSurplus.getTime()));
        }*/
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
        DetailNodeData detailNodeData;
        int a, b = 0, c = 0;//a为季，b为月，c为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01, leveldivide12;//等级划分关联
        for (a = 0; a < season_seasonList.size(); a++) {
            /*list.add(new Item(i, 0, 0, listisExpand[i],
                    season_seasonList.get(a).getDay(),
                    season_seasonList.get(a).getDate(),
                    formatPrice(season_seasonList.get(a).getJieyu()),
                    formatPrice(season_seasonList.get(a).getShouru()),
                    formatPrice(season_seasonList.get(a).getZhichu()),
                    "",
                    season_seasonList.get(a).getTime()));*/
            detailNodeData = new DetailNodeData(season_seasonList.get(a).getDay(), season_seasonList.get(a).getDate(),
                    formatPrice(season_seasonList.get(a).getJieyu()), formatPrice(season_seasonList.get(a).getShouru()),
                    formatPrice(season_seasonList.get(a).getZhichu()), "",
                    season_seasonList.get(a).getTime());
            list.add(new Node<DetailNodeData>(i + "", "-1", detailNodeData, "0"));
            leveldivide01 = i;
            i++;
            int ji = Integer.valueOf(season_seasonList.get(a).getDay().substring(0, 1));//     .equals(day_dayList.get(b).getDate().substring(5, 7) + "月")
            while ((Integer.valueOf(month_monthList.get(b).getDay().substring(0, 2)) <= (ji * 3))
                    && (Integer.valueOf(month_monthList.get(b).getDay().substring(0, 2)) >= (ji * 3 - 2))) {
               /* list.add(new Item(i, leveldivide01, 1, listisExpand[i],
                        month_monthList.get(b).getDay(),
                        month_monthList.get(b).getDate(),
                        formatPrice(month_monthList.get(b).getJieyu()),
                        formatPrice(month_monthList.get(b).getShouru()),
                        formatPrice(month_monthList.get(b).getZhichu()),
                        "",
                        month_monthList.get(b).getTime()));*/
                detailNodeData = new DetailNodeData(month_monthList.get(b).getDay(), month_monthList.get(b).getDate(),
                        formatPrice(month_monthList.get(b).getJieyu()), formatPrice(month_monthList.get(b).getShouru()),
                        formatPrice(month_monthList.get(b).getZhichu()), "",
                        month_monthList.get(b).getTime());
                list.add(new Node<DetailNodeData>(i + "", leveldivide01 + "", detailNodeData, "0"));
                leveldivide12 = i;
                i++;
                while (month_monthList.get(b).getDay().equals(LongToString(InfoList.get(c).getTime()).substring(5, 8))) {
                   /* list.add(new Item(i, leveldivide12, 2, listisExpand[i],
                            LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(16, 18),
                            InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(11, 16),
                            formatPrice(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText(),
                            InfoList.get(c).getTime()));*/
                    detailNodeData = new DetailNodeData(LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(16, 18), InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(11, 16), formatPrice(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText(), InfoList.get(c).getTime());
                    list.add(new Node<DetailNodeData>(i + "", leveldivide12 + "", detailNodeData, "1"));
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
        list_adapter();
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
        DetailNodeData detailNodeData;
        int a, b = 0, c = 0;//a为月，b为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01;//等级划分关联
        for (a = 0; a < month_monthList.size(); a++) {
          /*  list.add(new Item(i, 0, 0, listisExpand[i],
                    month_monthList.get(a).getDay(),
                    month_monthList.get(a).getDate(),
                    formatPrice(month_monthList.get(a).getJieyu()),
                    formatPrice(month_monthList.get(a).getShouru()),
                    formatPrice(month_monthList.get(a).getZhichu()),
                    "",
                    month_monthList.get(a).getTime()));*/
            detailNodeData = new DetailNodeData(month_monthList.get(a).getDay(), month_monthList.get(a).getDate(),
                    formatPrice(month_monthList.get(a).getJieyu()), formatPrice(month_monthList.get(a).getShouru()),
                    formatPrice(month_monthList.get(a).getZhichu()), "",
                    month_monthList.get(a).getTime());
            list.add(new Node<DetailNodeData>(i + "", "-1", detailNodeData, "0"));
            leveldivide01 = i;
            i++;
            while (month_monthList.get(a).getDay().equals(LongToString(InfoList.get(b).getTime()).substring(5, 8))) {
               /* list.add(new Item(i, leveldivide01, 1, listisExpand[i],
                        LongToString(InfoList.get(b).getTime()).substring(8, 10),
                        LongToString(InfoList.get(b).getTime()).substring(16, 18),
                        InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(11, 16),
                        formatPrice(InfoList.get(b).getMoney()),
                        InfoList.get(b).getText(),
                        InfoList.get(b).getTime()));*/
                detailNodeData = new DetailNodeData(LongToString(InfoList.get(b).getTime()).substring(8, 10),
                        LongToString(InfoList.get(b).getTime()).substring(16, 18), InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(11, 16), formatPrice(InfoList.get(b).getMoney()),
                        InfoList.get(b).getText(), InfoList.get(b).getTime());
                list.add(new Node<DetailNodeData>(i + "", leveldivide01 + "", detailNodeData, "1"));
                i++;
                b++;
                if (b >= InfoList.size()) {
                    break;
                }
            }
        }
        list_adapter();
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

        DetailNodeData detailNodeData;
        int a, b = 0, c = 0;//a为月，b为日，c为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01, leveldivide12;//等级划分关联listisExpand[i]
        for (a = 0; a < day_monthList.size(); a++) {
           /* list.add(new Item(i, 0, 0, listisExpand[i],
                    day_monthList.get(a).getDay(),
                    day_monthList.get(a).getDate(),
                    formatPrice(day_monthList.get(a).getJieyu()),
                    formatPrice(day_monthList.get(a).getShouru()),
                    formatPrice(day_monthList.get(a).getZhichu()),
                    "",
                    day_monthList.get(a).getTime()
            ));*/
            detailNodeData = new DetailNodeData(day_monthList.get(a).getDay(), day_monthList.get(a).getDate(),
                    formatPrice(day_monthList.get(a).getJieyu()), formatPrice(day_monthList.get(a).getShouru()),
                    formatPrice(day_monthList.get(a).getZhichu()), "",
                    day_monthList.get(a).getTime());
            list.add(new Node<DetailNodeData>(i + "", "-1", detailNodeData, "0"));
            leveldivide01 = i;
            i++;

            while (day_monthList.get(a).getDay().equals(day_dayList.get(b).getDate().substring(5, 7) + "月")) {
                /*list.add(new Item(i, leveldivide01, 1, listisExpand[i],
                        day_dayList.get(b).getDay(),
                        day_dayList.get(b).getDate(),
                        formatPrice(day_dayList.get(b).getJieyu()),
                        formatPrice(day_dayList.get(b).getShouru()),
                        formatPrice(day_dayList.get(b).getZhichu()),
                        "",
                        day_dayList.get(b).getTime()
                ));*/
                detailNodeData = new DetailNodeData(day_dayList.get(b).getDay(), day_dayList.get(b).getDate(),
                        formatPrice(day_dayList.get(b).getJieyu()), formatPrice(day_dayList.get(b).getShouru()),
                        formatPrice(day_dayList.get(b).getZhichu()), "",
                        day_dayList.get(b).getTime());
                list.add(new Node<DetailNodeData>(i + "", leveldivide01 + "", detailNodeData, "0"));
                leveldivide12 = i;
                i++;
                while (LongToString(day_dayList.get(b).getTime()).substring(0, 11).equals(
                        LongToString(InfoList.get(c).getTime()).substring(0, 11))) {
                   /* list.add(new Item(i, leveldivide12, 2, listisExpand[i],
                            LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(16, 18),
                            InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(11, 16),
                            formatPrice(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText(),
                            InfoList.get(c).getTime()));*/
                    detailNodeData = new DetailNodeData(LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(16, 18),
                            InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(11, 16),
                            formatPrice(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText(),
                            InfoList.get(c).getTime());
                    list.add(new Node<DetailNodeData>(i + "", leveldivide12 + "", detailNodeData, "1"));
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
        list_adapter();

    }


    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public String LongToString(long date) {
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
