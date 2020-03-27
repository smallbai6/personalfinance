package com.personalfinance.app.Time_Type;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.personalfinance.app.R;

import java.util.Calendar;

public class TimePop extends PopupWindow implements View.OnClickListener{
    private Button buttoncancel;
    private PopupWindow timepop;
    private Calendar mDate=Calendar.getInstance();
    private int select_mYear,select_mMonth,select_mDay,select_mHour,select_mMinute;
    private TimeChoose mTimeChoose;
    private OnDateTimeSetListener mOnDateTimeSetListener;//接口
    private Activity context;
    public TimePop(final Activity context, View parent,String selectTime) {
        super(context);
        this.context=context;
        select_mYear = Integer.valueOf(selectTime.substring(0,4));
        select_mMonth = Integer.valueOf(selectTime.substring(5,7));
        select_mDay=Integer.valueOf(selectTime.substring(8,10));
        select_mHour=Integer.valueOf(selectTime.substring(12,14));
        select_mMinute=Integer.valueOf(selectTime.substring(15,17));
        mDate.set(select_mYear,select_mMonth-1,select_mDay,select_mHour,select_mMinute);




        mTimeChoose=new TimeChoose(context,selectTime,0);
        buttoncancel=(Button)mTimeChoose.findViewById(R.id.time_cancel);

        timepop = new PopupWindow(mTimeChoose,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timepop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        //timepop.setOutsideTouchable(true);
        //timepop.setTouchable(true);
       timepop.setFocusable(true);

        buttoncancel.setOnClickListener(this);

        timepop.showAtLocation(parent, Gravity.BOTTOM,0,0);


        timepop.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.OnDateTimeSet(mDate.getTimeInMillis());
                   // Log.d("liang", "弹窗消失" + mDate.get(Calendar.MONTH));
                }
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
                mDate.set(Calendar.MILLISECOND, 0);
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.OnDateTimeSet(mDate.getTimeInMillis());
               }
               // Log.d("liang", "onDateTimeChanged  " + mDate.get(Calendar.MONTH));
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