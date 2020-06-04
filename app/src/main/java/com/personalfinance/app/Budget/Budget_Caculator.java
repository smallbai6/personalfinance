package com.personalfinance.app.Budget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.personalfinance.app.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Budget_Caculator extends FrameLayout implements View.OnClickListener {

    private OnCaculatorSetListener mOnCaculatorSetListener;//回调
    private PopupWindow moneyPop;
    private Activity context;
    private View contentView;

    private TextView budget_result;


    private RelativeLayout[] btnNum = new RelativeLayout[11];// 数值按钮
    private RelativeLayout Sure;//确定
    private ImageView backspace,closepop;

    private String result="";

    @SuppressLint("WrongViewCast")
    public Budget_Caculator(final Activity context, View parent) {
        super(context);
        this.context = context;
        contentView = View.inflate(context, R.layout.budgetcaculator_pop, null);
        //弹窗设置
        moneyPop = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        moneyPop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        moneyPop.setOutsideTouchable(true);
        moneyPop.showAtLocation(parent, Gravity.BOTTOM, 0, 0);


        // 获取运算符确定 删除
        Sure = (RelativeLayout) contentView.findViewById(R.id.finance_equal);
        Sure.setOnClickListener(this);
        backspace = (ImageView) contentView.findViewById(R.id.finance_backspace);
        backspace.setOnClickListener(this);
        // 获取数字
        btnNum[0] = (RelativeLayout) contentView.findViewById(R.id.finance_num0);
        btnNum[1] = (RelativeLayout) contentView.findViewById(R.id.finance_num1);
        btnNum[2] = (RelativeLayout) contentView.findViewById(R.id.finance_num2);
        btnNum[3] = (RelativeLayout) contentView.findViewById(R.id.finance_num3);
        btnNum[4] = (RelativeLayout) contentView.findViewById(R.id.finance_num4);
        btnNum[5] = (RelativeLayout) contentView.findViewById(R.id.finance_num5);
        btnNum[6] = (RelativeLayout) contentView.findViewById(R.id.finance_num6);
        btnNum[7] = (RelativeLayout) contentView.findViewById(R.id.finance_num7);
        btnNum[8] = (RelativeLayout) contentView.findViewById(R.id.finance_num8);
        btnNum[9] = (RelativeLayout) contentView.findViewById(R.id.finance_num9);
        btnNum[10] = (RelativeLayout) contentView.findViewById(R.id.finance_point);
        NumberAction na = new NumberAction();
        for (RelativeLayout bc : btnNum) {
            bc.setOnClickListener(na);
        }
        moneyPop.setOnDismissListener(mOnDissmissListener);
        closepop=(ImageView) contentView.findViewById(R.id.finance_closepop);
        closepop.setOnClickListener(this);
        budget_result=(TextView)contentView.findViewById(R.id.budget_caculator_result);
    }


    public PopupWindow.OnDismissListener mOnDissmissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {

        }
    };

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finance_closepop:
                moneyPop.dismiss();
                break;
            case R.id.finance_equal://确定
                mOnCaculatorSetListener.OnCaculatorSet(1,result);
                moneyPop.dismiss();
                break;

            case R.id.finance_backspace://删除
                if(result.length()!=0){
                    result=result.substring(0,result.length()-1);
                    budget_result.setText(result);
                }
                break;
            default:
                break;
        }
    }

    // 数字按钮监听器
    private class NumberAction implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.d("liangjialing","点击");
            RelativeLayout btn = (RelativeLayout) view;
            String input = btn.getContentDescription().toString();
            //判断result是否为空，如果为空输入又是"."，就什么也不做
            if (result.equals("") && input.equals(".")) {
                return;
            }
            // 判断result是否有"."，如果有，输入又是"."，就什么也不做
            if (result.indexOf(".") != -1 && input.equals(".")) {
                return;
            }
            //判断result是否有"."，如果有则判断小数点后最多有两位
            if (result.indexOf(".") != -1) {
                Log.d("liangjialing", "result.indexOf=  " + result.indexOf("."));
                if ((result.indexOf(".") + 3) <= result.length()) {
                    return ;
                }
            }
            result += input;
            budget_result.setText(result);

        }
    }


    public interface OnCaculatorSetListener {
        void OnCaculatorSet(int Sort,String date);
    }

    public void setOnCaculatorSetListener(OnCaculatorSetListener callback) {
        mOnCaculatorSetListener = callback;
    }

}
