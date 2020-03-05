package com.personalfinance.app;

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

import com.personalfinance.app.Detail.DetailBulkAdapter;
import com.personalfinance.app.Detail.OnTreeNodeCheckedChangeListener;
import com.personalfinance.app.Detail.OnTreeNodeClickListener;
import com.personalfinance.app.Sqlite.Info;
import com.personalfinance.app.Sqlite.Node;
import com.personalfinance.app.Sqlite.NodeData;

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
    private List<Info> InfoList = new ArrayList<>();
    private List<String> day_dayList = new ArrayList<>();
    private ListView listView;
    private List<Node> list = new ArrayList<>();
    private DetailBulkAdapter mAdapter;
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
        currentyear = String.valueOf(intent.getIntExtra("year",0));
        listView = (ListView) findViewById(R.id.detail_bulkeditor_listview);
        detail_list();
        mAdapter = new DetailBulkAdapter(listView, this, list,
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
            public void onCheckChange(Node node, int position, boolean isChecked) {
                showchoosetotal_totalmoney();
            }
        });
        mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
            @Override
            public void onClick(Node node, int position) {
                showchoosetotal_totalmoney();
            }
        });
    }

    private void showchoosetotal_totalmoney() {
        Log.d("setcheck", "dianji");
        //获取所有选中节点
        int total = 0;
        double totalmoney = 0;
        List<Node> selectedNode = mAdapter.getSelectedNode();
        if (selectedNode.size() == 0) {//选中的节点为空，都没有选中；
            showchoosetotal.setText("未进行选择");
            showtotalmoney.setText("");
        } else {
            for (Node n : selectedNode) {
                if (n.isLeaf()) {//1级计入数内
                    NodeData nodeData = (NodeData) n.getData();
                    total++;
                    if (nodeData.getA().substring(0, 1).equals("0")) {
                        totalmoney -= Double.valueOf(nodeData.getC());
                    } else {
                        totalmoney += Double.valueOf(nodeData.getC());
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
                List<Node> allNode = mAdapter.getAllNodes();
                for (Node node : allNode) {
                    mAdapter.setChecked(node, true);
                }
                showchoosetotal_totalmoney();
                break;
            case R.id.detail_bulkeditor_allnochoosebutton://全不选
                List<Node> allNodec = mAdapter.getAllNodes();
                for (Node node : allNodec) {
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
        List<Node> selectedNode = mAdapter.getSelectedNode();
        for (Node node : selectedNode) {
            NodeData nodeData = (NodeData) node.getData();
            if (node.isLeaf()) {
                //如果是叶子节点,进行删除
                String type = nodeData.getA();
                String money = nodeData.getC();
                String time = String.valueOf(nodeData.getTime());
                if (type.substring(0, 1).equals("0")) {
                    //支出
                    db.delete("expendinfo", "User_Name=? AND Money=? " +
                                    "AND Type=? AND Time=?",
                            new String[]{Username, money, type.substring(1), time});
                } else if (type.substring(0, 1).equals("1")) {
                    db.delete("incomeinfo", "User_Name=? AND Money=? " +
                                    "AND Type=? AND Time=? ",
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
        for(int i=0;i<2;i++){
            if(i==0){
                cursor = db.query("expendinfo", null, "User_Name=?",
                        new String[]{Username}, null, null, null);
            }else if(i==1){
                cursor = db.query("incomeinfo", null, "User_Name=?",
                        new String[]{Username}, null, null, null);
            }
            if (cursor.moveToFirst()) {
                do {
                    long time = cursor.getLong(cursor.getColumnIndex("Time"));
                    if (LongToString(time).substring(0, 4).equals(currentyear)) {
                        String money = cursor.getString(cursor.getColumnIndex("Money"));
                        String type = i + cursor.getString(cursor.getColumnIndex("Type"));
                        String text = cursor.getString(cursor.getColumnIndex("Message"));
                        Info info = new Info(Double.valueOf(money), type, time, text);
                        InfoList.add(info);
                    }
                } while (cursor.moveToNext());
            }
        }

        Collections.sort(InfoList);

        for (Info info : InfoList) {
           // Log.d("DetailActivity.liang", info.getMoney() + "  " + info.getType()
          //          + "  " + info.getTime() + "   " + info.getText() + "      " + LongToString(info.getTime()));
        }
    }

    private void detail_Detailday_day() {
        //Log.d("DetailActivity.liang", "进入detail_Detailday_day");
        day_dayList.clear();
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
                createnew = false;
                if (i == (InfoList.size() - 1)) {
                    day_dayList.add(time);
                }
            } else {//总结出一个
                day_dayList.add(time);
                i = i - 1;
                createnew = true;
            }
        }
        Log.d("DEAliang", "1级流水");
        for (String detailSurplus : day_dayList) {
            Log.d("DEAliang", detailSurplus);
        }
    }

    private void detail_list() {
        list.clear();
        detail_DetailInfo(currentyear);
        detail_Detailday_day();
        int a = 0, b = 0;
        int i = 0;//为指针在list中的位置
        int leveldivide01;//等级划分关联
        NodeData nodeData;
        for (a = 0; a < day_dayList.size(); a++) {
            nodeData = new NodeData(day_dayList.get(a),"","", "", "", "",0);
            list.add(new Node<NodeData>(i + "", "-1", nodeData, "0"));
            leveldivide01 = i;
            i++;
            // Log.d("aaaaad","InfoList.get(b).getTime() =  "+LongToString(InfoList.get(b).getTime()).substring(0,11));
            while (day_dayList.get(a).equals(LongToString(InfoList.get(b).getTime()).substring(0, 11))) {
                nodeData = new  NodeData(InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(11, 16),
                        formatPrice(InfoList.get(b).getMoney()),
                        "","","",
                        InfoList.get(b).getTime());
                list.add(new Node<NodeData>(i + "", leveldivide01 + "", nodeData, "1"));
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
