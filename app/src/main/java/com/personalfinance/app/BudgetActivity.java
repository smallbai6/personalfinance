package com.personalfinance.app;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Budget.BudgetAdapter;
import com.personalfinance.app.Budget.BudgetClass;
import com.personalfinance.app.Budget.Budget_Caculator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BudgetActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Cursor cursor;
    private String Username;
    private Date date;
    private Intent intent;
    /*
    年季月日支出收入
     */
    private PopupWindow choosePopupWindow;
    private View contentView;
    private ListView chooselistView;
    private ArrayList<String> chooseList = new ArrayList<>();
    private ArrayAdapter<String> chooseAdapter;
    private String choose_ysmdString[] = new String[]{"本日", "本月", "本季", "本年"};
    private String choose_typeString[] = new String[]{"支出", "收入"};
    private int chooselevel = 0;//选择的时ysmd还是type
    private TextView back, choose_ysmd, choose_type,choose_genduo;
    private RelativeLayout layout_choose_type;
    private Drawable drawable;
    /*
    显示列表
     */
    private MyListView budgetListView;
    private List<BudgetClass> budgetList = new ArrayList<>();//显示的列表
    private BudgetAdapter budgetAdapter;
    private int typenumber = 0;//支出或收入时的类型个数
    private String zyc_String[] = new String[]{"支出", "余额", "超支", "收入", "待收", "超收"};
    private String consumetype[] = new String[11];//消费类型
    private double consumetype_money[] = new double[11];//各个消费类型的消费总金额

    private Budget_Caculator mCaculatorPop;
    /*
    总预算部分
     */
    private TextView totaltv, totaltva, totaltvb;
    private TextView totalyusuan, totala, totalb;
    private String totaltv_String[] = new String[]{"支出总预算", "收入总目标"};
    private String totaltva_String[] = new String[]{"已用", "已收"};
    private String totaltvb_String[] = new String[]{"可用", "超支", "待收", "超收"};
    /*
    同步
     */
    private ImageView refreshiv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
       /* cursor = db.query("userinfo", null, "User_Login=?",
                new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            Username = cursor.getString(cursor.getColumnIndex("User_Name"));
        } else {//没有登录用户时用户名就为请立即登录
            Username = "请立即登录";
        }*/
        intent=getIntent();
        Username=intent.getStringExtra("Username");
        date = new Date();
        budget_update();
        back = (TextView) findViewById(R.id.budget_back_button);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        back.setCompoundDrawables(drawable, null, null, null);
        refreshiv = (ImageView) findViewById(R.id.budget_total_refresh);//刷新图标
        choose_ysmd = (TextView) findViewById(R.id.budget_choose_yrmd); //年季月日
        choose_ysmd.setText(choose_ysmdString[0]);
        drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
        drawable2ysmd();
        choose_type = (TextView) findViewById(R.id.budget_choose_type); //支出收入
        choose_type.setText(choose_typeString[0]);

        choose_genduo=(TextView)findViewById(R.id.budget_choose_gengduo);
        drawable = getResources().getDrawable(R.mipmap.shangjiantou);
        drawable2type();//这个改成imageView在relativelayout的最右侧

        layout_choose_type = (RelativeLayout) findViewById(R.id.budget_type_relativeLayout);
        contentView = getLayoutInflater().inflate(R.layout.textlist, null);
        chooselistView = contentView.findViewById(R.id.textlist_View);
        chooseAdapter = new ArrayAdapter<>(BudgetActivity.this, android.R.layout.simple_list_item_1, chooseList);
        chooselistView.setAdapter(chooseAdapter);
        InitPopupWindow();
        back.setOnClickListener(this);
        choose_ysmd.setOnClickListener(this);
        refreshiv.setOnClickListener(this);
        layout_choose_type.setOnClickListener(this);
        budgetListView = (MyListView) findViewById(R.id.budget_listview);
        totaltv = (TextView) findViewById(R.id.budget_total_yusuan);
        totalyusuan = (TextView) findViewById(R.id.budget_total_yusuanmoney);
        drawable=getResources().getDrawable(R.mipmap.bianxietubiao);
        drawable.setBounds(0, 0, 40, 40);
        totalyusuan.setCompoundDrawables(null,null,drawable,null);
        totalyusuan.setCompoundDrawablePadding(30);
        totaltva = (TextView) findViewById(R.id.budget_total_tva);
        totala = (TextView) findViewById(R.id.budget_total_tvamoney);
        totaltvb = (TextView) findViewById(R.id.budget_total_tvb);
        totalb = (TextView) findViewById(R.id.budget_total_tvbmoney);
        totaltv.setText(totaltv_String[0]);
        totalyusuan.setText("0.00");
        totalyusuan.setOnClickListener(this);
        totaltva.setText(totaltva_String[0]);
        totala.setText("0.00");
        totaltvb.setText(totaltvb_String[0]);
        totalb.setText("0.00");
        show_budgetlist(choose_ysmd.getText().toString(), choose_type.getText().toString());
        total_budget(choose_ysmd.getText().toString(), choose_type.getText().toString());
        chooselistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (chooselevel == 0) {
                    //年季月日
                    choose_ysmd.setText(chooseList.get(position));
                } else if (chooselevel == 1) {
                    choose_type.setText(chooseList.get(position));
                }
                choosePopupWindow.dismiss();
                show_budgetlist(choose_ysmd.getText().toString(), choose_type.getText().toString());
                total_budget(choose_ysmd.getText().toString(), choose_type.getText().toString());
            }
        });
        choosePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (chooselevel == 0) {
                    drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
                    drawable2ysmd();
                } else if (chooselevel == 1) {
                    drawable = getResources().getDrawable(R.mipmap.shangjiantou);
                    drawable2type();
                }
            }
        });
        budgetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mCaculatorPop = new Budget_Caculator(BudgetActivity.this, chooselistView);
                mCaculatorPop.setOnCaculatorSetListener(new Budget_Caculator.OnCaculatorSetListener() {
                    @Override
                    public void OnCaculatorSet(String date) {
                        //budgetList列表的显示更新
                        if(Double.valueOf(date)>=0){
                            budgetlist_change(date, position);
                            budget_change(choose_ysmd.getText().toString(), choose_type.getText().toString(), consumetype[position], date);
                            total_budget(choose_ysmd.getText().toString(), choose_type.getText().toString());
                        }else{
                            Toast.makeText(BudgetActivity.this,"设置金额不能为负数",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.budget_back_button://点击返回键
                intent = new Intent(BudgetActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.budget_total_refresh://点击刷新，使得总预算和各个类型的预算和相同
                refresh_showDialog();
                break;
            case R.id.budget_choose_yrmd://年季月日
                chooselevel = 0;
                Initchooselist(chooselevel);
                choosePopupWindow.showAsDropDown(choose_ysmd);
                if (choosePopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
                    drawable2ysmd();
                }
                break;
            case R.id.budget_type_relativeLayout://支出收入
                chooselevel = 1;
                Initchooselist(chooselevel);
                choosePopupWindow.showAsDropDown(choose_type);
                if (choosePopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.mipmap.xiajiantou);
                    drawable2type();
                }
                break;
            case R.id.budget_total_yusuanmoney://点击总预算设置
                mCaculatorPop = new Budget_Caculator(BudgetActivity.this, chooselistView);
                mCaculatorPop.setOnCaculatorSetListener(new Budget_Caculator.OnCaculatorSetListener() {
                    @Override
                    public void OnCaculatorSet(String date) {
                        if(Double.valueOf(date)>=0){
                            totalbudget_change(choose_ysmd.getText().toString(), choose_type.getText().toString(), date);
                        }else{
                            Toast.makeText(BudgetActivity.this,"设置金额不能为负数",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void Initchooselist(int level) {
        //列表内容配置
        chooseList.clear();
        if (level == 0) {
            for (int i = 0; i < choose_ysmdString.length; i++) {
                chooseList.add(choose_ysmdString[i]);
            }
        } else if (level == 1) {
            for (int i = 0; i < choose_typeString.length; i++) {
                chooseList.add(choose_typeString[i]);
            }
        }
        chooseAdapter.notifyDataSetChanged();
        chooselistView.setSelection(0);
    }

    private void drawable2ysmd() {
        drawable.setBounds(0, 0, 30, 30);
        choose_ysmd.setCompoundDrawables(null, null, drawable, null);
        choose_ysmd.setCompoundDrawablePadding(10);
    }

    private void drawable2type() {
        drawable.setBounds(0, 0, 30, 30);
        choose_genduo.setCompoundDrawablePadding(10);
        choose_genduo.setCompoundDrawables(null,null,drawable,null);
    }

    private void InitPopupWindow() {
        choosePopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        choosePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        choosePopupWindow.setOutsideTouchable(true);
        choosePopupWindow.setTouchable(true);
        choosePopupWindow.setFocusable(true);
    }

    private void refresh_showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.picture);
        builder.setTitle("温馨提示");
        builder.setMessage("你确定要将各个类型的预算汇总为总预算么？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refresh_budget(choose_ysmd.getText().toString(), choose_type.getText().toString());
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

    /*
       检查数据库中budget将不符合的数据进行删除
        */
    private void budget_update() {
        int currentseason = season_judge(Integer.valueOf(getTimes(date).substring(5, 7)));
        for (int i = 0; i < choose_typeString.length; i++) {
            if (i == 0) {
                cursor = db.query("expendbudget", null, "User_Name=?"
                        , new String[]{Username}, null, null, null);
            } else if (i == 1) {
                cursor = db.query("incomebudget", null, "User_Name=?"
                        , new String[]{Username}, null, null, null);
            }
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex("DMSY")).equals(choose_ysmdString[0])) {
                        //本日
                        if (!LongToString(cursor.getLong(cursor.getColumnIndex("Time"))).substring(0, 11).
                                equals(getTimes(date).substring(0, 11))) {
                            delete_sqlite(cursor, i);
                        }

                    } else if (cursor.getString(cursor.getColumnIndex("DMSY")).equals(choose_ysmdString[1])) {
                        //本月
                        if (!LongToString(cursor.getLong(cursor.getColumnIndex("Time"))).substring(0, 8).
                                equals(getTimes(date).substring(0, 8))) {
                            delete_sqlite(cursor, i);
                        }
                    } else if (cursor.getString(cursor.getColumnIndex("DMSY")).equals(choose_ysmdString[2])) {
                        //本季
                        if (LongToString(cursor.getLong(cursor.getColumnIndex("Time"))).substring(0, 5).
                                equals(getTimes(date).substring(0, 5))) {
                            if (!(Integer.valueOf(getTimes(date).substring(5, 7)) <= (currentseason * 3)) &&
                                    !(Integer.valueOf(getTimes(date).substring(5, 7)) >= (currentseason * 3 - 2))) {
                                delete_sqlite(cursor, i);
                            }
                        }
                    } else if (cursor.getString(cursor.getColumnIndex("DMSY")).equals(choose_ysmdString[3])) {
                        //本年
                        if (!LongToString(cursor.getLong(cursor.getColumnIndex("Time"))).substring(0, 5).
                                equals(getTimes(date).substring(0, 5))) {
                            delete_sqlite(cursor, i);
                        }
                    }
                } while (cursor.moveToNext());
            }
        }
    }

    /*
    数据库中删除的内容budget_update
     */
    private void delete_sqlite(Cursor cursor, int iore) {
        if (iore == 0) {
            db.delete("expendbudget", "User_Name=? AND Type=? AND Money=?" +
                            "AND DMSY=? AND Time=?"
                    , new String[]{Username,
                            cursor.getString(cursor.getColumnIndex("Type")),
                            cursor.getString(cursor.getColumnIndex("Money")),
                            cursor.getString(cursor.getColumnIndex("DMSY")),
                            String.valueOf(cursor.getLong(cursor.getColumnIndex("Time")))});
        } else if (iore == 1) {
            db.delete("incomebudget", "User_Name=? AND Type=? AND Money=?" +
                            "AND DMSY=? AND Time=?"
                    , new String[]{Username,
                            cursor.getString(cursor.getColumnIndex("Type")),
                            cursor.getString(cursor.getColumnIndex("Money")),
                            cursor.getString(cursor.getColumnIndex("DMSY")),
                            String.valueOf(cursor.getLong(cursor.getColumnIndex("Time")))});
        }
    }

    private void budgetlist_change(String date, int position) {
        BudgetClass budgetClass;
        double resultmoney = Double.valueOf(date) - consumetype_money[position];
        if (choose_type.getText().toString().equals(choose_typeString[0])) {
            if (resultmoney >= 0) {
                budgetClass = new BudgetClass(consumetype[position],
                        date, zyc_String[1], formatPrice(resultmoney));
                //Log.d("jisuan", formatPrice(resultmoney) + "正");
            } else {
                budgetClass = new BudgetClass(consumetype[position],
                        date, zyc_String[2], formatPrice(resultmoney * (-1.0)));
                //  Log.d("jisuan", formatPrice(resultmoney*(-1.0)) + "反");
            }
            budgetList.set(position, budgetClass);
        }
        if (choose_type.getText().toString().equals(choose_typeString[1])) {
            if (resultmoney >= 0) {
                budgetClass = new BudgetClass(consumetype[position],
                        date, zyc_String[4], formatPrice(resultmoney));
                // Log.d("jisuan", formatPrice(resultmoney) + "正");
            } else {
                budgetClass = new BudgetClass(consumetype[position],
                        date, zyc_String[5], formatPrice(resultmoney * (-1.0)));
                //Log.d("jisuan", formatPrice(resultmoney) + "反");
            }
            budgetList.set(position, budgetClass);
        }
        budgetAdapter.notifyDataSetChanged();
    }

    /*
    各个类型预算进行设置更改,数据库的更改
     */
    private void budget_change(String ysmd, String iore, String type, String money) {
        //type消费类型  iore支出或收入 ysmd本年季月日
        if (iore.equals(choose_typeString[0])) {
            cursor = db.query("expendbudget",
                    null, "User_Name=? AND Type=?AND DMSY=?"
                    , new String[]{Username, type, ysmd}, null, null, null);
            ContentValues values = new ContentValues();
            if (cursor.moveToFirst()) {
                //进行数据的更改
                values.put("Money", money);
                values.put("Time", date.getTime());
                db.update("expendbudget", values,
                        "User_Name=? AND Type=? AND DMSY=?"
                        , new String[]{Username, type, ysmd});
            } else {
                //进行数据的添加
                values.put("User_Name", Username);
                values.put("Type", type);
                values.put("Money", money);
                values.put("DMSY", ysmd);
                values.put("Time", date.getTime());
                db.insert("expendbudget", null, values);
            }
        } else if (iore.equals(choose_typeString[1])) {
            cursor = db.query("incomebudget",
                    null, "User_Name=? AND Type=?AND DMSY=?"
                    , new String[]{Username, type, ysmd}, null, null, null);
            ContentValues values = new ContentValues();
            if (cursor.moveToFirst()) {
                //进行数据的更改
                values.put("Money", money);
                values.put("Time", date.getTime());
                db.update("incomebudget", values,
                        "User_Name=? AND Type=? AND DMSY=?"
                        , new String[]{Username, type, ysmd});
            } else {
                //进行数据的添加
                values.put("User_Name", Username);
                values.put("Type", type);
                values.put("Money", money);
                values.put("DMSY", ysmd);
                values.put("Time", date.getTime());
                db.insert("incomebudget", null, values);
            }
        }
    }

    /*
    进行将总预算更改为各个类型分预算之和
     */
    private void refresh_budget(String ysmd, String iore) {
        double totaltypemoney = 0;//各个类型支出总预算
        if (iore.equals(choose_typeString[0])) {
            cursor = db.query("expendbudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
        } else if (iore.equals(choose_typeString[1])) {
            cursor = db.query("incomebudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);

        }
        if (cursor.moveToFirst()) {
            do {
                if (!cursor.getString(cursor.getColumnIndex("Type")).equals("总预算")) {
                    totaltypemoney = totaltypemoney + Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                }
            } while (cursor.moveToNext());
        }
        totalbudget_show(iore, totaltypemoney);
    }

    /*
       总预算根据个各类类型预算和进行变化，如果没有总预算则进行总预算的insert
         */
    private void total_budget(String ysmd, String iore) {
        //首先查找数据库得到本日支出中符合各个类型预算的总值
        double totaltypemoney = 0;//各个类型的总预算金额
        double totalbudgetmoney = 0;//总预算金额
        //Log.d("shujuku","获得totaltypemoney="+totaltypemoney+"     totalbudgetmoney="+totalbudgetmoney);
        ContentValues values = new ContentValues();
        //Log.d("shujuku","进入total_budget");
        if (iore.equals(choose_typeString[0])) {
            cursor = db.query("expendbudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex("Type")).equals("总预算")) {
                        totalbudgetmoney = Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                    } else {
                        totaltypemoney = totaltypemoney + Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                    }
                } while (cursor.moveToNext());
            }
            //获得本日 支出中各个类型总预算的值  如果总预算金额设过则得到数据库中的总预算金额，反之仍为0
            cursor = db.query("expendbudget", null,
                    "User_Name=? AND Type=? AND DMSY=?"
                    , new String[]{Username, "总预算", ysmd}, null, null, null);
            if (totaltypemoney != 0) {//各个类型的预算中有已经设置的
                if (totaltypemoney >= totalbudgetmoney) {
                    //a>=b => b=a  数据库中内容更改    反之b不变
                    totalbudgetmoney = totaltypemoney;
                    if (cursor.moveToFirst()) {
                        // ContentValues values=new ContentValues();
                        values.put("Money", formatPrice(totalbudgetmoney));
                        values.put("Time", date.getTime());
                        db.update("expendbudget", values,
                                "User_Name=? AND Type=? AND DMSY=?"
                                , new String[]{Username, "总预算", ysmd});
                    } else {
                        // ContentValues values = new ContentValues();
                        values.put("User_Name", Username);
                        values.put("Type", "总预算");
                        values.put("Money", formatPrice(totalbudgetmoney));
                        values.put("DMSY", ysmd);
                        values.put("Time", date.getTime());
                        db.insert("expendbudget", null, values);
                    }
                }
            } else {//各个类型的预算中都没有设置过或者设置的值为0.00

                if (!cursor.moveToFirst()) {
                    // ContentValues values = new ContentValues();
                    values.put("User_Name", Username);
                    values.put("Type", "总预算");
                    values.put("Money", "0.00");
                    values.put("DMSY", ysmd);
                    values.put("Time", date.getTime());
                    db.insert("expendbudget", null, values);
                }
            }
        } else if (iore.equals(choose_typeString[1])) {
            cursor = db.query("incomebudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex("Type")).equals("总预算")) {
                        totalbudgetmoney = Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                    } else {
                        totaltypemoney = totaltypemoney + Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                    }
                } while (cursor.moveToNext());
            }
            //获得本日 支出中各个类型总预算的值  如果总预算金额设过则得到数据库中的总预算金额，反之仍为0
            cursor = db.query("incomebudget", null,
                    "User_Name=? AND Type=? AND DMSY=?"
                    , new String[]{Username, "总预算", ysmd}, null, null, null);
            if (totaltypemoney != 0) {//各个类型的预算中有已经设置的
                if (totaltypemoney >= totalbudgetmoney) {
                    //a>=b => b=a  数据库中内容更改    反之b不变
                    totalbudgetmoney = totaltypemoney;
                    if (cursor.moveToFirst()) {
                        //ContentValues values=new ContentValues();
                        values.put("Money", formatPrice(totalbudgetmoney));
                        values.put("Time", date.getTime());
                        db.update("incomebudget", values,
                                "User_Name=? AND Type=? AND DMSY=?"
                                , new String[]{Username, "总预算", ysmd});
                    } else {
                        //ContentValues values = new ContentValues();
                        values.put("User_Name", Username);
                        values.put("Type", "总预算");
                        values.put("Money", formatPrice(totalbudgetmoney));
                        values.put("DMSY", ysmd);
                        values.put("Time", date.getTime());
                        db.insert("incomebudget", null, values);
                    }
                }
            } else {//各个类型的预算中都没有设置过或者设置的值为0.00
                if (!cursor.moveToFirst()) {
                    //ContentValues values = new ContentValues();
                    values.put("User_Name", Username);
                    values.put("Type", "总预算");
                    values.put("Money", "0.00");
                    values.put("DMSY", ysmd);
                    values.put("Time", date.getTime());
                    db.insert("incomebudget", null, values);
                }
            }
        }
        totalbudget_show(iore, totalbudgetmoney);
    }

    /*
    点击总预算，进行总预算设置
     */
    private void totalbudget_change(String ysmd, String iore, String money) {
        double totaltypemoney = 0;
        if (iore.equals(choose_typeString[0])) {
            cursor = db.query("expendbudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (!cursor.getString(cursor.getColumnIndex("Type")).equals("总预算")) {
                        totaltypemoney = totaltypemoney + Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                    }
                } while (cursor.moveToNext());
            }
            if (Double.valueOf(money) >= totaltypemoney) {
                ContentValues values = new ContentValues();
                cursor = db.query("expendbudget", null,
                        "User_Name=? AND Type=? AND DMSY=?"
                        , new String[]{Username, "总预算", ysmd}, null, null, null);
                values.put("Money", money);
                values.put("Time", date.getTime());
                db.update("expendbudget", values,
                        "User_Name=? AND Type=? AND DMSY=?"
                        , new String[]{Username, "总预算", ysmd});
                totalbudget_show(iore, Double.valueOf(money));
            }else{
                Toast.makeText(BudgetActivity.this,"总预算不能小于各个类型预算之和！",Toast.LENGTH_SHORT).show();
            }
        } else if (iore.equals(choose_typeString[1])) {
            cursor = db.query("incomebudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (!cursor.getString(cursor.getColumnIndex("Type")).equals("总预算")) {
                        totaltypemoney = totaltypemoney + Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                    }
                } while (cursor.moveToNext());
            }
            if (Double.valueOf(money) >= totaltypemoney) {
                ContentValues values = new ContentValues();
                cursor = db.query("incomebudget", null,
                        "User_Name=? AND Type=? AND DMSY=?"
                        , new String[]{Username, "总预算", ysmd}, null, null, null);
                values.put("Money", money);
                values.put("Time", date.getTime());
                db.update("incomebudget", values,
                        "User_Name=? AND Type=? AND DMSY=?"
                        , new String[]{Username, "总预算", ysmd});
                totalbudget_show(iore, Double.valueOf(money));
            }
            else{
                Toast.makeText(BudgetActivity.this,"总预算不能小于各个类型预算之和！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    在total_budget和totalbudget_change中数据库更改完毕后进行显示的更改
     */
    private void totalbudget_show(String iore, double totalbudgetmoney) {//预算  各个消费预算和
        if (iore.equals(choose_typeString[0])) {//支出
            totaltv.setText(totaltv_String[0]);
            totaltva.setText(totaltva_String[0]);
        } else if (iore.equals(choose_typeString[1])) {//收入
            totaltv.setText(totaltv_String[1]);
            totaltva.setText(totaltva_String[1]);
        }
        totalyusuan.setText(formatPrice(totalbudgetmoney));
        double totalconsumemoney = 0;//各个消费类型消费金额的总和
        for (int i = 0; i < typenumber; i++) {
            totalconsumemoney += consumetype_money[i];
        }
        totala.setText(formatPrice(totalconsumemoney));
        //总预算-已用金额
        if ((totalbudgetmoney - totalconsumemoney) >= 0) {
            if (choose_type.getText().toString().equals(choose_typeString[0])) {
                totaltvb.setText(totaltvb_String[0]);
            } else {
                totaltvb.setText(totaltvb_String[2]);
            }
            totalb.setText(formatPrice(totalbudgetmoney - totalconsumemoney));
        } else {
            if (choose_type.getText().toString().equals(choose_typeString[0])) {
                totaltvb.setText(totaltvb_String[1]);
            } else {
                totaltvb.setText(totaltvb_String[3]);
            }
            totalb.setText(formatPrice(totalconsumemoney - totalbudgetmoney));
        }
    }


    private void show_budgetlist(String ysmd, String iore) {
        get_type(iore);
        Result_typemoney(ysmd, iore);
        Result_budgetlist(ysmd, iore);
    }

    /*
    获得消费类型;
     */
    private void get_type(String iore) {//得到消费类型支出 收入
        typenumber = 0;
        if (iore.equals(choose_typeString[0])) {//支出
            cursor = db.query("expendtype", null, null,
                    null, null, null, null);
        } else if (iore.equals(choose_typeString[1])) {//收入
            cursor = db.query("incometype", null, null,
                    null, null, null, null);
        }
        if (cursor.moveToFirst()) {
            do {
                consumetype[typenumber] = cursor.getString(cursor.getColumnIndex("Type_Name"));
                typenumber++;
            } while (cursor.moveToNext());
        }
    }

    /*
    计算各个消费类型的总金额
     */
    private void Result_typemoney(String ysmd, String iore) {
        //首先初始化各个消费类型的总金额
        for (int i = 0; i < typenumber; i++) {
            consumetype_money[i] = 0.00;
        }
        int currentseason = season_judge(Integer.valueOf(getTimes(date).substring(5, 7)));
        //确定支出或者收入中各消费类型的消费金额
        if (iore.equals(choose_typeString[0])) {//支出
            cursor = db.query("expendinfo", null, "User_Name=?"
                    , new String[]{Username}, null, null, null);
            // Log.d("budgetactivity.liang", "expendinfo的数据");
        }
        else if (iore.equals(choose_typeString[1])) {//收入
            cursor = db.query("incomeinfo", null, "User_Name=?"
                    , new String[]{Username}, null, null, null);
        }
        if (cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(cursor.getColumnIndex("Time"));
                if (ysmd.equals(choose_ysmdString[0]) && LongToString(time).substring(0, 11).equals(getTimes(date).substring(0, 11))) {
                    //本日 并且时间对上了
                    for (int i = 0; i < typenumber; i++) {
                        if (cursor.getString(cursor.getColumnIndex("Type")).equals(consumetype[i])) {
                            //消费类型相同
                            consumetype_money[i] = consumetype_money[i] +
                                    Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                            break;
                        }
                    }
                } else if (ysmd.equals(choose_ysmdString[1]) && LongToString(time).substring(0, 8).equals(getTimes(date).substring(0, 8))) {
                    //本月 并且时间对上了
                    for (int i = 0; i < typenumber; i++) {
                        if (cursor.getString(cursor.getColumnIndex("Type")).equals(consumetype[i])) {
                            //消费类型相同
                            consumetype_money[i] = consumetype_money[i] +
                                    Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                            break;
                        }
                    }
                } else if (ysmd.equals(choose_ysmdString[2]) && LongToString(time).substring(0, 5).equals(getTimes(date).substring(0, 5))) {
                    //本季 并且时间对上了
                    if ((Integer.valueOf(LongToString(time).substring(5, 7)) <= (currentseason * 3)) &&
                            (Integer.valueOf(LongToString(time).substring(5, 7)) >= (currentseason * 3 - 2))) {
                        for (int i = 0; i < typenumber; i++) {
                            if (cursor.getString(cursor.getColumnIndex("Type")).equals(consumetype[i])) {
                                //消费类型相同
                                consumetype_money[i] = consumetype_money[i] +
                                        Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                                break;
                            }
                        }
                    }
                } else if (ysmd.equals(choose_ysmdString[3]) && LongToString(time).substring(0, 5).equals(getTimes(date).substring(0, 5))) {
                    //本年 并且时间对上了
                    for (int i = 0; i < typenumber; i++) {
                        if (cursor.getString(cursor.getColumnIndex("Type")).equals(consumetype[i])) {
                            //消费类型相同
                            consumetype_money[i] = consumetype_money[i] +
                                    Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }// 消费类型中的总消费金额确定
    }

    /*
    初始化显示的budgetList列表
     */
    private void Init_budgetList(String iore) {//初始化显示的budgetList列表 为支出
        budgetList.clear();
        BudgetClass budgetClass;
        if (iore.equals(choose_typeString[0])) {
            for (int i = 0; i < typenumber; i++) {
                if (consumetype_money[i] == 0) {
                    budgetClass = new BudgetClass(consumetype[i], "未设置", zyc_String[0], formatPrice(consumetype_money[i]));
                } else {
                    budgetClass = new BudgetClass(consumetype[i], "未设置", zyc_String[2], formatPrice(consumetype_money[i]));
                }
                budgetList.add(budgetClass);
            }
        } else if (iore.equals(choose_typeString[1])) {
            for (int i = 0; i < typenumber; i++) {
                if (consumetype_money[i] == 0) {
                    budgetClass = new BudgetClass(consumetype[i], "未设置", zyc_String[3], formatPrice(consumetype_money[i]));
                } else {
                    budgetClass = new BudgetClass(consumetype[i], "未设置", zyc_String[5], formatPrice(consumetype_money[i]));
                }
                budgetList.add(budgetClass);
            }
        }

    }

    /*
    根据数据库对budgetlist进行更改
     */
    private void Result_budgetlist(String ysmd, String iore) {
        Init_budgetList(iore);
        if (iore.equals(choose_typeString[0])) {//支出
            cursor = db.query("expendbudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
        } else if (iore.equals(choose_typeString[1])) {
            cursor = db.query("incomebudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
        }
        if (cursor.moveToFirst()) {//数据库中含有数据
            do {
                //找寻对应的数据
                for (int i = 0; i < budgetList.size(); i++) {
                    if (cursor.getString(cursor.getColumnIndex("Type")).
                            equals(budgetList.get(i).getType())) {
                        //更新数据
                        double resultmoney = Double.valueOf(cursor.getString(
                                cursor.getColumnIndex("Money"))) - consumetype_money[i];
                        BudgetClass budgetClass;
                        if (iore.equals(choose_typeString[0])) {//支出
                            if (resultmoney >= 0) {//余额
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[1], formatPrice(resultmoney));
                            } else {//超支
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[2], formatPrice(resultmoney * (-1.0)));
                            }
                            budgetList.set(i, budgetClass);
                        }
                        else if (iore.equals(choose_typeString[1])) {
                            if (resultmoney >= 0) {//待收
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[4], formatPrice(resultmoney));
                            } else {//超收
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[5], formatPrice(resultmoney * (-1.0)));
                            }
                            budgetList.set(i, budgetClass);
                        }

                        break;
                    }
                }
            } while (cursor.moveToNext());
        }
        /*if (iore.equals(choose_typeString[0])) {//支出
            cursor = db.query("expendbudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
            if (cursor.moveToFirst()) {//数据库中含有数据
                do {
                    //找寻对应的数据
                    for (int i = 0; i < budgetList.size(); i++) {
                        if (cursor.getString(cursor.getColumnIndex("Type")).
                                equals(budgetList.get(i).getType())) {
                            //更新数据
                            double resultmoney = Double.valueOf(cursor.getString(
                                    cursor.getColumnIndex("Money"))) - consumetype_money[i];
                            BudgetClass budgetClass;
                            if (resultmoney >= 0) {//余额
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[1], formatPrice(resultmoney));
                            } else {//超支
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[2], formatPrice(resultmoney * (-1.0)));
                            }
                            budgetList.set(i, budgetClass);
                            break;
                        }
                    }
                } while (cursor.moveToNext());
            }
        }
        else if (iore.equals(choose_typeString[1])) {
            cursor = db.query("incomebudget", null, "User_Name=? AND DMSY=?"
                    , new String[]{Username, ysmd}, null, null, null);
            if (cursor.moveToFirst()) {//数据库中含有数据
                do {
                    //找寻对应的数据
                    for (int i = 0; i < budgetList.size(); i++) {
                        if (cursor.getString(cursor.getColumnIndex("Type")).
                                equals(budgetList.get(i).getType())) {
                            //更新数据
                            double resultmoney = Double.valueOf(cursor.getString(
                                    cursor.getColumnIndex("Money"))) - consumetype_money[i];
                            BudgetClass budgetClass;
                            if (resultmoney >= 0) {//待收
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[4], formatPrice(resultmoney));
                            } else {//超收
                                budgetClass = new BudgetClass(consumetype[i],
                                        cursor.getString(cursor.getColumnIndex("Money")),
                                        zyc_String[5], formatPrice(resultmoney * (-1.0)));
                            }
                            budgetList.set(i, budgetClass);
                            break;
                        }
                    }
                } while (cursor.moveToNext());
            }
        }*/
        budgetAdapter = new BudgetAdapter(BudgetActivity.this,
                R.layout.budget_type, budgetList);
        budgetListView.setAdapter(budgetAdapter);
    }

    /*
    季节判断
     */
    private int season_judge(int month) {
        int currentseason = 0;
        switch (month) {
            case 1:
            case 2:
            case 3:
                //第一季
                currentseason = 1;
                break;
            case 4:
            case 5:
            case 6:
                currentseason = 2;
                break;
            case 7:
            case 8:
            case 9:
                currentseason = 3;
                break;
            case 10:
            case 11:
            case 12:
                currentseason = 4;
                break;
            default:
                break;
        }
        return currentseason;
    }
    /*
     *获取系统时间
     */
    private String getTimes(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        return format.format(date);
    }

    /*
     *时间数据类型转换
     */
    private String LongToString(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }

    public static String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(price);
        return format;
    }
}
