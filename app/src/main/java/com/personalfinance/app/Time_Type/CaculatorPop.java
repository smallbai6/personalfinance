package com.personalfinance.app.Time_Type;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

public class CaculatorPop extends FrameLayout implements View.OnClickListener {
    private OnCaculatorSetListener mOnCaculatorSetListener;//回调
    private PopupWindow moneyPop;
    //private Button btncancel;
    private ImageView btncancel;
    private Activity context;
    private View contentView;
    private Drawable drawable;


    private RelativeLayout[] btnNum = new RelativeLayout[11];// 数值按钮
    private RelativeLayout[] btnCommand = new RelativeLayout[3];// 符号按钮
    private ImageView backspace;
    private double result; // 计算结果
    private String process;//计算过程
    private String lastCommand; // 用于保存运算符当前运算符
    private boolean commonClick;//用于判断是否上一次点击了符号按钮
    private boolean firstFlag; // 用于判断是否是首次输入,true首次,false不是首次
    private String lastresult;//用于最后记录输入的数值
    private ArrayList<String> lastvalues = new ArrayList<>();//用于记录输入数值的数组
    private ArrayList<String> lastfuhao = new ArrayList<>();//用于记录输入运算符的数组
    private ArrayList<String> lastpoint = new ArrayList<>();//用于记录每组数值中小数位数
    private String inputCommand;//用于获得运算符号按键上的内容
    private int pointdigit; //用于限制输入的数字只能有两位数计数
    private boolean pointjudge;//用于判断是否输入了小数点


    public CaculatorPop(final Activity context, View parent) {
        super(context);
        this.context = context;
        contentView = View.inflate(context, R.layout.tally_detail_caculator_pop, null);
        //弹窗设置
        moneyPop = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        moneyPop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        moneyPop.setOutsideTouchable(true);

        moneyPop.showAtLocation(parent, Gravity.BOTTOM, 0, 0);



        // 获取运算符+-=
        btnCommand[0] = (RelativeLayout) contentView.findViewById(R.id.add);
        btnCommand[1] = (RelativeLayout) contentView.findViewById(R.id.subtract);
        btnCommand[2] = (RelativeLayout) contentView.findViewById(R.id.equal);
        //btnCommand[3] = (RelativeLayout) contentView.findViewById(R.id.backspace);
        backspace = (ImageView) contentView.findViewById(R.id.backspace);
        backspace.setOnClickListener(this);
        // 获取数字
        btnNum[0] = (RelativeLayout) contentView.findViewById(R.id.num0);
        btnNum[1] = (RelativeLayout) contentView.findViewById(R.id.num1);
        btnNum[2] = (RelativeLayout) contentView.findViewById(R.id.num2);
        btnNum[3] = (RelativeLayout) contentView.findViewById(R.id.num3);
        btnNum[4] = (RelativeLayout) contentView.findViewById(R.id.num4);
        btnNum[5] = (RelativeLayout) contentView.findViewById(R.id.num5);
        btnNum[6] = (RelativeLayout) contentView.findViewById(R.id.num6);
        btnNum[7] = (RelativeLayout) contentView.findViewById(R.id.num7);
        btnNum[8] = (RelativeLayout) contentView.findViewById(R.id.num8);
        btnNum[9] = (RelativeLayout) contentView.findViewById(R.id.num9);
        btnNum[10] = (RelativeLayout) contentView.findViewById(R.id.point);
        NumberAction na = new NumberAction();
        CommandAction ca = new CommandAction();
        for (RelativeLayout bc : btnNum) {
            bc.setOnClickListener(na);
        }
        for (RelativeLayout bc : btnCommand) {
            bc.setOnClickListener(ca);
        }
        moneyPop.setOnDismissListener(mOnDissmissListener);
        btncancel = (ImageView) contentView.findViewById(R.id.money_cancel);
        btncancel.setOnClickListener(this);
        result = 0;
        process = "";
        lastCommand = "确\n定";
        commonClick = false;
        firstFlag = true;
        lastresult = "";
        lastvalues.clear();
        lastfuhao.clear();
        lastfuhao.add("确\n定");
        lastpoint.clear();
        pointjudge = false;
        pointdigit = 0;
    }

    public void calculator() {
        // 初始化各项值
        result = 0; // 结果值
        process = "";//过程为空
        lastCommand = "确\n定"; // 运算符
        commonClick = false;//每点击符号按钮
        firstFlag = true; // 是首次运算
        mOnCaculatorSetListener.OnCaculatorSet(2, process);
        lastresult = "";
        lastvalues.clear();
        lastfuhao.clear();
        lastfuhao.add("确\n定");
        pointjudge = false;
        pointdigit = 0;
    }

    //lastcommand为"="时候过程区域显示内容
    private void process_equals() {
        if (!lastCommand.equals("确\n定")) {
            mOnCaculatorSetListener.OnCaculatorSet(2, process);
        } else {
            mOnCaculatorSetListener.OnCaculatorSet(2, "");
        }
    }

    public PopupWindow.OnDismissListener mOnDissmissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            calculator();
        }
    };

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.money_cancel:
                moneyPop.dismiss();
                calculator();
                break;
            /**/case R.id.backspace:
                if (firstFlag) {// 首次输入"-"的情况
                    return;
                } else {
                    deletebtn();
                }
                break;
            default:
                break;
        }
    }
    // 数字按钮监听器
    private class NumberAction implements OnClickListener {
        @Override
        public void onClick(View view) {
            RelativeLayout btn = (RelativeLayout) view;
            String input = btn.getContentDescription().toString();
            if (firstFlag) { // 首次输入
                // 一上就".",就什么也不做
                if (input.equals(".")) {
                    return;
                }
                mOnCaculatorSetListener.OnCaculatorSet(1, "");
                firstFlag = false;// 改变是否首次输入的标记值
            } else {
                //判断lastresult是否为空，如果为空输入又是"."，就什么也不做
                if (lastresult.equals("") && input.equals(".")) {
                    return;
                }
                // 判断lastresult是否有"."，如果有，输入又是"."，就什么也不做
                if (lastresult.indexOf(".") != -1 && input.equals(".")) {
                    return;
                }
                if (input.equals(".")) {
                    pointjudge = true;
                    pointdigit = 0;
                }
                if (pointdigit == 3) {
                    return;
                }
                if (pointjudge) {
                    pointdigit += 1;
                }
                if (!commonClick && !input.equals(".")) {
                    switch (lastCommand) {
                        case "+":
                            result = result - Double.parseDouble(lastresult);
                            break;
                        case "-":
                            result = result + Double.parseDouble(lastresult);
                            break;
                        default:
                            break;
                    }
                }
                commonClick = false;
                if (lastresult.equals("0") && !input.equals(".")) {
                    lastresult = "";
                    process = process.substring(0, process.length() - 1);
                }
            }
            //共有的
            process = process + input;

            process_equals();
            lastresult = lastresult + input;
            if (!input.equals(".")) {
                calculate(Double.parseDouble(lastresult));// 保存显示区域的值,并计算
            }
        }
    }


    // 符号按钮监听器
    private class CommandAction implements OnClickListener {
        @Override
        public void onClick(View view) {
            RelativeLayout btn = (RelativeLayout) view;
            inputCommand = btn.getContentDescription().toString();
            if (firstFlag) {// 首次输入"-"的情况
                if (inputCommand.equals("-")) {
                    mOnCaculatorSetListener.OnCaculatorSet(1, "-");
                    process = process + inputCommand;
                    lastresult = lastresult + inputCommand + "0";
                    calculate(Double.parseDouble(lastresult));
                    firstFlag = false;
                } else {//首次输入其他运算符
                    return;
                }
            }

            else if (inputCommand.equals("确\n定")) {
                calculator();
                moneyPop.dismiss();
            } else {//点击"+"or"-"
                //  Log.d("lianga", "点击+/-");
                addorsub();
                //  Log.d("lianga", "点击+/-完成");
            }

        }
    }

    /*
     *点击"+"or"-"
     */
    private void addorsub() {
        // Log.d("liang", "进入addorsub");
        if (lastresult != "") {
            if (lastresult.substring(lastresult.length() - 1).equals(".")) {
                return;
            }
        }
        if (process.substring(process.length() - 1).equals("+") || process.substring(process.length() - 1).equals("-")) {
            process = process.substring(0, process.length() - 1) + inputCommand;
            mOnCaculatorSetListener.OnCaculatorSet(2, process);
            lastCommand = inputCommand; // 保存你点击的运算符
            lastfuhao.remove(lastfuhao.size() - 1);
            lastfuhao.add(inputCommand);
        } else {
            process = process + inputCommand;
            mOnCaculatorSetListener.OnCaculatorSet(2, process);
            lastCommand = inputCommand; // 保存你点击的运算符
            lastfuhao.add(inputCommand);
            lastvalues.add(lastresult);
            lastresult = "";
            commonClick = true;
            lastpoint.add(String.valueOf(pointdigit));
            pointjudge = false;
            pointdigit = 0;
        }

    }

    /*
     *点击删除键
     */
    private void deletebtn() {
        // Log.d("liang", "初始  result:" + result + "   process:" + process + "   lastresult:" + lastresult);
        if (lastresult.equals("")) {
            //删除符号并获取上一个lastresult
            if (process.equals("")) {
                return;
            }
            process = process.substring(0, process.length() - 1);
            lastresult = lastvalues.get(lastvalues.size() - 1);
            lastvalues.remove(lastvalues.size() - 1);
            lastfuhao.remove(lastfuhao.size() - 1);
            lastCommand = lastfuhao.get(lastfuhao.size() - 1);
            //取出小数点位数
            pointdigit = Integer.valueOf(lastpoint.get(lastpoint.size() - 1));
            lastpoint.remove(lastpoint.size() - 1);
            if (pointdigit != 0) {
                pointjudge = true;
            }


            process_equals();
        } else {//考虑一开始输入的是"-"
            if (lastresult.equals("-0")) {
                calculator();
                mOnCaculatorSetListener.OnCaculatorSet(1, "0.00");
                return;
            }
            //正常删除
            process = process.substring(0, process.length() - 1);
            if (process.equals("")) {
                calculator();
                mOnCaculatorSetListener.OnCaculatorSet(1, "0.00");
                return;
            }
            String lastposition = lastresult.substring(lastresult.length() - 1);
            if (!lastposition.equals(".")) {
                switch (lastCommand) {
                    case "+":
                        result = result - Double.parseDouble(lastresult);
                        break;
                    case "-":
                        result = result + Double.parseDouble(lastresult);
                        break;
                    default:
                        break;
                }
                mOnCaculatorSetListener.OnCaculatorSet(1, formatPrice(result));
            }
            //   Log.d("liang", "b:  result:" + result + "   process:" + process + "   lastresult:" + lastresult);

            if (pointjudge) {
                pointdigit -= 1;
            }
            if (lastposition.equals(".")) {
                pointdigit = 0;
                pointjudge = false;
            }
            lastresult = lastresult.substring(0, lastresult.length() - 1);
            if (!lastposition.equals(".") && lastresult.length() != 0) {
                calculate(Double.parseDouble(lastresult));
            }
            //  Log.d("liang", "c:  result:" + result + "   process:" + process + "   lastresult:" + lastresult);

            if (lastCommand.equals("确\n定") && process.substring(process.length() - 1).equals(".")) {
                process = process.substring(0, process.length() - 1);
                lastresult = lastresult.substring(0, lastresult.length() - 1);
                calculate(Double.parseDouble(lastresult));
            }
            process_equals();
            if (process.substring(process.length() - 1).equals("+") || process.substring(process.length() - 1).equals("-")) {
                commonClick = true;
            }
        }
    }

    // 计算用的方法
    private void calculate(double x) {

        if (lastCommand.equals("+")) {
            result += x;
        } else if (lastCommand.equals("-")) {
            result -= x;
        } else if (lastCommand.equals("确\n定")) {
            result = x;
        }
        mOnCaculatorSetListener.OnCaculatorSet(1, formatPrice(result));
    }

    public static String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(price);
        return format;
    }


    public interface OnCaculatorSetListener {
        void OnCaculatorSet(int sort, String date);
    }

    public void setOnCaculatorSetListener(OnCaculatorSetListener callback) {
        mOnCaculatorSetListener = callback;
    }

}
