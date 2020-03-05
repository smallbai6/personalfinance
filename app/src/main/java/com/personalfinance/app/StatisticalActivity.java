package com.personalfinance.app;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.personalfinance.app.Statistical.TimeZDYPop;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticalActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private Cursor cursor;
    private String Username;
    private Intent intent;
    private Drawable drawable;
    //-----------------------------
    private PopupWindow choosePopupWindow;
    private View view_choose;
    private ListView chooselistView;
    private ArrayAdapter<String> chooseAdapter;
    /*
    上边的按键
     */
    private TextView backbutton, choosebutton;
    private String[] chooseString = {"支出", "收入"};
    /*
    下部分的按键
     */
    private TextView choosetime;
    private long start_time, end_time;
    private View view_dialog;
    private Dialog timedialog;//本日月季年自定义
    private int choosetimetype=0;//时间类型选择 0本日 1本月 2本季 3本年 4自定义
    //private TextView[] time_tv = new TextView[5];
    private LinearLayout[] linearLayouts=new LinearLayout[5];
    private TimeZDYPop timeZDYPop;
    private ImageView lastyearIV, nextyearIV;
    /*
    饼状图
     */
    public static final int[] PIE_COLORS = {Color.parseColor("#F34B1C"),
            Color.parseColor("#FF9500"), Color.parseColor("#FFEB3B"),
            Color.parseColor("#619A3F"), Color.parseColor("#1C60F3"),
            Color.parseColor("#ee82ee"), Color.parseColor("#ff00ff")
    };
    private List<String> consumetype = new ArrayList<>();
    private PieChart pieChart;
    private List<PieEntry> entries = new ArrayList<>();
    /*
    饼状图下的标签
     */
    public static final int[] LEGEND_COLORS = {R.color.colorred, R.color.colororange, R.color.colorlightyellow,
            R.color.colorgreen, R.color.colorblue, R.color.colorindigo, R.color.colorpurple};
    private TextView[] piechart_legend = new TextView[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistical_piechart);

        db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        cursor = db.query("userinfo", null, "User_Login=?",
                new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            Username = cursor.getString(cursor.getColumnIndex("User_Name"));
        } else {//没有登录用户时用户名就为请立即登录
            Username = "请立即登录";
        }
        pieChart = (PieChart) findViewById(R.id.PieChart);
        InitChoosePopupWindow();//初始化收入支出的弹窗
        SetChoosePopupWindow();//进行弹窗中数据的适配监听
        Set_Legend();//进行饼图标签控件的设置
        backbutton = (TextView) findViewById(R.id.piechart_back);//退出活动
        drawable = getResources().getDrawable(R.mipmap.zuojiantou);
        drawable.setBounds(0, 0, 40, 40);
        backbutton.setCompoundDrawables(drawable, null, null, null);
        backbutton.setCompoundDrawablePadding(10);
        backbutton.setOnClickListener(this);
        choosebutton = (TextView) findViewById(R.id.piechart_choose);//支出收入按键
        drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
        drawabletotubiao(choosebutton);
        choosebutton.setOnClickListener(this);
        choosetime = (TextView) findViewById(R.id.statistical_time);//时间选择按键
        choosetime.setOnClickListener(this);
        lastyearIV = (ImageView) findViewById(R.id.statistical_time_lastyear);
        lastyearIV.setOnClickListener(this);
        nextyearIV = (ImageView) findViewById(R.id.statistical_time_nextyear);
        nextyearIV.setOnClickListener(this);
        //时间点击设置
        InitTimeDialog();
        SetTimeDialog();

        //一开始支出收入部分和时间部分进行初始化显示
        choosebutton.setText(chooseString[0]);
        GetYear();
        choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
        GetAllConsume(choosebutton.getText().toString(), start_time, end_time);


        //点击列表
        chooselistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Toast.makeText(StatisticalActivity.this, "支出", Toast.LENGTH_SHORT).show();
                    choosebutton.setText(chooseString[0]);
                    GetAllConsume(choosebutton.getText().toString(), start_time, end_time);

                } else if (position == 1) {
                    Toast.makeText(StatisticalActivity.this, "收入", Toast.LENGTH_SHORT).show();
                    choosebutton.setText(chooseString[1]);
                    GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                }
                choosePopupWindow.dismiss();
            }
        });

        choosePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                drawable = getResources().getDrawable(R.mipmap.shangsanjiao);
                drawabletotubiao(choosebutton);
            }
        });
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // Log.d("liangjialing", ((PieEntry) e).getLabel() + "   " + ((PieEntry) e).getValue());
                Activity_Change(Username, choosebutton.getText().toString(), ((PieEntry) e).getLabel(), start_time, end_time);

            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    /*
        支付收入的图标变换
         */
    private void drawabletotubiao(TextView textView) {
        drawable.setBounds(0, 0, 30, 30);
        textView.setCompoundDrawables(null, null, drawable, null);
        textView.setCompoundDrawablePadding(10);
    }

    /*
    初始化popupWindow
     */
    private void InitChoosePopupWindow() {
        view_choose = getLayoutInflater().inflate(R.layout.textlist, null);
        choosePopupWindow = new PopupWindow(view_choose,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        choosePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        choosePopupWindow.setOutsideTouchable(true);
        choosePopupWindow.setTouchable(true);
        choosePopupWindow.setFocusable(true);
    }

    private void SetChoosePopupWindow() {
        chooselistView = view_choose.findViewById(R.id.textlist_View);
        chooseAdapter = new ArrayAdapter<>(StatisticalActivity.this, android.R.layout.simple_list_item_1, chooseString);
        chooselistView.setAdapter(chooseAdapter);
    }


    /*
    初始化时间选择对话框
     */
    private void InitTimeDialog() {
        view_dialog = LayoutInflater.from(this).inflate(R.layout.statistical_time, null);
        timedialog = new Dialog(this, R.style.DialogTheme);
        timedialog.setContentView(view_dialog);
    }

    /*
    设置时间选择对话框的控件
     */
    private void SetTimeDialog() {
        //弹窗的按键
        linearLayouts[0]=(LinearLayout)view_dialog.findViewById(R.id.statistical_time_day) ;
        linearLayouts[1]=(LinearLayout)view_dialog.findViewById(R.id.statistical_time_month) ;
        linearLayouts[2]=(LinearLayout)view_dialog.findViewById(R.id.statistical_time_season) ;
        linearLayouts[3]=(LinearLayout)view_dialog.findViewById(R.id.statistical_time_year) ;
        linearLayouts[4]=(LinearLayout)view_dialog.findViewById(R.id.statistical_time_zidingyi) ;
        DialogClick dc = new DialogClick();
        for (LinearLayout linearLayout : linearLayouts) {
            linearLayout.setOnClickListener(dc);
        }

        /*timedialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //   Log.d("liangjialing", "setOnCancelListener");
            }
        });*/
    }

    private class DialogClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.statistical_time_day://本日
                    GetDay();
                    choosetimetype=0;
                    choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
                    timedialog.cancel();
                    Log.d("liangjialing",start_time+"    "+end_time);
                    GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                    break;
                case R.id.statistical_time_month://本月
                    GetMonth();
                    choosetimetype=1;
                    choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
                    timedialog.cancel();
                    Log.d("liangjialing",start_time+"    "+end_time);
                    GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                    break;
                case R.id.statistical_time_season://本季
                    GetSeason();
                    choosetimetype=2;
                    choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
                    timedialog.cancel();
                    Log.d("liangjialing",start_time+"    "+end_time);
                    GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                    break;
                case R.id.statistical_time_year://本年
                    GetYear();
                    choosetimetype=3;
                    choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
                    timedialog.cancel();
                    Log.d("liangjialing",start_time+"    "+end_time);
                    GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                    break;
                case R.id.statistical_time_zidingyi://自定义
                    timedialog.cancel();//关闭该对话框
                    //打开popupwindow时间设置
                    //choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
                    timeZDYPop = new TimeZDYPop(StatisticalActivity.this, choosetime, start_time, end_time);
                    timeZDYPop.setOnDateTimeSetListener(new TimeZDYPop.OnDateTimeSetListener() {
                        @Override
                        public void OnDateTimeSet(long start, long end) {
                            //  Log.d("showtime","jinru");
                            start_time = start;
                            end_time = end;
                            choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
                            GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                            choosetimetype=4;
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.piechart_back://返回到主界面
                intent = new Intent(StatisticalActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.piechart_choose://进行支出收入选择
                choosePopupWindow.showAsDropDown(choosebutton);//弹出弹窗
                drawable = getResources().getDrawable(R.mipmap.xiasanjiao);
                drawabletotubiao(choosebutton);
                break;
            case R.id.statistical_time://进行时间选择
                //点击弹出dialog
                show_dialog(200);
                //  Log.d("tiancai","进行时间选择");
                break;
            case R.id.statistical_time_lastyear://上
                SetLast();
                choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
               // Log.d("liangjialing",start_time+"    "+end_time);
                GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                break;
            case R.id.statistical_time_nextyear://下
                SetNext();
                choosetime.setText(LongToString(start_time) + "-" + LongToString(end_time));
               // Log.d("liangjialing",start_time+"    "+end_time);
                GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                break;
            default:
                break;
        }
    }
private void SetLast(){
    Calendar start_calendar = Calendar.getInstance();
    Calendar end_calendar = Calendar.getInstance();
    start_calendar.setTimeInMillis(start_time);
    end_calendar.setTimeInMillis(end_time);
    if(choosetimetype==0){//本日
        start_calendar.add(Calendar.DAY_OF_MONTH, -1);//倒回到前一天
        end_calendar.add(Calendar.DAY_OF_MONTH, -1);//倒回到前一天
    }else if(choosetimetype==1) {//本月
        start_calendar.add(Calendar.MONTH,-1);
        end_calendar.set(Calendar.DAY_OF_MONTH,1);
        end_calendar.add(Calendar.DAY_OF_MONTH,-1);
    }else if(choosetimetype==2){//本季
        start_calendar.add(Calendar.MONTH,-3);
        end_calendar.add(Calendar.DAY_OF_MONTH,1);
        end_calendar.add(Calendar.MONTH,-3);
        end_calendar.add(Calendar.DAY_OF_MONTH,-1);
    }else if(choosetimetype==3){//本年
        start_calendar.add(Calendar.YEAR,-1);
        end_calendar.add(Calendar.YEAR,-1);
    }
    end_calendar.set(Calendar.HOUR_OF_DAY, 23);
    end_calendar.set(Calendar.MINUTE, 59);
    end_calendar.set(Calendar.SECOND, 59);
    end_calendar.set(Calendar.MILLISECOND, 999);
    start_time = start_calendar.getTimeInMillis();
    end_time = end_calendar.getTimeInMillis();
    if(choosetimetype==4){//自定义
        long init_start_time=start_time;
        long init_end_time=end_time;
        // end_time-start_time;
        end_time=init_start_time-1;
        start_time=end_time-(init_end_time-init_start_time);

    }
}
private void SetNext(){
    Calendar start_calendar = Calendar.getInstance();
    Calendar end_calendar = Calendar.getInstance();
    start_calendar.setTimeInMillis(start_time);
    end_calendar.setTimeInMillis(end_time);
    if(choosetimetype==0){//本日
        start_calendar.add(Calendar.DAY_OF_MONTH, 1);
        end_calendar.add(Calendar.DAY_OF_MONTH, 1);
    }else if(choosetimetype==1) {//本月
        start_calendar.add(Calendar.MONTH,1);
        end_calendar.add(Calendar.DAY_OF_MONTH,1);
        end_calendar.add(Calendar.MONTH,1);
        end_calendar.add(Calendar.DAY_OF_MONTH,-1);
    }else if(choosetimetype==2){//本季
        start_calendar.add(Calendar.MONTH,3);
        end_calendar.add(Calendar.DAY_OF_MONTH,1);
        end_calendar.add(Calendar.MONTH,3);
        end_calendar.add(Calendar.DAY_OF_MONTH,-1);
    }else if(choosetimetype==3){//本年
        start_calendar.add(Calendar.YEAR,1);
        end_calendar.add(Calendar.YEAR,1);
    }
    end_calendar.set(Calendar.HOUR_OF_DAY, 23);
    end_calendar.set(Calendar.MINUTE, 59);
    end_calendar.set(Calendar.SECOND, 59);
    end_calendar.set(Calendar.MILLISECOND, 999);
    start_time = start_calendar.getTimeInMillis();
    end_time = end_calendar.getTimeInMillis();
    if(choosetimetype==4){//自定义
        long init_start_time=start_time;
        long init_end_time=end_time;
        start_time=init_end_time+1;
        end_time=start_time+(init_end_time-init_start_time);
    }
}
    /*
    对话框弹出
     */
    public void show_dialog(int i) {
        //获取屏幕的宽高
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;
        //进行dialog宽高的设置
        Window dialogWindow = timedialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //设置弹出动画
        //  dialogWindow.setWindowAnimations(R.style.main_menu_animStyle);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = i;
        lp.width = (int) (width * 0.8);
        dialogWindow.setAttributes(lp);
        timedialog.show();

    }

    /*
    进行获得收入支出类别+时间范围的所有消费
     */
    private void GetAllConsume(String iore, long start, long end) {//收入或支出  开始时间  结束时间
        GetConsumetype(iore);
        if (iore.equals(chooseString[0])) {
            cursor = db.query("expendinfo", null, "User_Name=?"
                    , new String[]{Username}, null, null, null);
        } else if (iore.equals(chooseString[1])) {
            cursor = db.query("incomeinfo", null, "User_Name=?"
                    , new String[]{Username}, null, null, null);
        }
        double[] consumemoney = new double[11];
        for (int i = 0; i < consumetype.size(); i++) {
            consumemoney[i] = 0;
        }
        if (cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(cursor.getColumnIndex("Time"));
                if ((time >= start) && (time <= end)) {
                    for (int i = 0; i < consumetype.size(); i++) {
                        if (consumetype.get(i).equals(cursor.getString(cursor.getColumnIndex("Type")))) {
                            consumemoney[i] += Double.valueOf(cursor.getString(cursor.getColumnIndex("Money")));
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        //  Log.d("liangjialing", "GetAllConsume");
        for (double list : consumemoney) {
            //    Log.d("liangjialing", "GetAllConsume_consumemoney:    " + list);
        }

        //金额占比计算
        double totalmoney = 0;
        for (int i = 0; i < consumetype.size(); i++) {
            totalmoney += consumemoney[i];
        }
        float[] consumepercent = new float[11];
        for (int i = 0; i < consumetype.size(); i++) {
            if (consumemoney[i] == 0) {
                consumepercent[i] = -1;
            } else {
                consumepercent[i] = (float) (consumemoney[i] / totalmoney);
            }
        }

        //  Log.d("liangjialing", "moneypercent");
        for (float list : consumepercent) {
            // Log.d("liangjialing", "GetAllConsume_moneypercent:    " + list);
        }
        Create_Legend(consumemoney, consumetype);
        create_PieChart(consumepercent, totalmoney);
    }

    /*
        得到消费类型
         */
    private void GetConsumetype(String iore) {
        consumetype.clear();
        if (iore.equals(chooseString[0])) {
            cursor = db.query("expendtype", null, null
                    , null, null, null, null);
        } else if (iore.equals(chooseString[1])) {
            cursor = db.query("incometype", null, null
                    , null, null, null, null);
        }
        if (cursor.moveToFirst()) {
            do {
                consumetype.add(cursor.getString(cursor.getColumnIndex("Type_Name")));
            } while (cursor.moveToNext());
        }
        // Log.d("liangjialing", "GetConsumtype");
        for (String list : consumetype) {
            //    Log.d("liangjialing", "GetConsumtype:    " + list);
        }

    }

    /*
    进行饼状图的创建
     */
    private void create_PieChart(float[] percent, double total) {
        entries.clear();
        for (int i = 0; i < consumetype.size(); i++) {
            if (percent[i] != (-1.0)) {
                entries.add(new PieEntry(percent[i], consumetype.get(i)));
                // Log.d("liangjialing", "entry  :    " + entries.get(i).getValue()+"  "+ entries.get(i).getLabel());
            }
        }
        if (entries.isEmpty()) {//此时没有数据进行显示
            setPieChart(pieChart, "暂无数据");
        } else {
            setPieChart(pieChart, "合计:\n" + total);
        }
        setPieChartData(pieChart, entries);


    }

    // -->设置饼图数据
    public void setPieChart(PieChart pieChart, String title) {
        pieChart.setUsePercentValues(true);//设置使用百分比（后续有详细介绍）
        pieChart.getDescription().setEnabled(false);//设置描述,没有描述
        pieChart.setDrawEntryLabels(false);//设置pieChart是否只显示饼图上百分比不显示文字
        pieChart.setExtraOffsets(40, 40, 40, 40); //设置边距
        pieChart.setDragDecelerationFrictionCoef(0.85f);//设置摩擦系数（值越小摩擦系数越大）
        pieChart.setRotationEnabled(true);//是否可以旋转
        pieChart.setHighlightPerTapEnabled(true);//点击是否放大
        pieChart.setDrawCenterText(true);//设置绘制环中文字
        pieChart.setCenterText(title);//设置环中的文字
        pieChart.setCenterTextSize(22f);//设置环中文字的大小
        pieChart.setRotationAngle(120f);//设置旋转角度
        //pieChart.setTransparentCircleRadius(100f);//设置半透明圆环的半径,看着就有一种立体的感觉
        //这个方法为true就是环形图，为false就是饼图
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);//设置中间圆盘的半径,值为所占饼图的百分比
        pieChart.setTransparentCircleRadius(45f);//设置中间透明圈的半径,值为所占饼图的百分比
        //设置环形中间空白颜色是白色
        pieChart.setHoleColor(Color.WHITE);
        //设置半透明圆环的颜色
        pieChart.setTransparentCircleColor(Color.LTGRAY);
        //设置半透明圆环的透明度
        pieChart.setTransparentCircleAlpha(125);
        //数据显示动画
        pieChart.animateX(1500, Easing.EasingOption.EaseInOutQuad);

        //图例设置
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);//是否显示图例
        legend.setFormSize(20f);
        legend.setTextSize(20f);
        legend.setForm(Legend.LegendForm.SQUARE);

        legend.setFormToTextSpace(4f);//设置图例标签和响应的图例形式之间的控件
        legend.setXEntrySpace(30f);//设置水平轴上图例条目之间的间隔
        legend.setYEntrySpace(20f);//设置垂直轴上图例条目之间的间隔
        legend.setWordWrapEnabled(true);//lend是否换行
        legend.setMaxSizePercent(0.9f);//设置换行最大百分比
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);//图例相对于图表横向的位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);//图例相对于图表纵向的位置
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);//图例显示的方向
        legend.setDrawInside(false);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);

    }

    private void setPieChartData(PieChart pieChart, List<PieEntry> list) {

        PieDataSet dataSet = new PieDataSet(list, "");
        dataSet.setHighlightEnabled(true);
        dataSet.setSliceSpace(1f);//设置饼块之间的间隔
        dataSet.setSelectionShift(5f);//设置饼块选中时偏离饼图中心的距离
        dataSet.setColors(PIE_COLORS);//设置饼块的颜色
        //设置数据显示方式有见图
        dataSet.setValueLinePart1OffsetPercentage(90f);//数据连接线距图形片内部边界的距离，为百分数
        dataSet.setValueLinePart1Length(0.6f);//当值位置为外边线时，表示线的前半段长度。
        dataSet.setValueLinePart2Length(0.2f);// 当值位置为外边线时，表示线的后半段长度。
        dataSet.setValueLineColor(Color.BLACK);//设置连接线的颜色
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());//值设置成百分比
        pieData.setValueTextSize(15f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

    /*
    饼图标签的设置
     */
    private void Set_Legend() {
        piechart_legend[0] = (TextView) findViewById(R.id.piechart_legend1);
        piechart_legend[1] = (TextView) findViewById(R.id.piechart_legend2);
        piechart_legend[2] = (TextView) findViewById(R.id.piechart_legend3);
        piechart_legend[3] = (TextView) findViewById(R.id.piechart_legend4);
        piechart_legend[4] = (TextView) findViewById(R.id.piechart_legend5);
        piechart_legend[5] = (TextView) findViewById(R.id.piechart_legend6);
        piechart_legend[6] = (TextView) findViewById(R.id.piechart_legend7);
        Legend_TextView LT = new Legend_TextView();
        for (TextView textView : piechart_legend) {
            textView.setOnClickListener(LT);
        }
    }

    /*
    饼图标签的创建
     */
    private void Create_Legend(double[] money, List<String> type) {
        List<Integer> legend_number = new ArrayList<>();
        for (int i = 0; i < type.size(); i++) {
            if (money[i] != 0) {
                //记录
                legend_number.add(i);
            }
        }
        for (int i = 0; i < piechart_legend.length; i++) {
            if (i < legend_number.size()) {
                piechart_legend[i].setVisibility(View.VISIBLE);
                piechart_legend[i].setText(type.get(legend_number.get(i)));
                //加入颜色
                drawable = getResources().getDrawable(LEGEND_COLORS[i]);
                drawable.setBounds(0, 0, 30, 30);
                piechart_legend[i].setCompoundDrawables(drawable, null, null, null);
                piechart_legend[i].setCompoundDrawablePadding(10);
            } else {
                piechart_legend[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    /*
    标签点击响应
     */
    private class Legend_TextView implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            TextView tv = (TextView) view;
            //tv.getText().toString()
            Toast.makeText(StatisticalActivity.this, tv.getText().toString(), Toast.LENGTH_SHORT).show();
            Activity_Change(Username, choosebutton.getText().toString(), tv.getText().toString(), start_time, end_time);
        }
    }


    /*
    活动跳转
     */
    private void Activity_Change(String username, String iore, String type_name, long start, long end) {
        intent = new Intent(StatisticalActivity.this, StatisticalEditorActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("iore", iore);
        intent.putExtra("type_name", type_name);
        intent.putExtra("start", start);
        intent.putExtra("end", end);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1://重新刷新一遍
                GetAllConsume(choosebutton.getText().toString(), start_time, end_time);
                break;
            default:
                break;
        }
    }

    /*
    获取当天的时间
     */
    private void GetDay() {
        Calendar start_calendar = Calendar.getInstance();
        start_calendar.set(Calendar.HOUR_OF_DAY, 00);
        start_calendar.set(Calendar.MINUTE, 00);
        start_calendar.set(Calendar.SECOND, 00);
        start_calendar.set(Calendar.MILLISECOND, 00);
        start_time = start_calendar.getTimeInMillis();

        Calendar end_calendar = Calendar.getInstance();
        end_calendar.set(Calendar.HOUR_OF_DAY, 23);
        end_calendar.set(Calendar.MINUTE, 59);
        end_calendar.set(Calendar.SECOND, 59);
        end_calendar.set(Calendar.MILLISECOND, 999);
        end_time = end_calendar.getTimeInMillis();
        // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
    }

    /*
        获取当月的时间
         */
    private void GetMonth() {
        Calendar calendar = Calendar.getInstance();
        start_time = start_date(calendar.get(Calendar.MONTH));
        end_time = end_date(calendar.get(Calendar.MONTH));
        // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
    }

    /*
    获取当季节的时间
     */
    private void GetSeason() {
        Calendar calendar = Calendar.getInstance();
        switch (calendar.get(Calendar.MONTH) + 1) {
            case 1:
            case 2:
            case 3:
                start_time = start_date(0);
                end_time = end_date(2);
                // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            case 4:
            case 5:
            case 6:
                start_time = start_date(3);
                end_time = end_date(5);
                // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            case 7:
            case 8:
            case 9:
                start_time = start_date(6);
                end_time = end_date(8);
                // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            case 10:
            case 11:
            case 12:
                start_time = start_date(9);
                end_time = end_date(11);
                //Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            default:
                break;
        }
    }

    /*
    获取当年的时间
     */
    private void GetYear() {
        start_time = start_date(0);
        end_time=end_date(11);
    }

    private long start_date(int startmonth) {
        Calendar start_calendar = Calendar.getInstance();
        start_calendar.set(Calendar.MONTH, startmonth);
        start_calendar.set(Calendar.DAY_OF_MONTH, 1);
        start_calendar.set(Calendar.HOUR_OF_DAY, 00);
        start_calendar.set(Calendar.MINUTE, 00);
        start_calendar.set(Calendar.SECOND, 00);
        start_calendar.set(Calendar.MILLISECOND, 00);
        return start_calendar.getTimeInMillis();
    }

    private long end_date(int endmonth) {
        Calendar end_calendar = Calendar.getInstance();
        end_calendar.set(Calendar.MONTH, endmonth);
        end_calendar.set(Calendar.DAY_OF_MONTH, 1);//日期设置为1号
        end_calendar.add(Calendar.MONTH,1);
        end_calendar.add(Calendar.DAY_OF_MONTH, -1);//倒回到前一天
        end_calendar.set(Calendar.HOUR_OF_DAY, 23);
        end_calendar.set(Calendar.MINUTE, 59);
        end_calendar.set(Calendar.SECOND, 59);
        end_calendar.set(Calendar.MILLISECOND, 999);
        return end_calendar.getTimeInMillis();
    }

    /*
    获取列表的高度
     */
    public static int getListHeight(ListView list) {
        ListAdapter listAdapter = list.getAdapter();
        if (listAdapter == null) {
            return 0;
        }
        int totalHeight = 0;
        int count = listAdapter.getCount();
        for (int i = 0; i < count; i++) {
            View listItem = listAdapter.getView(i, null, list);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        return totalHeight;

    }

    /*
     *时间数据类型转换
     */
    private String LongToString(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间HH:mm:ss SSS
        String sDateTime = new SimpleDateFormat("yyyy.MM.dd").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }
}
