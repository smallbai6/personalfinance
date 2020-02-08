package com.personalfinance.app.Time_Type;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.personalfinance.app.R;

import java.lang.reflect.Field;
import java.util.Calendar;

public class TimeChoose extends FrameLayout {
    private final NumberPicker mYearSpinner;
    private final NumberPicker mMonthSpinner;
    private final NumberPicker mDaySpinner;
    private final NumberPicker mHourSpinner;
    private final NumberPicker mMinuteSpinner;
    private Calendar mDate;
    private int mYear, mMonth,mHour,mMinute;
    private int select_mYear,select_mMonth,select_mDay,select_mHour,select_mMinute;
    private String[] mYearDisplayValues = new String[152];
    private String[] mMonthDisplayValues = new String[12];
    private String[] mDateDisplayValues = new String[7];
    private OnDateTimeChangedListener mOnDateTimeChangedListener;

    public TimeChoose(Context context,String selectTime) {
        super(context);
        Log.d("liang","进入TimeChoose");
        select_mYear = Integer.valueOf(selectTime.substring(0,4));
        select_mMonth = Integer.valueOf(selectTime.substring(5,7));
        select_mDay=Integer.valueOf(selectTime.substring(8,10));
        select_mHour=Integer.valueOf(selectTime.substring(12,14));
        select_mMinute=Integer.valueOf(selectTime.substring(15));
        Log.d("liang","selectTime="+selectTime);
       // Log.d("liang",selectTime.substring(0,4)+" "+selectTime.substring(5,7)+" "+selectTime.substring(8,10)+
       //         " "+selectTime.substring(12,14)+" "+selectTime.substring(15));
       // Log.d("liang",select_mYear+" "+select_mMonth+" "+" "+select_mDay+" "+select_mHour+" "+select_mMinute);
        mDate = Calendar.getInstance();
        mYear = mDate.get(Calendar.YEAR);
        mMonth = mDate.get(Calendar.MONTH);
       // mHour=mDate.get(Calendar.HOUR_OF_DAY);
       // mMinute=mDate.get(Calendar.MINUTE);
        mHour=select_mHour;
        mMinute=select_mMinute;
        inflate(context, R.layout.time, this);
        mYearSpinner = (NumberPicker) this.findViewById(R.id.time_year);
        setNumberPickerDividerColor(mYearSpinner);
        mYearSpinner.getChildAt(0).setFocusable(false);
        mYearSpinner.setMinValue(mYear - 71);
        mYearSpinner.setMaxValue(mYear + 80);
        updateYearControl();
        mYearSpinner.setValue(select_mYear);
        mYearSpinner.setWrapSelectorWheel(true);//设置为不可循环
        mYearSpinner.setOnValueChangedListener(mOnYearChangedListener);

        mMonthSpinner = (NumberPicker) this.findViewById(R.id.time_month);
        setNumberPickerDividerColor(mMonthSpinner);
        mMonthSpinner.getChildAt(0).setFocusable(false);
        mMonthSpinner.setMaxValue(12);
        mMonthSpinner.setMinValue(1);
        updateMonthControl();
        mMonthSpinner.setValue(select_mMonth);
        mMonthSpinner.setWrapSelectorWheel(true);
        mMonthSpinner.setOnValueChangedListener(mOnMonthChangedListener);

        mDate.set(select_mYear, select_mMonth, select_mDay,select_mHour,select_mMinute);
        mDaySpinner = (NumberPicker) this.findViewById(R.id.time_day);
        setNumberPickerDividerColor(mDaySpinner);
        mDaySpinner.getChildAt(0).setFocusable(false);
        mDaySpinner.setMaxValue(6);
        mDaySpinner.setMinValue(0);
        updateDateControl();
       // mDaySpinner.setValue(select_mDay);
        mDaySpinner.setWrapSelectorWheel(true);
        mDaySpinner.setOnValueChangedListener(mOnDayChangedListener);

        mHourSpinner = (NumberPicker) this.findViewById(R.id.time_hour);
        setNumberPickerDividerColor(mHourSpinner);
        mHourSpinner.getChildAt(0).setFocusable(false);
        mHourSpinner.setMaxValue(23);
        mHourSpinner.setMinValue(0);
        mHourSpinner.setValue(select_mHour);
       // mHourSpinner.setValue(mHour);
        mHourSpinner.setWrapSelectorWheel(true);
        mHourSpinner.setOnValueChangedListener(mOnHourChangedListener);

        mMinuteSpinner = (NumberPicker) this.findViewById(R.id.time_minute);
        setNumberPickerDividerColor(mMinuteSpinner);
        mMinuteSpinner.getChildAt(0).setFocusable(false);
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setMinValue(0);
        mMinuteSpinner.setValue(select_mMinute);
       // mMinuteSpinner.setValue(mMinute);
        mMinuteSpinner.setWrapSelectorWheel(true);
        mMinuteSpinner.setOnValueChangedListener(mOnMinuteChangedListener);
       // Log.d("liang", "onValueChangedListener  " + mDate.get(Calendar.YEAR));

    }

    private OnValueChangeListener mOnYearChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mYear = mYearSpinner.getValue();
            mDate.set(mYear, mMonth, mDate.get(Calendar.DAY_OF_MONTH),mHour,mMinute);
            updateDateControl();
            onDateTimeChanged();
        }
    };
    private OnValueChangeListener mOnMonthChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mMonth = mMonthSpinner.getValue() - 1;
            mDate.set(mYear, mMonth, mDate.get(Calendar.DAY_OF_MONTH),mHour,mMinute);
            updateDateControl();
            onDateTimeChanged();
        }
    };
    private OnValueChangeListener mOnDayChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
            mDate.set(mYear, mMonth, mDate.get(Calendar.DAY_OF_MONTH),mHour,mMinute);
            updateDateControl();
            onDateTimeChanged();
        }
    };
    private OnValueChangeListener mOnHourChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mHour=mHourSpinner.getValue();
            mDate.set(mYear, mMonth, mDate.get(Calendar.DAY_OF_MONTH),mHour,mMinute);
            onDateTimeChanged();
        }
    };

    private OnValueChangeListener mOnMinuteChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mMinute=mMinuteSpinner.getValue();
            mDate.set(mYear, mMonth, mDate.get(Calendar.DAY_OF_MONTH),mHour,mMinute);
            onDateTimeChanged();
        }
    };


    private void updateYearControl() {//将年范围数组添加完毕
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.YEAR, 1);
        mYearSpinner.setDisplayedValues(null);
        for (int i = 71; i >= 0; --i) {
            cal.add(Calendar.YEAR, -1);
            mYearDisplayValues[i] = (String) DateFormat.format("yyyy年", cal);
        }
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.YEAR, 0);
        for (int i = 72; i < 152; ++i) {
            cal.add(Calendar.YEAR, 1);
            mYearDisplayValues[i] = (String) DateFormat.format("yyyy年", cal);
        }

        mYearSpinner.setDisplayedValues(mYearDisplayValues);//设置显示值
        mYearSpinner.setValue(mYear);
        mYearSpinner.invalidate();
    }
    private void updateMonthControl() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.MONTH, -1);
        mMonthSpinner.setDisplayedValues(null);
        for (int i = 0; i < 12; ++i) {
            mMonthDisplayValues[i] = (i + 1) + "月";
        }
        mMonthSpinner.setDisplayedValues(mMonthDisplayValues);
        mMonthSpinner.setValue(mMonth + 1);
        mMonthSpinner.invalidate();
    }
    private void updateDateControl() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.DAY_OF_MONTH, -7 / 2 - 1);
        mDaySpinner.setDisplayedValues(null);
        for (int i = 0; i < 7; ++i) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            //   mDateDisplayValues[i] = (String) DateFormat.format("dd日", cal);
            mDateDisplayValues[i] = (String) DateFormat.format("d日 E", cal);
        }
        mDaySpinner.setDisplayedValues(mDateDisplayValues);
        mDaySpinner.setValue(7 / 2);
        mDaySpinner.invalidate();
    }


    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(TimeChoose view, int year, int month, int day, int hour, int minute);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) {
        mOnDateTimeChangedListener = callback;
    }

    private void onDateTimeChanged() {
        if (mOnDateTimeChangedListener != null) {
            mOnDateTimeChangedListener.onDateTimeChanged(this, mYear,
                    mMonth, mDate.get(Calendar.DAY_OF_MONTH),mDate.get(Calendar.HOUR_OF_DAY),mDate.get(Calendar.MINUTE));
        }
    }

    private void setNumberPickerDividerColor(NumberPicker numberPicker) {
        NumberPicker picker = numberPicker;
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(picker, new ColorDrawable(this.getResources().getColor(R.color.colorwhitegray)));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                Field selectorWheelPaintField;
                try {
                    selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    try {
                        ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}