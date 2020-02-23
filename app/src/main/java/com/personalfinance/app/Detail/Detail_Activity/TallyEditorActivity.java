package com.personalfinance.app;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Time_Type.CaculatorPop;
import com.personalfinance.app.Time_Type.TimePop;
import com.personalfinance.app.Time_Type.TypePop;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TallyEditorActivity extends AppCompatActivity implements View.OnClickListener {
    /*
     *数据库
     */
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    /*
     *活动跳转
     */
    private Intent intent;
    /*
     *顶部按钮
     */
    private TextView back, save, choose;
    private int record;
    private String[] income_expend = {"支付", "收入"};
    private Drawable drawable;
    /*
     *填写的内容
     */
    private TextView money, process, type, time;
    private EditText message;
    private String InitializeMoney, InitializeType, InitializeMessage;
    private long InitializeTime, currentdate;
    private LinearLayout layout_money, layout_type, layout_time;
    /*
     *底部保存按钮
     */
    private Button buttonback, buttondelete;
    /*
     *PopupWindow的配置内容
     */
    /*
     *金额&时间&类别
     */
    private CaculatorPop mCaculatorPop;
    private TimePop mTimePop;
    private Date date;
    private TypePop mTypePop;
    /*
     *进行保存操作
     */
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tally_editor);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        back = (TextView) findViewById(R.id.tally_editor_back);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        back.setCompoundDrawables(drawable, null, null, null);
        choose = (TextView) findViewById(R.id.tally_editor_choose);
        save = (TextView) findViewById(R.id.tally_editor_save);

        money = (TextView) findViewById(R.id.tallycontent_money);
        layout_money = (LinearLayout) findViewById(R.id.layout_money);
        process = (TextView) findViewById(R.id.tallycontent_process);
        type = (TextView) findViewById(R.id.tallycontent_type);
        layout_type = (LinearLayout) findViewById(R.id.layout_type);
        time = (TextView) findViewById(R.id.tallycontent_time);
        layout_time = (LinearLayout) findViewById(R.id.layout_time);
        message = (EditText) findViewById(R.id.tallycontent_message);

        buttonback = (Button) findViewById(R.id.tallycontent_editor_save);
        buttondelete = (Button) findViewById(R.id.tallycontent_editor_delete);


        //获取用户名称
        /*Cursor cursor = db.query("userinfo", null, "User_Login=?", new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndex("User_Name"));
        } else {//没有登录用户时用户名就为请立即登录
            username = "请立即登录";
        }*/
//获得上一个活动的信息
        intent = getIntent();
        username=intent.getStringExtra("username");
        if (intent.getStringExtra("type").substring(0, 1).equals("0")) {
            choose.setText(income_expend[0]);
            record = 0;
        } else {
            choose.setText(income_expend[1]);
            record = 1;
        }
        InitializeMoney = intent.getStringExtra("money");
        money.setText(InitializeMoney);
        InitializeType = intent.getStringExtra("type").substring(1);
        type.setText(InitializeType);
        currentdate = intent.getLongExtra("time", 0);
        InitializeTime = currentdate;
        time.setText(LongToString(currentdate));
        InitializeMessage = intent.getStringExtra("message");
        message.setText(InitializeMessage);

        money.setOnClickListener(this);
        layout_type.setOnClickListener(this);
        layout_time.setOnClickListener(this);

        back.setOnClickListener(this);
        save.setOnClickListener(this);
        buttonback.setOnClickListener(this);
        buttondelete.setOnClickListener(this);
    }

    /*
     *按钮响应事件
     */
    public void onClick(View v) {
        //Toast.makeText(TallyEditorActivity.this, v.getId() + "", Toast.LENGTH_SHORT).show();
        switch (v.getId()) {
            case R.id.tallycontent_money://金额

                mCaculatorPop = new CaculatorPop(TallyEditorActivity.this, layout_money);
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
                mTypePop = new TypePop(TallyEditorActivity.this, layout_type, record, type.getText().toString());
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
                mTimePop = new TimePop(TallyEditorActivity.this, layout_time, time.getText().toString());
                mTimePop.setOnDateTimeSetListener(new TimePop.OnDateTimeSetListener() {
                    @Override
                    public void OnDateTimeSet(long date) {
                        time.setText(LongToString(date));
                        currentdate = date;
                    }
                });
                break;
            case R.id.tally_editor_save:
            case R.id.tallycontent_editor_save://进行更新保存
                if (choose.getText().toString().equals(income_expend[0])) {//支付
                    expend();
                  //  Log.d("jinxingbaocun", "zhifu");
                } else if (choose.getText().toString().equals(income_expend[1])) {//收入
                   // Log.d("jinxingbaocun", "shouru");
                    income();
                }
                //返回到上一个活动中
                intent = new Intent(TallyEditorActivity.this, DetailActivity.class);
               /* if(currentdate!=InitializeTime){
                    //时间更改了
                    setResult(RESULT_CANCELED,intent);
                }else{
                    setResult(RESULT_OK, intent);
                }*/
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.tallycontent_editor_delete://删除该条数据
                delete_Dialog();
                break;
            case R.id.tally_editor_back:
                //返回到上一个活动中
                intent = new Intent(TallyEditorActivity.this, DetailActivity.class);
                //startActivity(intent);
                setResult(RESULT_CANCELED, intent);
                finish();
                break;
            default:
                break;
        }
    }
    private void delete_Dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除提示");
        builder.setMessage("是否删除该条记录");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (choose.getText().toString().equals(income_expend[0])) {//支付
                    db.delete("expendinfo", "User_Name=? AND Expend_Money=? " +
                                    "AND Expend_Type=? AND Expend_Time=? AND Expend_Message=?",
                            new String[]{username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
                } else if (choose.getText().toString().equals(income_expend[1])) {//收入
                    db.delete("incomeinfo", "User_Name=? AND Income_Money=? " +
                                    "AND Income_Type=? AND Income_Time=? AND Income_Message=?",
                            new String[]{username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
                }
                //返回到上一个活动中
                intent = new Intent(TallyEditorActivity.this, DetailActivity.class);
                //startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
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
     *保存更改后的支付信息
     */
    private void expend() {
        ContentValues values = new ContentValues();
        values.put("User_Name", username);
        values.put("Expend_Money", money.getText().toString());
        values.put("Expend_Type", type.getText().toString());
        // values.put("Expend_Time", time.getText().toString());
        values.put("Expend_Time", currentdate);
        values.put("Expend_Message", message.getText().toString());

        db.update("expendinfo", values, "User_Name=? AND Expend_Money=? " +
                        "AND Expend_Type=? AND Expend_Time=? AND Expend_Message=?",
                new String[]{username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
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
        db.update("incomeinfo", values, "User_Name=? AND Income_Money=? " +
                        "AND Income_Type=? AND Income_Time=? AND Income_Message=?",
                new String[]{username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
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
    private String LongToStringa(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }
    // date要转换的date类型的时间
    public static long DateToLong(Date date) {
        return date.getTime();
    }
}
