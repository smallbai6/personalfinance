package com.personalfinance.app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Time_Type.CaculatorPop;
import com.personalfinance.app.Time_Type.TimePop;
import com.personalfinance.app.Time_Type.TypePop;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TallyActivity extends AppCompatActivity implements View.OnClickListener {

    /*
     *数据库
     */
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    /*
     *活动跳转
     */
    private Intent intent;
    private String huodong;
    /*
     *顶部按钮
     */
    private TextView back,save,choose;
    private Drawable drawable;
    /*
     *填写的内容
     */
    private TextView money, process, type, time;
    private EditText message;
    private LinearLayout layout_money, layout_type, layout_time;
    /*
     *底部保存按钮
     */
    private Button buttonback, buttonagain;
    /*
     *PopupWindow的配置内容
     */
    private PopupWindow choosePopupWindow;
    private ListView list;
    private View contentView, PopParent;
    private String[] income_expend = {"支付", "收入"};
    private ArrayAdapter<String> adapter;
    private List<String> chooseList = new ArrayList<>();

    /*
     *标识符记录
     */
    private int Level = 1;//收入支出和分类的区分标志1：收入支付；2：分类
    private int record = 0;//记录上次点击的信息；
    /*
     *背景
     */
    private View background;
    /*
     *金额&时间&类别
     */
    private CaculatorPop mCaculatorPop;
    private TimePop mTimePop;
    private Date date;
    private long currentdate;
    private TypePop mTypePop;
    /*
     *进行保存操作
     */
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tally);
       intent=getIntent();
       huodong=intent.getStringExtra("HuoDong");

        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        back = (TextView) findViewById(R.id.tally_back);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        back.setCompoundDrawables(drawable, null, null, null);
        choose = (TextView) findViewById(R.id.tally_choose);
        save = (TextView) findViewById(R.id.tally_save);
        money = (TextView) findViewById(R.id.tallycontent_money);
        layout_money = (LinearLayout) findViewById(R.id.layout_money);
        process = (TextView) findViewById(R.id.tallycontent_process);
        type = (TextView) findViewById(R.id.tallycontent_type);
        layout_type=(LinearLayout)findViewById(R.id.layout_type);
        time = (TextView) findViewById(R.id.tallycontent_time);
        layout_time=(LinearLayout)findViewById(R.id.layout_time);
        message = (EditText) findViewById(R.id.tallycontent_message);
        buttonback = (Button) findViewById(R.id.tallycontent_save);
        buttonagain = (Button) findViewById(R.id.tallycontent_again);

        money.setText("0.00");
        process.setText("");
        contentView = getLayoutInflater().inflate(R.layout.textlist, null);
        InitchooseList();
        background = this.findViewById(R.id.son_tally);
        PopParent = this.findViewById(R.id.parent_tally);
        choose.setText(income_expend[0]);//初始时是支付
        drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
        drawablel2tubiao();
        Typelist(0);//初始时列表是支付时的分类列表
        InitPopupWindow();//初始化PopupWindow
        date = new Date();
        time.setText(getTimes(date));
        currentdate = DateToLong(date);
        //获取用户名称
        Cursor cursor = db.query("userinfo", null, "User_Login=?", new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndex("User_Name"));
        } else {//没有登录用户时用户名就为请立即登录
            username = "请立即登录";
        }

        choose.setOnClickListener(this);//支付收入
        money.setOnClickListener(this);//金额
        layout_type.setOnClickListener(this);//类别分类
        layout_time.setOnClickListener(this);//时间
        save.setOnClickListener(this);//保存
        back.setOnClickListener(this);//返回主界面
        buttonback.setOnClickListener(this);//下面的保存按钮
        buttonagain.setOnClickListener(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    choose.setText(chooseList.get(position));//填入支付或收入
                    record = position;
                    Typelist(position);//如果选择支出则获取支出数据库，反之获取收入数据库
                choosePopupWindow.dismiss();
            }
        });
        choosePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                background.setBackgroundColor(Color.TRANSPARENT);
                drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
                drawablel2tubiao();
            }
        });

    }
    /*
     *PopupWindow初始化
     */
    private void InitPopupWindow() {
        choosePopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        choosePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        choosePopupWindow.setOutsideTouchable(true);
        choosePopupWindow.setTouchable(true);
        choosePopupWindow.setFocusable(true);
    }

    /*
     *支付收入列表初始化
     */
    private void InitchooseList() {
        list = contentView.findViewById(R.id.textlist_View);
        adapter = new ArrayAdapter<>(TallyActivity.this, android.R.layout.simple_list_item_1, chooseList);
        list.setAdapter(adapter);
        //列表内容配置
        chooseList.clear();
        for (int i = 0; i < income_expend.length; i++) {
            chooseList.add(income_expend[i]);
        }
        adapter.notifyDataSetChanged();
        list.setSelection(0);
    }

    /*
     *初始化分类的列表
     */
    private void Typelist(int position) {
        //获取数据库中的数据
        Cursor cursor = db.query("expendtype", null, null, null, null, null, null);
        if (position == 0) {
            cursor = db.query("expendtype", null, null, null, null, null, null);
        } else if (position == 1) {
            cursor = db.query("incometype", null, null, null, null, null, null);
        }
        if (cursor.moveToNext()) {
            String name = "";
            if (position == 0) {
                name = cursor.getString(cursor.getColumnIndex("ExpendType_Name"));
            } else if (position == 1) {
                name = cursor.getString(cursor.getColumnIndex("IncomeType_Name"));
            }
            type.setText(name);
        }
    }

    private void drawablel2tubiao() {
        drawable.setBounds(0, 0, 30, 30);
        choose.setCompoundDrawables(null, null, drawable, null);
        choose.setCompoundDrawablePadding(10);
    }
    /*
     *按钮响应事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tally_choose://支付或收入
                choosePopupWindow.showAsDropDown(choose, 0, 0);
                if (choosePopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
                    drawablel2tubiao();
                }
                //Level = 1;//支付收入
                background.setBackgroundColor(Color.GRAY);
                break;
            case R.id.tallycontent_money://金额
                mCaculatorPop = new CaculatorPop(TallyActivity.this, layout_money);
                mCaculatorPop.setOnCaculatorSetListener(new CaculatorPop.OnCaculatorSetListener() {
                    @Override
                    public void OnCaculatorSet(int sort, String date) {
                        if (sort == 1) {
                            money.setText(date);
                        } else if (sort == 2) {
                            process.setText(date);
                        }
                    }
                });
                break;
            case R.id.layout_type://分类类别
                mTypePop = new TypePop(TallyActivity.this, layout_type, record, type.getText().toString());
                //Log.d("liang","进入mTypePop");
                mTypePop.setOnTypeSetListener(new TypePop.OnTypeSetListener() {
                    @Override
                    public void OnTypeSet(String date) {
                        type.setText(date);
                    }
                });
                break;
            case R.id.layout_time://时间
                // Log.d("liang", "text的内容" + time.getText().toString());
                mTimePop = new TimePop(TallyActivity.this, layout_time, time.getText().toString());
                mTimePop.setOnDateTimeSetListener(new TimePop.OnDateTimeSetListener() {
                    @Override
                    public void OnDateTimeSet(long date) {
                        time.setText(LongToString(date));
                        currentdate = date;
                    }
                });
                // Log.d("liang", "完毕");
                break;
            case R.id.tally_save:
            case R.id.tallycontent_save://进行保存
                if (choose.getText().toString().equals(income_expend[0])) {//支付
                    expend();
                } else if (choose.getText().toString().equals(income_expend[1])) {//收入
                    income();
                }
                //退出记账活动
                //判断是由哪一个活动进入的TallyActivity
                if(huodong.equals("MainActivity.java")){
                    intent = new Intent(TallyActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else if(huodong.equals("DetailActivity.java")){
                    intent = new Intent(TallyActivity.this, DetailActivity.class);
                    //startActivity(intent);
                    setResult(RESULT_OK,intent);
                    finish();
                }

                break;
            case R.id.tallycontent_again:
                //再记一笔先保存再将内容清空
                if (choose.getText().toString().equals(income_expend[0])) {//支付
                    expend();
                } else if (choose.getText().toString().equals(income_expend[1])) {//收入
                    income();
                }
                //清空金额
                money.setText("0.00");
                //Toast.makeText(TallyActivity.this, "tallycontent_again", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tally_back:
                //直接退出记账活动
                if(huodong.equals("MainActivity.java")){
                    intent = new Intent(TallyActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else if(huodong.equals("DetailActivity.java")){
                    intent = new Intent(TallyActivity.this, DetailActivity.class);
                    //startActivity(intent);
                    setResult(RESULT_OK,intent);
                    finish();
                }
                //Toast.makeText(TallyActivity.this, "tally_back", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /*
     *保存支付信息
     */
    private void expend() {
        ContentValues values = new ContentValues();
        values.put("User_Name", username);
        values.put("Expend_Money", money.getText().toString());
       // Log.d("jinxingbaocun","数据库中有money= "+Double.parseDouble(money.getText().toString()));
        values.put("Expend_Type", type.getText().toString());
        // values.put("Expend_Time", time.getText().toString());
        values.put("Expend_Time", currentdate);
        values.put("Expend_Message", message.getText().toString());
        db.insert("expendinfo", null, values);
        values.clear();
    }

    /*
     *保存收入信息
     */
    private void income() {
        ContentValues values = new ContentValues();
        values.put("User_Name", username);
        values.put("Income_Money", money.getText().toString());
        values.put("Income_Type", type.getText().toString());
        values.put("Income_Time", currentdate);
        values.put("Income_Message", message.getText().toString());
        db.insert("incomeinfo", null, values);
        values.clear();
    }
    /*
     *背景透明度
     */
    private void darkenBackground(Float bgcolor) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgcolor;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
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

    // date要转换的date类型的时间
    public static long DateToLong(Date date) {
        return date.getTime();
    }
}
