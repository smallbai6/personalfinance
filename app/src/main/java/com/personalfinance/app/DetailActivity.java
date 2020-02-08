package com.personalfinance.app;

import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Detail.Node;
import com.personalfinance.app.Detail.TreeAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Intent intent;
    private ListView listView;
    private TextView backbutton, choosermd;

    private List<Item> list = new ArrayList<>();
    private MyAdapter adapter;

    /*
    年份和季月天选择
     */
    PopupWindow choosePopupWindow;
    private View contentView;
    private ListView chooselistView;
    private ArrayAdapter<String> yrmdadapter;
    private List<String> yrmdchooseList = new ArrayList<>();
    private String[] yearString = {"上一年", "下一年"};
    private String[] rmdString = {"季", "月", "日"};
    private Drawable drawable;
    private int choosetype;//选择类型是yearString 还是rmdString
    private ImageView choosey;//点击进行上一年 下一年选择
    private Calendar calendar = Calendar.getInstance();//获得当前时间
    private int showyear;//当前需要显示时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        listView = (ListView) findViewById(R.id.listview);//流水列表
        seasonlist();

        backbutton = (TextView) findViewById(R.id.detail_back_button);
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        backbutton.setCompoundDrawables(drawable, null, null, null);
        showyear = calendar.get(Calendar.YEAR);
        backbutton.setText(showyear + "年");

        choosermd = (TextView) findViewById(R.id.detail_choose_rmd);//选择季月
        drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
        drawable2tubiao();

        choosey = (ImageView) findViewById(R.id.detail_year_shengluetubiao);

        contentView = getLayoutInflater().inflate(R.layout.textlist, null);
        chooselistView = contentView.findViewById(R.id.textlist_View);
        yrmdadapter = new ArrayAdapter<>(DetailActivity.this, android.R.layout.simple_list_item_1, yrmdchooseList);
        chooselistView.setAdapter(yrmdadapter);
        InitPopupWindow();//初始化popupWindow


        chooselistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (choosetype == 0) {//点击的是年份
                    if (position == 0) {//上一年
                        showyear = showyear - 1;
                    } else if (position == 1) {//下一年
                        showyear = showyear + 1;
                    }
                    backbutton.setText(showyear + "年");
                } else if (choosetype == 1) {//点击的是季月日
                    choosermd.setText(yrmdchooseList.get(position));
                }
                choosePopupWindow.dismiss();
            }
        });
        choosePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (choosetype == 1) {
                    drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
                    drawable2tubiao();
                }
            }
        });
        backbutton.setOnClickListener(this);
        choosermd.setOnClickListener(this);
        choosey.setOnClickListener(this);
    }

    private void drawable2tubiao() {
        drawable.setBounds(0, 0, 30, 30);
        choosermd.setCompoundDrawables(null, null, drawable, null);
        choosermd.setCompoundDrawablePadding(10);
    }

    private void InitPopupWindow() {
        choosePopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        choosePopupWindow.setWidth(175);
        choosePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        choosePopupWindow.setOutsideTouchable(true);
        choosePopupWindow.setTouchable(true);
        choosePopupWindow.setFocusable(true);
    }

    private void Init2yearlist() {
        //列表内容配置
        yrmdchooseList.clear();
        for (int i = 0; i < yearString.length; i++) {
            yrmdchooseList.add(yearString[i]);
        }
        adapter.notifyDataSetChanged();
        chooselistView.setSelection(0);
    }

    private void Init2rmdlist() {
        //列表内容配置
        yrmdchooseList.clear();
        for (int i = 0; i < rmdString.length; i++) {
            yrmdchooseList.add(rmdString[i]);
        }
        adapter.notifyDataSetChanged();
        chooselistView.setSelection(0);
    }

    private void seasonlist() {
        list.clear();
        list.add(new Item(0, 0, 0, false, "4季"));
        list.add(new Item(1, 0, 1, false, "2月"));
        list.add(new Item(2, 1, 2, false, "23号"));
        list.add(new Item(3, 1, 2, false, "20号"));
        list.add(new Item(4, 0, 1, false, "1月"));
        list.add(new Item(5, 4, 2, false, "15号"));
        list.add(new Item(6, 4, 2, false, "10号"));

        list.add(new Item(7, 0, 0, false, "4季"));
        list.add(new Item(8, 7, 1, false, "2月"));
        list.add(new Item(9, 8, 2, false, "23号"));
        list.add(new Item(10, 8, 2, false, "20号"));
        list.add(new Item(11, 7, 1, false, "1月"));
        list.add(new Item(12, 11, 2, false, "15号"));
        list.add(new Item(13, 11, 2, false, "10号"));
        adapter = new MyAdapter(list);
        adapter.setOnInnerItemClickListener(new TreeAdapter.OnInnerItemClickListener<Item>() {
            @Override
            public void onClick(Item node) {
                Toast.makeText(DetailActivity.this, "click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInnerItemLongClickListener(new TreeAdapter.OnInnerItemLongClickListener<Item>() {
            @Override
            public void onLongClick(Item node) {
                Toast.makeText(DetailActivity.this, "long click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });

        listView.setAdapter(adapter);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_back_button://返回主活动
                intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.detail_year_shengluetubiao://点击上一年下一年的省略图标
                Init2yearlist();
                choosePopupWindow.showAsDropDown(choosey);
                choosetype = 0;
                break;
            case R.id.detail_choose_rmd://选择季月日
                Init2rmdlist();
                choosePopupWindow.showAsDropDown(choosermd);
                if (choosePopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
                    drawable2tubiao();
                }
                choosetype = 1;
                break;
        }
    }

    private class MyAdapter extends TreeAdapter<Item> {
        MyAdapter(List<Item> nodes) {
            super(nodes);
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        /**
         * 获取当前位置的条目类型
         */
        @Override
        public int getItemViewType(int position) {
            if (getItem(position).hasChild()) {
                return 1;
            }
            return 0;
        }

        @Override
        protected Holder<Item> getHolder(int position) {
            // Log.d("liang", "getItem(position).name=  " + getItem(position).name
            //         + "  getItem(position).level=" + getItem(position).level +
            //        "  position= " + position);
            switch (getItemViewType(position)) {
                case 1:
                    return new Holder<Item>() {
                        private TextView tv;
                        private ImageView iv;

                        @Override
                        protected View createConvertView(int position) {
                            View view = View.inflate(DetailActivity.this, R.layout.detail_type_a, null);
                            tv = (TextView) view.findViewById(R.id.a_detail_month);
                            iv = (ImageView) view.findViewById(R.id.a_detail_jiantou);
                            return view;
                        }

                        @Override
                        protected void setData(Item node) {
                            iv.setVisibility(node.hasChild() ? View.VISIBLE : View.INVISIBLE);
                            iv.setBackgroundResource(node.isExpand ? R.mipmap.ic_launcher : R.mipmap.ic_launcher_round);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                            params.leftMargin = node.level * 20 + 10;
                            Log.d("liang", "node.level=  " + node.level);
                            tv.setLayoutParams(params);
                            tv.setText(node.name);
                        }
                    };
                default:
                    return new Holder<Item>() {
                        private TextView tv;

                        @Override
                        protected View createConvertView(int position) {
                            View view = View.inflate(DetailActivity.this, R.layout.detail_type_b, null);
                            tv = (TextView) view.findViewById(R.id.b_detail_day);
                            return view;
                        }

                        @Override
                        protected void setData(Item node) {
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                            params.leftMargin = 60;//(node.level + 3) * dip2px(20);
                            tv.setLayoutParams(params);
                            tv.setText(node.name);
                        }
                    };
            }
        }
    }

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private class Item extends Node<Item> {
        String name;

        Item(int id, int pId, int level, boolean isExpand, String name) {
            super(id, pId, level, isExpand);
            this.name = name;
        }
    }
}