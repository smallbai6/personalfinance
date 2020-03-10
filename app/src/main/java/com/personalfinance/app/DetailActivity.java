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

import com.personalfinance.app.Detail.DetailAdapter;
import com.personalfinance.app.Detail.OnInnerItemClickListener;
import com.personalfinance.app.Detail.OnInnerItemLongClickListener;
import com.personalfinance.app.Sqlite.Node;
import com.personalfinance.app.Sqlite.NodeData;

import java.util.ArrayList;
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
    private TextView backbutton, chooseysmd;
    private ImageView addtally;
    /*
    结余
     */
    private TextView detail_money, detail_incomemoney, detail_expendmoney;
    /*
    年份和季月天选择
     */
    PopupWindow choosePopupWindow;
    private View contentView;
    private ListView chooselistView;
    private List<String> ysmdchooseList = new ArrayList<>();
    private ArrayAdapter<String> yrmdadapter;

    private String[] shenglueString = {"上一", "下一","批量删除"};
    private String[] ysmdString = {"日","月","季","年"};
    private Drawable drawable;//按键旁的图标显示
    private int choosetype;//选择类型是shenglueString 还是ysmdString
    private ImageView chooseln;//点击进行上一年 下一年选择
    private long start_time,end_time;
    private int timetype;//时间段类型


    /*
    总列表和各个等级排序列表
     */
    private MyListView listView;
    private List<Node> list = new ArrayList<>();
    private DetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        /*cursor = db.query("userinfo", null,
                "User_Login=?", new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            Username = cursor.getString(cursor.getColumnIndex("User_Name"));
        } else {.//没有登录用户时用户名就为请立即登录
            Username = "请立即登录";
        }
*/
        intent=getIntent();
        Username=intent.getStringExtra("Username");
        start_time=intent.getLongExtra("start_time",0);
        end_time=intent.getLongExtra("end_time",0);
        timetype=intent.getIntExtra("timetype",-1);




        listView = (MyListView) findViewById(R.id.detail_listview);//流水列表
        backbutton = (TextView) findViewById(R.id.detail_back_button);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        backbutton.setCompoundDrawables(drawable, null, null, null);
        //backbutton.setText(DetailList.LongToString(start_time).substring(0,5));
        BackbuttonText();
        chooseysmd = (TextView) findViewById(R.id.detail_choose_rmd);//选择季月
        chooseysmd.setText(ysmdString[timetype]);
        drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
        drawable2tubiao();

        addtally = (ImageView) findViewById(R.id.detail_tianjia);

        chooseln = (ImageView) findViewById(R.id.detail_year_shengluetubiao);
        detail_money = (TextView) findViewById(R.id.detail_money);
        detail_incomemoney = (TextView) findViewById(R.id.detail_incomemoney);
        detail_expendmoney = (TextView) findViewById(R.id.detail_expendmoney);

        contentView = getLayoutInflater().inflate(R.layout.textlist, null);
        chooselistView = contentView.findViewById(R.id.textlist_View);
        yrmdadapter = new ArrayAdapter<>(DetailActivity.this, android.R.layout.simple_list_item_1, ysmdchooseList);
        chooselistView.setAdapter(yrmdadapter);
        InitPopupWindow();//初始化popupWindow

        chooselistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (choosetype == 0) {
                    if (position == 0) {//上一
                        StartEndTime startEndTime=new StartEndTime(start_time,end_time,timetype);
                        long[] SE_time=startEndTime.SetLast();
                        start_time=SE_time[0];
                        end_time=SE_time[1];
                       // showyear = showyear - 1;
                    } else if (position == 1) {//下一
                        StartEndTime startEndTime=new StartEndTime(start_time,end_time,timetype);
                        long[] SE_time=startEndTime.SetNext();
                        start_time=SE_time[0];
                        end_time=SE_time[1];
                      //  showyear = showyear + 1;
                    }else if(position==2){//批量删除
                        intent = new Intent(DetailActivity.this, DetailEditorActivity.class);
                        intent.putExtra("Username", Username);
                        intent.putExtra("start_time",start_time);
                        intent.putExtra("end_time",end_time);
                        //intent.putExtra("year", showyear);
                        startActivityForResult(intent, 3);
                    }
                    //backbutton.setText(DetailList.LongToString(start_time).substring(0,5) );
                    BackbuttonText();
                }
                else if (choosetype == 1) {//点击的是年季月日
                    chooseysmd.setText(ysmdchooseList.get(position));
                }
                //不管点击的是什么，都进行列表重建
               Refresh_List();
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
        chooseysmd.setOnClickListener(this);
        chooseln.setOnClickListener(this);
        addtally.setOnClickListener(this);
        //Refresh_List();列表刷新
        /*DetailList detailList=new DetailList(Username,start_time,end_time);
        list=detailList.Detailyear_list();
        list_adapter();*/
        Refresh_List();
    }
    private void BackbuttonText(){
        String string="";
        if(timetype==0){
            //2020.03.06
            string=DetailList.LongToString(start_time).substring(0,4)+"."+
                    DetailList.LongToString(start_time).substring(5,7)+"."+
                    DetailList.LongToString(start_time).substring(8,10);
        }else if(timetype==1){
            //2020年03月
            string=DetailList.LongToString(start_time).substring(0,8);
        }else if(timetype==2){
            //2020.01.16-03.16
            string=DetailList.LongToString(start_time).substring(0,4)+"."+
                    DetailList.LongToString(start_time).substring(5,7)+"."+
                    DetailList.LongToString(start_time).substring(8,10)+"-"+
                    DetailList.LongToString(end_time).substring(5,7)+"."+
                    DetailList.LongToString(start_time).substring(8,10);
        }else if(timetype==3){
            //2020年
            string=DetailList.LongToString(start_time).substring(0,5);
        }
        backbutton.setText(string);
    }
    private void drawable2tubiao() {
        drawable.setBounds(0, 0, 30, 30);
        chooseysmd.setCompoundDrawables(null, null, drawable, null);
        chooseysmd.setCompoundDrawablePadding(10);
    }

    private void InitPopupWindow() {
        choosePopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //choosePopupWindow.setWidth(200);
        choosePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        choosePopupWindow.setOutsideTouchable(true);
        choosePopupWindow.setTouchable(true);
        choosePopupWindow.setFocusable(true);
    }

    private void Init2ysmdchooselist(int choosetype) {
        //列表内容配置
        ysmdchooseList.clear();
        if (choosetype == 0) {
            for (int i = 0; i < shenglueString.length-1; i++) {
                ysmdchooseList.add(shenglueString[i]+ysmdString[timetype]);
            }
            ysmdchooseList.add(shenglueString[shenglueString.length-1]);
        } else if (choosetype == 1) {
            for (int i = 0; i < ysmdString.length; i++) {
                ysmdchooseList.add(ysmdString[i]);
            }
        }
        adapter.notifyDataSetChanged();
        chooselistView.setSelection(0);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_back_button://返回主活动
                intent = new Intent(DetailActivity.this, MainActivity.class);
                //startActivity(intent);
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.detail_year_shengluetubiao://点击上一年下一年的省略图标
                choosetype = 0;
                Init2ysmdchooselist(choosetype);
                choosePopupWindow.setWidth(200);
                choosePopupWindow.showAsDropDown(chooseln);
                break;
            case R.id.detail_choose_rmd://选择季月日
                choosetype = 1;
                Init2ysmdchooselist(choosetype);
                choosePopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                choosePopupWindow.showAsDropDown(chooseysmd);
                if (choosePopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
                    drawable2tubiao();
                }
                break;

            case R.id.detail_tianjia://添加数据
                intent = new Intent(DetailActivity.this, TallyActivity.class);
                intent.putExtra("Username",Username);
                intent.putExtra("HuoDong", "DetailActivity.java");
                startActivityForResult(intent, 1);//requestCode=1为进入TallyActivity.class中
                //startActivity(intent);
                //finish();
        }
    }

    private void list_adapter() {
        adapter = new DetailAdapter(listView, this, list,
                0, R.mipmap.shangjiantou, R.mipmap.xiajiantou);
        listView.setAdapter(adapter);
         Total_show();
        adapter.setOnInnerItemClickListener(new OnInnerItemClickListener() {
            @Override
            public void onClick(Node node, int position) {
                Log.d("onClick"," position    "+position);
                NodeData nodeData = (NodeData) node.getData();
                Toast.makeText(DetailActivity.this, "短点  " + nodeData.getC(), Toast.LENGTH_SHORT).show();
                intent = new Intent(DetailActivity.this, TallyEditorActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("money", nodeData.getE());
                intent.putExtra("type", nodeData.getC());
                intent.putExtra("time", nodeData.getTime());
                intent.putExtra("message", nodeData.getF());
                intent.putExtra("HuoDong","DetailActivity.java");
                startActivityForResult(intent, 2);
            }
        });
        adapter.setOnInnerItemLongClickListener(new OnInnerItemLongClickListener() {
            @Override
            public void onClick(Node node, int position) {
                NodeData nodeData = (NodeData) node.getData();
                Toast.makeText(DetailActivity.this, "长点  " + nodeData.getC(), Toast.LENGTH_SHORT).show();
                intent = new Intent(DetailActivity.this, DetailEditorActivity.class);
                intent.putExtra("Username", Username);
                intent.putExtra("start_time",start_time);
                intent.putExtra("end_time",end_time);
                //intent.putExtra("year", showyear);
                startActivityForResult(intent, 3);
            }
        });
    }
    private void Total_show(){
        double totalmoney = 0;
        double totalexpendmoney = 0;
        double totalincomemoney = 0;
        for(Node node:list){
            if(node.isRootNode()){
                //为根节点
                NodeData nodeData =(NodeData)node.getData();
                totalmoney+=Double.valueOf(nodeData.getC());
                totalincomemoney+=Double.valueOf(nodeData.getD());
                totalexpendmoney+=Double.valueOf(nodeData.getE());
            }
        }
        detail_money.setText(DetailList.formatPrice(totalmoney));
        detail_expendmoney.setText(DetailList.formatPrice(totalexpendmoney));
        detail_incomemoney.setText(DetailList.formatPrice(totalincomemoney));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //DetailList detailList=new DetailList(Username,start_time,end_time);
        switch (requestCode) {
            case 1://添加数据
                int issave = data.getIntExtra("issave", -1);
              //  Log.d("liangjialinga",issave+"  "+chooseysmd.getText().toString());
                if (!((resultCode == RESULT_OK) && (issave == 0))) {
                    Refresh_List();
                }
                break;
            case 2://编辑数据
                if (resultCode == RESULT_OK) {
                    Refresh_List();
                }
                break;
            case 3://批量编辑
                int isdelete = intent.getIntExtra("isdelete", 0);
                if (resultCode == RESULT_OK) {//返回了
                    if (isdelete != 0) {
                        Refresh_List();
                    }
                }
                break;
            default:
                break;
        }
    }
    private void Refresh_List(){
        DetailList detailList=new DetailList(Username,start_time,end_time);
        if (chooseysmd.getText().equals(ysmdString[0])) {
            list=detailList.Detailday_list();
        } else if (chooseysmd.getText().equals(ysmdString[1])) {
            list=detailList.Detailmonth_list();
        } else if (chooseysmd.getText().equals(ysmdString[2])) {
            list=detailList.Detailseason_list();
        }else if (chooseysmd.getText().equals(ysmdString[3])) {
            list=detailList.Detailyear_list();
        }
        list_adapter();
    }

    /*public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public String LongToString(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy.MM月dd日HH:mmEE").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }*/

}
