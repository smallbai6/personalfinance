package com.personalfinance.app;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Detail.DetailBulkAdapter;
import com.personalfinance.app.Detail.OnTreeNodeCheckedChangeListener;
import com.personalfinance.app.Detail.OnTreeNodeClickListener;
import com.personalfinance.app.Sqlite.Info;
import com.personalfinance.app.Sqlite.Node;
import com.personalfinance.app.Sqlite.NodeData;
import com.personalfinance.app.Util.DataFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class DetailEditorActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int Detail_list = 1;
    private final static int Delete = 2;
    private SQLiteDatabase db;
    //final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private String Username;
    private long start_time, end_time;
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
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Detail_list:
                    mAdapter = new DetailBulkAdapter(listView, DetailEditorActivity.this, list,
                            1, R.mipmap.shangjiantou, R.mipmap.xiajiantou);
                    listView.setAdapter(mAdapter);
                    Log.d("TAG", "handler");
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
                    break;
                case Delete:
                    mAdapter.getVisibleNodes();
                    showchoosetotal.setText("未进行选择");
                    showtotalmoney.setText("");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_bulkeditor);
        intent = getIntent();
        Username = intent.getStringExtra("Username");
        start_time = intent.getLongExtra("start_time", 0);
        end_time = intent.getLongExtra("end_time", 0);
        listView = (ListView) findViewById(R.id.detail_bulkeditor_listview);
        Detail_list();
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
            showtotalmoney.setText("合计: " + DataFormatUtil.formatPrice(totalmoney));
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_bulkeditor_back:
                Log.d("TAG", "点击返回");
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
                Log.d("TAG", "isdelete=" + isdelete);
                delete();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("TAG", "点击返回");
            intent = new Intent(DetailEditorActivity.this, DetailActivity.class);
            intent.putExtra("isdelete", isdelete);
            setResult(RESULT_OK, intent);
            finish();
            return false;
        }
        return false;

    }

    private void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("TAG", "delete->run");
                List<Node> selectedNode = mAdapter.getSelectedNode();
                for (Node node : selectedNode) {
                    NodeData nodeData = (NodeData) node.getData();
                    if (node.isLeaf()) {
                        //如果是叶子节点,进行删除
                        String type = nodeData.getA();
                        String money = nodeData.getC();
                        String time = String.valueOf(nodeData.getTime());
                        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
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
                        db.close();
                    }
                }
                handler.sendEmptyMessage(Delete);

            }
        }).start();


    }

    private void Detailday_day() {
        day_dayList.clear();
        String time;
        boolean createnew = true;
        for (int i = 0; i < InfoList.size(); i++) {
            if (i == 0) {
                time = DetailList.LongToString(InfoList.get(0).getTime()).substring(0, 11);
            } else if (i != 0 && createnew) {
                time = DetailList.LongToString(InfoList.get(i).getTime()).substring(0, 11);
            } else {
                time = DetailList.LongToString(InfoList.get(i - 1).getTime()).substring(0, 11);
            }
            if (DetailList.LongToString(InfoList.get(i).getTime()).substring(0, 11).equals(time)) {//如果是同一天
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

    }

    private void Detail_list() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                list.clear();
                DetailList detailList = new DetailList(Username, start_time, end_time);
                InfoList = detailList.DetailInfo();
                Detailday_day();
                int a = 0, b = 0;
                int i = 0;//为指针在list中的位置
                int leveldivide01;//等级划分关联
                NodeData nodeData;
               for (a = 0; a < day_dayList.size(); a++) {
                    nodeData = new NodeData(day_dayList.get(a), "", "", "", "", "", 0);
                    list.add(new Node<NodeData>(i + "", "-1", nodeData, "0"));
                    leveldivide01 = i;
                    i++;
                    while (day_dayList.get(a).equals(DetailList.LongToString(InfoList.get(b).getTime()).substring(0, 11))) {
                        nodeData = new NodeData(InfoList.get(b).getType(),
                                DetailList.LongToString(InfoList.get(b).getTime()).substring(12, 17),
                                DataFormatUtil.formatPrice(InfoList.get(b).getMoney()),
                                "", "", "",
                                InfoList.get(b).getTime());
                        list.add(new Node<NodeData>(i + "", leveldivide01 + "", nodeData, "1"));
                        i++;
                        b++;
                        if (b >= InfoList.size()) {
                            break;
                        }
                    }
                }
                handler.sendEmptyMessage(Detail_list);
            }
        }).start();

    }

}
