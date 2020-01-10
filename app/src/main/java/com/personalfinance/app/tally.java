package com.personalfinance.app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class tally extends AppCompatActivity implements View.OnClickListener {
    /*
     *顶部按钮
     */
    Button back;
    TextView choose;
    Button save;
    /*
     *填写的内容
     */
    EditText money;
    TextView type;
    TextView time;
    EditText message;
    /*
     *底部保存按钮
     */
    Button buttonback;
    /*
     *PopupWindow的配置内容
     */
    PopupWindow choosePopupWindow;
    ListView list;
    View contentView;
    View PopParent;
    String[] income_expend = {"支付", "收入"};
    private ArrayAdapter<String> adapter;
    private List<String> chooseList = new ArrayList<>();
    // LinearLayout poplayout;
    /*
     *数据库
     */
    SQLiteDatabaseHelper dbHelper;
    SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    /*
     *标识符记录
     */
    private int Level = 1;//收入支出和分类的区分标志1：收入支付；2：分类
    private int record = 0;//记录上次点击的信息；
    /*
     *点击次数设置
     */
    private int frequence = 0;
    /*
     *背景
     */
    View background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tally);
        back = (Button) findViewById(R.id.tally_back);
        choose = (TextView) findViewById(R.id.tally_choose);
        save = (Button) findViewById(R.id.tally_save);
        money = (EditText) findViewById(R.id.tallycontent_money);
        type = (TextView) findViewById(R.id.tallycontent_type);
        time = (TextView) findViewById(R.id.tallycontent_time);
        message = (EditText) findViewById(R.id.tallycontent_message);
        buttonback = (Button) findViewById(R.id.tallycontent_save);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        contentView = getLayoutInflater().inflate(R.layout.textlist, null);

        //poplayout = contentView.findViewById(R.id.textlist_LinearLayout);

        adapter = new ArrayAdapter<>(tally.this, android.R.layout.simple_list_item_1, chooseList);
        list = contentView.findViewById(R.id.textlist_View);
        list.setAdapter(adapter);
        background = this.findViewById(R.id.son_tally);
        PopParent = this.findViewById(R.id.parent_tally);
        choose.setText(income_expend[0]);//初始时是支付
        Typelist(0);//初始时列表是支付时的分类列表
        type.setText(chooseList.get(0));//分类中写支付是分类第一个类别
        InitPopupWindow();//初始化PopupWindow

        choose.setOnClickListener(this);
        type.setOnClickListener(this);
        //poplayout.setOnClickListener(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击弹出弹框
                if (choosePopupWindow != null && choosePopupWindow.isShowing()) {
                    choosePopupWindow.dismiss();
                }
                if (Level == 1) {
                    choose.setText(chooseList.get(position));//填入支付或收入
                    record = position;
                    Typelist(position);//如果选择支出则获取支出数据库，反之获取收入数据库
                    type.setText(chooseList.get(0));//分类中写入第一个类别名字
                } else if (Level == 2) {
                    type.setText(chooseList.get(position));
                }
            }
        });
        choosePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                background.setBackgroundColor(Color.TRANSPARENT);

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
    }

    /*
     *支付收入列表初始化
     */
    private void InitchooseList() {
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
        chooseList.clear();
        if (cursor.moveToFirst()) {
            do {
                String name = "";
                if (position == 0) {
                    name = cursor.getString(cursor.getColumnIndex("ExpendType_Name"));
                } else if (position == 1) {
                    name = cursor.getString(cursor.getColumnIndex("IncomeType_Name"));
                }
                chooseList.add(name);
            } while (cursor.moveToNext());
        }
        adapter.notifyDataSetChanged();
        list.setSelection(0);
    }


    /*
     *按钮响应事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tally_choose:
                InitchooseList();
                choosePopupWindow.setFocusable(true);
                choosePopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                choosePopupWindow.showAsDropDown(choose, 0, 0);
                Level = 1;//支付收入
                frequence = 1;
                background.setBackgroundColor(Color.GRAY);

                break;
            case R.id.tallycontent_type:
                choosePopupWindow.setFocusable(false);
                choosePopupWindow.setHeight(500);
                if (record == 0) {
                    Typelist(0);
                } else if (record == 1) {
                    Typelist(1);
                }
                choosePopupWindow.showAtLocation(PopParent, Gravity.BOTTOM, 0, 0);
                Level = 2;//分类
                break;
            /*case R.id.textlist_LinearLayout:
                choosePopupWindow.dismiss();
                break;*/
            default:
                break;
        }
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
}
