package com.personalfinance.app.Time_Type;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.personalfinance.app.R;
import com.personalfinance.app.Time_Type.TimeChoose;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TimeZDYPop extends PopupWindow implements View.OnClickListener {
    private View Parent;
    private int select_mYear, select_mMonth, select_mDay, select_mHour, select_mMinute;
    private String Time;//时间选择器上显示内容
    private Calendar mDate = Calendar.getInstance();
    private Activity context;
    private PopupWindow timePopupWindow;
    private TextView time_sure, time_cancel;
    private boolean sore = true;
    private LinearLayout startlayout, endlayout;
    private TextView starttext, endtext;
    private TimeChoose mTimeChoose,mTimeChooseb;
    private OnDateTimeSetListener mOnDateTimeSetListener;//接口

    private long startlong, endlong;//记录开始和结束的毫秒值


    public TimeZDYPop(final Activity context, View parent, long inputstart, long inputend) {
        super(context);
        this.context = context;
        Parent=parent;
        //2020.01.15-2020.12.25
        //一开始就点击的是开始时间
        ClickStart(LongToString(inputstart));
        startlong=inputstart;
        endlong=inputend;
        mTimeChoose = new TimeChoose(context, Time, 1);
        time_sure = (TextView) mTimeChoose.findViewById(R.id.time_sure);
        time_sure.setOnClickListener(this);
        time_cancel = (TextView)mTimeChoose.findViewById(R.id.time_cancel);
        time_cancel.setOnClickListener(this);
        startlayout = (LinearLayout) mTimeChoose.findViewById(R.id.statistical_zidingyi_start);
        startlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhitewhitegray));
        startlayout.setOnClickListener(this);
        endlayout = (LinearLayout) mTimeChoose.findViewById(R.id.statistical_zidingyi_end);
        endlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhite));
        endlayout.setOnClickListener(this);
        starttext = (TextView) mTimeChoose.findViewById(R.id.statistical_starttime);
        starttext.setText(LongToString(inputstart));
        endtext = (TextView) mTimeChoose.findViewById(R.id.statistical_endtime);
        endtext.setText(LongToString(inputend));


        timePopupWindow = new PopupWindow(mTimeChoose,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        timePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        timePopupWindow.setOutsideTouchable(true);
        timePopupWindow.setTouchable(true);
        timePopupWindow.setFocusable(true);
        timePopupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        timePopupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                //不作任何更改
            }
        });
        Listener();
        // Log.d("tiancai", mTimeChoose.getWidth()+"   dialog.show()   "+ mTimeChoose.getHeight());
    }
    /*private void Init(View parent){
        mTimeChoose = new TimeChoose(context, Time, 1);
        time_sure = (TextView) mTimeChoose.findViewById(R.id.time_sure);
        time_sure.setOnClickListener(this);
        time_cancel = (TextView)mTimeChoose.findViewById(R.id.time_cancel);
        time_cancel.setOnClickListener(this);
        startlayout = (LinearLayout) mTimeChoose.findViewById(R.id.statistical_zidingyi_start);
        startlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhitewhitegray));
        startlayout.setOnClickListener(this);
        endlayout = (LinearLayout) mTimeChoose.findViewById(R.id.statistical_zidingyi_end);
        endlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhite));
        endlayout.setOnClickListener(this);
        starttext = (TextView) mTimeChoose.findViewById(R.id.statistical_starttime);
        endtext = (TextView) mTimeChoose.findViewById(R.id.statistical_endtime);


        timePopupWindow = new PopupWindow(mTimeChoose,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        timePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        timePopupWindow.setOutsideTouchable(true);
        timePopupWindow.setTouchable(true);
        timePopupWindow.setFocusable(true);
        timePopupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        timePopupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                //不作任何更改
            }
        });
        Listener();
        // Log.d("tiancai", mTimeChoose.getWidth()+"   dialog.show()   "+ mTimeChoose.getHeight());

    }*/
    private void ClickStart(String kaishi) {
        select_mYear = Integer.valueOf(kaishi.substring(0, 4));
        select_mMonth = Integer.valueOf(kaishi.substring(5, 7));
        select_mDay = Integer.valueOf(kaishi.substring(8));
        //Log.d("liangjialing",select_mYear+"   "+select_mMonth+"    "+select_mDay);
        Time = kaishi + "  " + "00:00";
        select_mHour = 00;
        select_mMinute = 00;
        mDate.set(select_mYear, select_mMonth - 1, select_mDay, select_mHour, select_mMinute);
        sore = true;
    }

    private void ClickEnd(String jieshu) {
        select_mYear = Integer.valueOf(jieshu.substring(0, 4));
        select_mMonth = Integer.valueOf(jieshu.substring(5, 7));
        select_mDay = Integer.valueOf(jieshu.substring(8));
        //Log.d("liangjialing",select_mYear+"   "+select_mMonth+"    "+select_mDay);
        Time = jieshu + "  " + "23:59";
        select_mHour = 23;
        select_mMinute = 59;
        mDate.set(select_mYear, select_mMonth - 1, select_mDay, select_mHour, select_mMinute);
        sore = false;
    }

    private void Listener() {
        mTimeChoose.setOnDateTimeChangedListener(new TimeChoose.OnDateTimeChangedListener() {
            @Override
            public void onDateTimeChanged(TimeChoose view, int year, int month, int day, int hour, int minute) {
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, day);

                if (sore) {
                    mDate.set(Calendar.HOUR_OF_DAY, 0);
                    mDate.set(Calendar.MINUTE, 0);
                    mDate.set(Calendar.SECOND, 0);
                    mDate.set(Calendar.MILLISECOND, 0);
                    startlong = mDate.getTimeInMillis();
                    starttext.setText(LongToString(startlong));
                } else {
                    mDate.set(Calendar.HOUR_OF_DAY, 23);
                    mDate.set(Calendar.MINUTE, 59);
                    mDate.set(Calendar.SECOND, 59);
                    mDate.set(Calendar.MILLISECOND, 999);
                    endlong = mDate.getTimeInMillis();
                    endtext.setText(LongToString(endlong));
                }
                /*if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.OnDateTimeSet(mDate.getTimeInMillis());
                }*/
                // Log.d("liang", "onDateTimeChanged  " + mDate.get(Calendar.MONTH));
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time_sure:
                //数据更改回调
                timePopupWindow.dismiss();
                if (endlong >= startlong) {//进行回调
                    if (mOnDateTimeSetListener != null) {
                        mOnDateTimeSetListener.OnDateTimeSet(startlong, endlong);
                    }
                }
                break;
            case R.id.time_cancel://点击取消
                //不作任何改动
                timePopupWindow.dismiss();
                break;
            case R.id.statistical_zidingyi_start://点击开始时间
                //获得显示开始地方的string
                ClickStart(starttext.getText().toString());
                Log.d("showtime", "start  " + Time);
                mTimeChoose = new TimeChoose(context, Time, 1);
                //Init(Parent);
                startlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhitewhitegray));
                endlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhite));
                break;
            case R.id.statistical_zidingyi_end://点击结束时间
                ClickEnd(endtext.getText().toString());
                Log.d("showtime", "end  " + Time);
                mTimeChoose = new TimeChoose(context, Time, 1);
                //Init(Parent);
                startlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhite));
                endlayout.setBackgroundColor(context.getResources().getColor(R.color.colorwhitewhitegray));

                break;
        }
    }

    public interface OnDateTimeSetListener {//接口

        void OnDateTimeSet(long start, long end);//方法
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {//回调
        mOnDateTimeSetListener = callBack;
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
