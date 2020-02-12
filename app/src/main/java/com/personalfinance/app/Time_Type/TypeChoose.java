package com.personalfinance.app.Time_Type;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.personalfinance.app.R;

import java.lang.reflect.Field;

public class TypeChoose extends FrameLayout {
    private final NumberPicker mTypeSpinner;
    private int mType;
    private OnTypeChangedListener mOnTypeChangedListener;
    public TypeChoose(Context context, String[] List, int position) {
        super(context);
        mType=position;
        inflate(context, R.layout.type, this);
        mTypeSpinner = (NumberPicker) this.findViewById(R.id.type_type);
        setNumberPickerDividerColor(mTypeSpinner);
        mTypeSpinner.getChildAt(0).setFocusable(false);
        mTypeSpinner.setMinValue(0);
        mTypeSpinner.setMaxValue(List.length-1);
        mTypeSpinner.setDisplayedValues(List);
        mTypeSpinner.setValue(mType);
        mTypeSpinner.setWrapSelectorWheel(true);//设置为不可循环
        mTypeSpinner.setOnValueChangedListener(mOnChangedListener);
    }

    private OnValueChangeListener mOnChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mType = mTypeSpinner.getValue();
           // Log.d("liang",mType+"??");
            onTypeChanged();
        }
    };

    public interface OnTypeChangedListener {
        void onTypeChanged(TypeChoose view, int type);
    }

    public void setOnTypeChangedListener(OnTypeChangedListener callback) {
        mOnTypeChangedListener = callback;
    }

    private void onTypeChanged() {
        if (mOnTypeChangedListener != null) {
            mOnTypeChangedListener.onTypeChanged(this, mType);
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