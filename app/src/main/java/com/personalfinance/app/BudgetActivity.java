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
import android.os.Handler;
import android.os.Message;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Budget.BudgetAdapter;
import com.personalfinance.app.Budget.BudgetClass;
import com.personalfinance.app.Budget.Budget_Caculator;
import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Util.DataFormatUtil;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BudgetActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
   // final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
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
    private ImageView backimage;
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

    private final static int totalbudget_show = 1;
    private final static int show_budgetlist = 2;
    private final static int budgetlist_change = 3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case totalbudget_show:
                    totalbudget_show(msg.getData().getString("iore"),
                            msg.getData().getDouble("totalbudgetmoney"));
                    break;
                case show_budgetlist:
                    Log.d("TAG", "handler->show_budgetlist进行列表适配");
                    budgetAdapter = new BudgetAdapter(BudgetActivity.this,
                            R.layout.budget_type, budgetList);
                    budgetListView.setAdapter(budgetAdapter);
                    Log.d("TAG", "handler->setAdapter");
                    budgetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            mCaculatorPop = new Budget_Caculator(BudgetActivity.this, chooselistView);
                            mCaculatorPop.setOnCaculatorSetListener(new Budget_Caculator.OnCaculatorSetListener() {
                                @Override
                                public void OnCaculatorSet(int Sort,String date) {
                                    Log.d("TAG", "OnCaculatorSet");
                                    //budgetList列表的显示更新
                                    if(!date.equals("")) {
                                        if (Double.valueOf(date) >= 0) {
                                            budgetlist_change(DataFormatUtil.formatPrice(Double.valueOf(date)), position);
                                            budget_change(choose_ysmd.getText().toString(), choose_type.getText().toString(), consumetype[position], DataFormatUtil.formatPrice(Double.valueOf(date)));
                                            total_budget(choose_ysmd.getText().toString(), choose_type.getText().toString());
                                        } else {
                                            Toast.makeText(BudgetActivity.this, "设置金额不能为负数", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    });
                    break;
                case budgetlist_change:
                    budgetAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget);
        intent=getIntent();
        Username=intent.getStringExtra("Username");
        date = new Date();
        back = (TextView) findViewById(R.id.budget_back_button);
        backimage=(ImageView)findViewById(R.id.budget_back_backimageview);
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
        backimage.setOnClickListener(this);
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
        budget_update();
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
                    public void OnCaculatorSet(int Sort,String date) {
                        if(!date.equals("")) {
                        if(Double.valueOf(date)>=0){
                            totalbudget_change(choose_ysmd.getText().toString(), choose_type.getText().toString(), DataFormatUtil.formatPrice(Double.valueOf(date)));
                        }else{
                            Toast.makeText(BudgetActivity.this,"设置金额不能为负数",Toast.LENGTH_SHORT).show();
                        }
                    }}
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
     *budget_update()和delete_sqlite(Cursor cursor,int iore) 检查更新数据库，将不符合的数据进行删除
        */
    private void budget_update() {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
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
                } catch (Exception e) {

                } finally {
                    db.close();
                    cursor.close();
                }
            }
        });
        t1.start();
        while (t1.isAlive()) {
        }
    }
    private void delete_sqlite(Cursor cursor, int iore) {
        String Table = "";
        if (iore == 0) {
            Table = "expendbudget";
        } else if (iore == 1) {
            Table = "incomebudget";
        }
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        db.delete(Table, "User_Name=? AND Type=? AND Money=?" +
                        "AND DMSY=? AND Time=?"
                , new String[]{Username,
                        cursor.getString(cursor.getColumnIndex("Type")),
                        cursor.getString(cursor.getColumnIndex("Money")),
                        cursor.getString(cursor.getColumnIndex("DMSY")),
                        String.valueOf(cursor.getLong(cursor.getColumnIndex("Time")))});
        db.close();
    }

    /*
     * budgetlist_change(String date,int position) 当预算列表中预算设置改动，更新budgetlist的显示
     */
    private void budgetlist_change(String date, int position) {
        final String stringdate = date;
       // Log.d("liangjialing","stringdate=  "+stringdate);
        final int pos = position;
        new Thread(new Runnable() {
            @Override
            public void run() {

                BudgetClass budgetClass;
                double resultmoney = Double.valueOf(stringdate) - consumetype_money[pos];
                if (choose_type.getText().toString().equals(choose_typeString[0])) {
                    if (resultmoney >= 0) {
                        budgetClass = new BudgetClass(consumetype[pos],
                                stringdate, zyc_String[1], formatPrice(resultmoney));
                        //Log.d("jisuan", formatPrice(resultmoney) + "正");
                    } else {
                        budgetClass = new BudgetClass(consumetype[pos],
                                stringdate, zyc_String[2], formatPrice(resultmoney * (-1.0)));
                        //  Log.d("jisuan", formatPrice(resultmoney*(-1.0)) + "反");
                    }
                    budgetList.set(pos, budgetClass);
                }
                if (choose_type.getText().toString().equals(choose_typeString[1])) {
                    if (resultmoney >= 0) {
                        budgetClass = new BudgetClass(consumetype[pos],
                                stringdate, zyc_String[4], formatPrice(resultmoney));
                        // Log.d("jisuan", formatPrice(resultmoney) + "正");
                    } else {
                        budgetClass = new BudgetClass(consumetype[pos],
                                stringdate, zyc_String[5], formatPrice(resultmoney * (-1.0)));
                        //Log.d("jisuan", formatPrice(resultmoney) + "反");
                    }
                    budgetList.set(pos, budgetClass);
                }
                handler.sendEmptyMessage(budgetlist_change);
            }
        }).start();
    }

    /*
    各个类型预算进行设置更改,数据库的更改
     */
    private void budget_change(String ysmd, String iore, String type, String money) {
        //type消费类型  iore支出或收入 ysmd本年季月日
        final String stringysmd = ysmd;
        final String stringiore = iore;
        final String stringtype = type;
        final String stringmoney = money;
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    String Table = null;
                    if (stringiore.equals(choose_typeString[0])) {
                        Table = "expendbudget";
                    } else if (stringiore.equals(choose_typeString[1])) {
                        Table = "incomebudget";
                    }
                    cursor = db.query(Table,
                            null, "User_Name=? AND Type=?AND DMSY=?"
                            , new String[]{Username, stringtype, stringysmd}, null, null, null);
                    ContentValues values = new ContentValues();
                    if (cursor.moveToFirst()) {
                        //进行数据的更改
                        values.put("Money", stringmoney);
                        values.put("Time", date.getTime());
                        db.update(Table, values,
                                "User_Name=? AND Type=? AND DMSY=?"
                                , new String[]{Username, stringtype, stringysmd});
                    } else {
                        //进行数据的添加
                        values.put("User_Name", Username);
                        values.put("Type", stringtype);
                        values.put("Money", stringmoney);
                        values.put("DMSY", stringysmd);
                        values.put("Time", date.getTime());
                        db.insert(Table, null, values);
                    }
                } catch (Exception e) {
                } finally {
                    cursor.close();
                    db .close();
                }
            }
        });
        t1.start();
        while (t1.isAlive()) {
        }
        Log.d("TAG", "budget_change完毕");
    }

    /*
    进行将总预算更改为各个类型分预算之和
     */
    private void refresh_budget(String ysmd, String iore) {
        final String stringysmd = ysmd;
        final String stringiore = iore;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    double totaltypemoney = 0;//各个类型支出总预算
                    String Table = "";
                    if (stringiore.equals(choose_typeString[0])) {
                        Table = "expendbudget";
                    } else if (stringiore.equals(choose_typeString[1])) {
                        Table = "incomebudget";
                    }
                    cursor = db.query(Table, null, "User_Name=? AND DMSY=?"
                            , new String[]{Username, stringysmd}, null, null, null);
                    if (cursor.moveToFirst()) {
                        do {
                            if (!cursor.getString(cursor.getColumnIndex("Type")).equals("总预算")) {
                                totaltypemoney = totaltypemoney + Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                            }
                        } while (cursor.moveToNext());
                    }
                    ContentValues values = new ContentValues();
                    values.put("Money", formatPrice(totaltypemoney));
                    values.put("Time", date.getTime());
                    db.update(Table, values, "User_Name=? AND Type=? AND DMSY=?"
                            , new String[]{Username, "总预算", stringysmd});
                    Message message = new Message();
                    message.what = totalbudget_show;
                    Bundle bundle = new Bundle();
                    bundle.putString("iore", stringiore);
                    bundle.putDouble("totalbudgetmoney", totaltypemoney);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (Exception e) {
                } finally {
                    cursor.close();
                    db.close();
                }
            }
        }).start();
    }

    /*
       总预算根据个各类类型预算和进行变化，如果没有总预算则进行总预算的insert
         */
    private void total_budget(String ysmd, String iore) {
        //首先查找数据库得到本日支出中符合各个类型预算的总值
        final String stringysmd = ysmd;
        final String stringiore = iore;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    Log.d("TAG", "开始进行total_budget");
                    double totaltypemoney = 0;//各个类型的总预算金额
                    double totalbudgetmoney = 0;//总预算金额
                    String Table = null;
                    ContentValues values = new ContentValues();
                    if (stringiore.equals(choose_typeString[0])) {
                        Table = "expendbudget";
                    } else if (stringiore.equals(choose_typeString[1])) {
                        Table = "incomebudget";
                    }
                    cursor = db.query(Table, null, "User_Name=? AND DMSY=?"
                            , new String[]{Username, stringysmd}, null, null, null);
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
                    cursor = db.query(Table, null,
                            "User_Name=? AND Type=? AND DMSY=?"
                            , new String[]{Username, "总预算", stringysmd}, null, null, null);
                    if (totaltypemoney != 0) {//各个类型的预算中有已经设置的
                        if (totaltypemoney >= totalbudgetmoney) {
                            //a>=b => b=a  数据库中内容更改    反之b不变
                            totalbudgetmoney = totaltypemoney;
                            if (cursor.moveToFirst()) {
                                // ContentValues values=new ContentValues();
                                values.put("Money", formatPrice(totalbudgetmoney));
                                values.put("Time", date.getTime());
                                db.update(Table, values,
                                        "User_Name=? AND Type=? AND DMSY=?"
                                        , new String[]{Username, "总预算", stringysmd});
                            } else {
                                // ContentValues values = new ContentValues();
                                values.put("User_Name", Username);
                                values.put("Type", "总预算");
                                values.put("Money", formatPrice(totalbudgetmoney));
                                values.put("DMSY", stringysmd);
                                values.put("Time", date.getTime());
                                db.insert(Table, null, values);
                            }
                        }
                    } else {//各个类型的预算中都没有设置过或者设置的值为0.00
                        if (!cursor.moveToFirst()) {
                            // ContentValues values = new ContentValues();
                            values.put("User_Name", Username);
                            values.put("Type", "总预算");
                            values.put("Money", "0.00");
                            values.put("DMSY", stringysmd);
                            values.put("Time", date.getTime());
                            db.insert(Table, null, values);
                        }
                    }
                    Message message = new Message();
                    message.what = totalbudget_show;
                    Bundle bundle = new Bundle();
                    bundle.putString("iore", stringiore);
                    bundle.putDouble("totalbudgetmoney", totalbudgetmoney);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (Exception e) {
                } finally {
                    cursor.close();
                    db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                }
            }
        }).start();
    }

    /*
    点击总预算，进行总预算设置
     */
    private void totalbudget_change(String ysmd, String iore, String money) {
        try {
            double totaltypemoney = 0;
            String Table = "";
            if (iore.equals(choose_typeString[0])) {
                Table = "expendbudget";
            } else if (iore.equals(choose_typeString[1])) {
                Table = "incomebudget";
            }db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            cursor = db.query(Table, null, "User_Name=? AND DMSY=?"
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

                values.put("Money", money);
                values.put("Time", date.getTime());
                db.update(Table, values,
                        "User_Name=? AND Type=? AND DMSY=?"
                        , new String[]{Username, "总预算", ysmd});
                totalbudget_show(iore, Double.valueOf(money));
            } else {
                Toast.makeText(BudgetActivity.this, "总预算不能小于各个类型预算之和！", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
        } finally {
            cursor.close();
            db.close();
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

    /*
     *show_budgetlist(String ysmd,String iore) 显示预算列表，并设置监听事件
     */
    private void show_budgetlist(String ysmd, String iore) {
        final String stringysmd = ysmd;
        final String stringiore = iore;
        /*get_type(stringiore);
        Result_typemoney(stringysmd, stringiore);*/
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                get_type(stringiore);
                Result_typemoney(stringysmd, stringiore);
                Result_budgetlist(stringysmd, stringiore);
                Log.d("TAG", "show_budgetlist->Result_budgetlist得到列表结果");
                handler.sendEmptyMessage(show_budgetlist);
            }
        });
        t1.start();
        while (t1.isAlive()) {
        }
        Log.d("TAG", "show_budgetlist 执行完毕");
    }

    /*
     * get_type(String iore) 获得消费类型
     * Result_typemoney(String ysmd,String iore) 计算各个消费类型的总金额
     * Init_budgetList(String iore) 初始化显示budgetList列表
     * Result_budgetlist(String ysmd,String iore) 的到预算列表budgetlist
     */
    private void get_type(String iore) {//得到消费类型支出 收入
        try {
            typenumber = 0;
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            if (iore.equals(choose_typeString[0])) {//支出
                cursor = db.query("expendtype", null, null,
                        null, null, null, null);
            } else if (iore.equals(choose_typeString[1])) {//收入
                cursor = db.query("incometype", null, null,
                        null, null, null, null);
            }
            if (cursor.moveToFirst()) {
                do {
                    if(!cursor.getString(cursor.getColumnIndex("Type_Name")).equals("总预算")){
                    consumetype[typenumber] = cursor.getString(cursor.getColumnIndex("Type_Name"));
                    typenumber++; }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            cursor.close();
            db.close();
        }
    }
    private void Result_typemoney(String ysmd, String iore) {
        //首先初始化各个消费类型的总金额
        for (int i = 0; i < typenumber; i++) {
            consumetype_money[i] = 0.00;
        }
        //  Log.d("TAG", "show_budgetlist->a");
        int currentseason = season_judge(Integer.valueOf(getTimes(date).substring(5, 7)));
        //确定支出或者收入中各消费类型的消费金额
        // Log.d("TAG", "show_budgetlist->b");
        try {
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            if (iore.equals(choose_typeString[0])) {//支出
                cursor = db.query("expendinfo", null, "User_Name=?"
                        , new String[]{Username}, null, null, null);
            } else if (iore.equals(choose_typeString[1])) {//收入
                cursor = db.query("incomeinfo", null, "User_Name=?"
                        , new String[]{Username}, null, null, null);
            }
            // Log.d("TAG", "show_budgetlist->c");
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
        } catch (Exception e) {
        } finally {
            cursor.close();
            db.close();
        }
    }
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
    private void Result_budgetlist(String ysmd, String iore) {
        Init_budgetList(iore);
        try {db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
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
                            } else if (iore.equals(choose_typeString[1])) {
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
        } catch (Exception e) {
        } finally {
            cursor.close();
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        }
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
