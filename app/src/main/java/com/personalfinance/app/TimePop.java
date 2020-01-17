package com.personalfinance.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Calendar;

public class TimePop extends PopupWindow implements View.OnClickListener{
    private Button buttoncancel;
    private PopupWindow timepop;
    private Calendar mDate=Calendar.getInstance();
    private TimeChoose mTimeChoose;
    private OnDateTimeSetListener mOnDateTimeSetListener;//接口
    TextView text;
    private Activity context;
    public TimePop(final Activity context, View parent) {
        super(context);
        this.context=context;
        mTimeChoose=new TimeChoose(context);
        buttoncancel=(Button)mTimeChoose.findViewById(R.id.time_cancel);
        timepop = new PopupWindow(mTimeChoose,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timepop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        timepop.setOutsideTouchable(true);
        timepop.setTouchable(true);
        timepop.showAtLocation(parent, Gravity.BOTTOM,0,0);
        buttoncancel.setOnClickListener(this);
        timepop.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.OnDateTimeSet(mDate.getTimeInMillis());
                }
               // long currentTime=mDate.getTimeInMillis();
                //textView.setText(datetypechange(currentTime));
            }
        });
        mTimeChoose.setOnDateTimeChangedListener(new TimeChoose.OnDateTimeChangedListener() {
            @Override
            public void onDateTimeChanged(TimeChoose view, int year, int month, int day,int hour,int minute) {
                mDate.set(Calendar.YEAR,year);
                mDate.set(Calendar.MONTH,month);
                mDate.set(Calendar.DAY_OF_MONTH,day);
                mDate.set(Calendar.HOUR_OF_DAY,hour);
                mDate.set(Calendar.MINUTE,minute);
                mDate.set(Calendar.SECOND,0);
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.OnDateTimeSet(mDate.getTimeInMillis());
                }
               // long currentTime=mDate.getTimeInMillis();
                //textView.setText(datetypechange(currentTime));
            }
        });
   }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.time_cancel:
                timepop.dismiss();
                break;
        }
    }

    public interface OnDateTimeSetListener {//接口
        void OnDateTimeSet(long date);//方法
    }
    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {//回调
        mOnDateTimeSetListener = callBack;
    }
}
/*
public class TimePop extends PopupWindow  {
    private Button btnCancel;
    private Calendar mDate=Calendar.getInstance();
    private TimeChoose mTimeChoose;
    private Activity context;
    private OnDateTimeSetListener mOnDateTimeSetListener;
    public TimePop(final Activity context, View parent){
        super(context);
        this.context=context;
        mTimeChoose=new TimeChoose(context);
        mTimeChoose.setOnDateTimeChangedListener(new TimeChoose.OnDateTimeChangedListener() {
            @Override
            public void onDateTimeChanged(TimeChoose view, int year, int month, int day, int hour, int minute) {
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, day);
                mDate.set(Calendar.HOUR_OF_DAY, hour);
                mDate.set(Calendar.MINUTE, minute);
                mDate.set(Calendar.SECOND, 0);
            }
        });
        btnCancel=(Button)mTimeChoose.findViewById(R.id.time_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        this.setContentView(mTimeChoose);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xffffffff);
        this.setBackgroundDrawable(dw);
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public interface OnDateTimeSetListener {
        void OnDateTimeSet(long date);
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        mOnDateTimeSetListener = callBack;
    }

}*/