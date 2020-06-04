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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.DatabaseConfig;
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
    private String Username;
    private Intent intent;
    private String huodong;

    /*
     *顶部按钮
     */
    private ImageView backimage;
    private TextView back, save, choose;
    private int record;
    private String[] income_expend = {"支出", "收入"};
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tally_editor);
        back = (TextView) findViewById(R.id.tally_editor_back);
        backimage=(ImageView)findViewById(R.id.tally_editor_backimageview);
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

//获得上一个活动的信息
        intent = getIntent();
        huodong=intent.getStringExtra("HuoDong");
        Username=intent.getStringExtra("Username");
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
        time.setText(DetailList.LongToString(currentdate));
        InitializeMessage = intent.getStringExtra("message");
        message.setText(InitializeMessage);

        money.setOnClickListener(this);
        layout_type.setOnClickListener(this);
        layout_time.setOnClickListener(this);

        back.setOnClickListener(this);
        backimage.setOnClickListener(this);
        save.setOnClickListener(this);
        buttonback.setOnClickListener(this);
        buttondelete.setOnClickListener(this);
    }

    /*
     *按钮响应事件
     */
    public void onClick(View v) {
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
                mTypePop.setOnTypeSetListener(new TypePop.OnTypeSetListener() {
                    @Override
                    public void OnTypeSet(String date) {
                        type.setText(date);
                    }
                });
                break;
            case R.id.layout_time://时间
                mTimePop = new TimePop(TallyEditorActivity.this, layout_time, time.getText().toString());
                mTimePop.setOnDateTimeSetListener(new TimePop.OnDateTimeSetListener() {
                    @Override
                    public void OnDateTimeSet(long date) {
                        time.setText(DetailList.LongToString(date));
                        currentdate = date;
                    }
                });
                break;
            case R.id.tally_editor_save:
            case R.id.tallycontent_editor_save://进行更新保存
                income_expend(choose.getText().toString());
                //返回到上一个活动中
                if(huodong.equals("DetailActivity.java")){
                    intent = new Intent(TallyEditorActivity.this, DetailActivity.class);
                }else if(huodong.equals("StatisticalEditorActivity.java")){
                    intent = new Intent(TallyEditorActivity.this, StatisticalEditorActivity.class);
                }
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.tallycontent_editor_delete://删除该条数据
                delete_Dialog();
                break;
            case R.id.tally_editor_backimageview:
            case R.id.tally_editor_back:
                //返回到上一个活动中
                if(huodong.equals("DetailActivity.java")){
                    intent = new Intent(TallyEditorActivity.this, DetailActivity.class);
                }else if(huodong.equals("StatisticalEditorActivity.java")){
                    intent = new Intent(TallyEditorActivity.this, StatisticalEditorActivity.class);
                }
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
                db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                if (choose.getText().toString().equals(income_expend[0])) {//支付
                    db.delete("expendinfo", "User_Name=? AND Money=? " +
                                    "AND Type=? AND Time=? AND Message=?",
                            new String[]{Username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
                } else if (choose.getText().toString().equals(income_expend[1])) {//收入
                    db.delete("incomeinfo", "User_Name=? AND Money=? " +
                                    "AND Type=? AND Time=? AND Message=?",
                            new String[]{Username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
                }
                db.close();
                //返回到上一个活动中
                if(huodong.equals("DetailActivity.java")){
                    intent = new Intent(TallyEditorActivity.this, DetailActivity.class);
                }else if(huodong.equals("StatisticalEditorActivity.java")){
                    intent = new Intent(TallyEditorActivity.this, StatisticalEditorActivity.class);
                }
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
     *保存更改后的支付和收入信息
     */
    private void income_expend(String typeString) {
        final String string=typeString;
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                ContentValues values = new ContentValues();
                values.put("User_Name", Username);
                values.put("Money", money.getText().toString());
                values.put("Type", type.getText().toString());
                values.put("Time", currentdate);
                values.put("Message", message.getText().toString());
                if (string.equals(income_expend[0])) {
                    db.update("expendinfo", values, "User_Name=? AND Money=? " +
                                    "AND Type=? AND Time=? AND Message=?",
                            new String[]{Username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
                } else if (string.equals(income_expend[1])) {
                    db.update("incomeinfo", values, "User_Name=? AND Money=? " +
                                    "AND Type=? AND Time=? AND Message=?",
                            new String[]{Username, InitializeMoney, InitializeType, String.valueOf(InitializeTime), InitializeMessage});
                }
                db.close();
            }
        }).start();

    }

}
