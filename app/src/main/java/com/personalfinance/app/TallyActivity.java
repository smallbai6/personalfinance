package com.personalfinance.app;

import android.content.ContentValues;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Time_Type.CaculatorPop;
import com.personalfinance.app.Time_Type.TimePop;
import com.personalfinance.app.Time_Type.TypePop;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TallyActivity extends AppCompatActivity implements View.OnClickListener {
private Cursor cursor;
    /*
     *数据库
     */
    private SQLiteDatabase db;
    //final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    /*
     *活动跳转
     */
    private Intent intent;
    private String huodong;
    /*
     *顶部按钮  底部保存按钮 判断数据是否有保存（点击过再记一笔和保存）
     */
    private ImageView backimage;
    private TextView back,save,choose;
    private Drawable drawable;
    private Button buttonback, buttonagain;
    private int issave = 0;
    /*
     *填写的内容
     */
    private TextView money, process, type, time;
    private EditText message;
    private LinearLayout layout_money, layout_type, layout_time;

    /*
     *PopupWindow的配置内容
     */
    private PopupWindow choosePopupWindow;
    private ListView list;
    private View contentView, PopParent;
    private String[] income_expend = {"支出", "收入"};
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
   // private View background;
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
    private String Username;

    private final static int Typelist = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Typelist:
                    type.setText((String) msg.obj);
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tally);
       // db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
       intent=getIntent();
       huodong=intent.getStringExtra("HuoDong");
        Username=intent.getStringExtra("Username");

        back = (TextView) findViewById(R.id.tally_back);
        backimage=(ImageView)findViewById(R.id.tally_backimageview);
        //drawable = getResources().getDrawable(R.mipmap.zuojiantou);
       // drawable.setBounds(0, 0, 40, 40);
       // back.setCompoundDrawables(drawable, null, null, null);
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
        //background = this.findViewById(R.id.son_tally);
        PopParent = this.findViewById(R.id.parent_tally);
        choose.setText(income_expend[0]);//初始时是支付
        drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
        drawablel2tubiao();
        Typelist(0);//初始时列表是支付时的分类列表
        InitPopupWindow();//初始化PopupWindow
        date = new Date();
        time.setText(getTimes(date));
        currentdate =date.getTime();


        choose.setOnClickListener(this);//支付收入
        money.setOnClickListener(this);//金额
        layout_type.setOnClickListener(this);//类别分类
        layout_time.setOnClickListener(this);//时间
        save.setOnClickListener(this);//保存
        back.setOnClickListener(this);//返回主界面
        backimage.setOnClickListener(this);
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
                //background.setBackgroundColor(Color.TRANSPARENT);
                backgroundAlpha((float)1.0);
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
        final int pos = position;
        //获取数据库中的数据
        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    if (pos == 0) {
                        cursor = db.query("expendtype", null, null,
                                null, null, null, null);
                    } else if (pos == 1) {
                        cursor = db.query("incometype", null, null,
                                null, null, null, null);
                    }
                    if (cursor.moveToFirst()) {
                        do{
                            if(!cursor.getString(cursor.getColumnIndex("Type_Name")).equals("总预算")){
                                String name = "";
                                name = cursor.getString(cursor.getColumnIndex("Type_Name"));
                                Message message = new Message();
                                message.what = Typelist;
                                message.obj = name;
                                handler.sendMessage(message);
                                return;
                            }
                        }while(cursor.moveToNext());
                    }
                }catch(Exception e){
                }finally {
                   // if(null!=cursor){
                        cursor.close();
                        db.close();
                   // }
                }
            }
        }).start();

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
                //background.setBackgroundColor(Color.GRAY);
                backgroundAlpha((float)0.4);
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
                        time.setText(DetailList.LongToString(date));
                        currentdate = date;
                        Log.d("liangjialing", currentdate + "");
                    }
                });
                // Log.d("liang", "完毕");
                break;
            case R.id.tally_save:
            case R.id.tallycontent_save://进行保存
                //  Log.d("TAG","点击保存  "+choose.getText().toString());
                issave=1;
                income_expend(choose.getText().toString());
                //退出记账活动
                //判断是由哪一个活动进入的TallyActivity
                if(huodong.equals("MainActivity.java")){
                    intent = new Intent(TallyActivity.this, MainActivity.class);
                    //   Log.d("TAG","返回到MainActivity");
                }else if(huodong.equals("DetailActivity.java")){
                    intent = new Intent(TallyActivity.this, DetailActivity.class);
                }
                intent.putExtra("issave", issave);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.tallycontent_again:
                issave=1;
                //再记一笔先保存再将内容清空
                income_expend(choose.getText().toString());
                //清空金额
                money.setText("0.00");
                break;
            case R.id.tally_backimageview:
            case R.id.tally_back:
                //直接退出记账活动
                if(huodong.equals("MainActivity.java")){
                    intent = new Intent(TallyActivity.this, MainActivity.class);
                   // startActivity(intent);
                    // finish();
                }else if(huodong.equals("DetailActivity.java")){
                    intent = new Intent(TallyActivity.this, DetailActivity.class);
                    //startActivity(intent);

                }
                intent.putExtra("issave", issave);
                setResult(RESULT_CANCELED,intent);
                finish();
                //Toast.makeText(TallyActivity.this, "tally_back", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /*
     *保存支付信息
     */
    private void income_expend(String typeString){
        final String string = typeString;
        /* */
       Thread t1= new Thread(new Runnable() {
            @Override
            public void run() {
                  //  Log.d("TAG", "income_expend");
                db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    ContentValues values = new ContentValues();
                    values.put("User_Name", Username);
                    values.put("Money", money.getText().toString());
                    values.put("Type", type.getText().toString());
                    values.put("Time", currentdate);
                    values.put("Message", message.getText().toString());
                    if (string.equals(income_expend[0])) {
                        db.insert("expendinfo", null, values);
                    } else if (string.equals(income_expend[1])) {
                        db.insert("incomeinfo", null, values);
                    }
                    values.clear();
                    db.close();
                //Log.d("liangjialing","a");
            }
        });
       t1.start();
       while(t1.isAlive()){}
    }

    /**
     * 遮罩层
     * @param alpha
     */
    private void backgroundAlpha(float alpha) {
        WindowManager.LayoutParams lp =getWindow().getAttributes();
        lp.alpha = alpha;
        getWindow().setAttributes(lp);
    }
    /*
     *获取系统时间
     */
    private String getTimes(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm EE");
        return format.format(date);
    }
}
