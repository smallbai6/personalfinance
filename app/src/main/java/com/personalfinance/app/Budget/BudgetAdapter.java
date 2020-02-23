package com.personalfinance.app.Budget;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.personalfinance.app.R;

import java.util.List;

public class BudgetAdapter extends ArrayAdapter<BudgetClass> {
    private int resourceId;

    public BudgetAdapter(Context context, int textViewRexourceId,
                         List<BudgetClass> objects) {
        super(context, textViewRexourceId, objects);
        resourceId = textViewRexourceId;
    }

    @Override
    public View getView(int position, View converView, ViewGroup parent) {
        BudgetClass budgetClass = getItem(position);//确定某一项
        View view;
        ViewHolder viewHolder;
        if (converView == null) {
            view = LayoutInflater.from(getContext()).
                    inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.type = (TextView) view.findViewById(R.id.budget_type_type);
            viewHolder.budgetmoney = (TextView) view.findViewById(R.id.budget_type_yusuanmoney);
            viewHolder.zyc = (TextView) view.findViewById(R.id.budget_type_zyc);
            viewHolder.resultmoney = (TextView) view.findViewById(R.id.budget_type_zycmoney);
            viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.budget_type_progressBar);
            view.setTag(viewHolder);
        } else {
            view = converView;
            viewHolder = (ViewHolder) view.getTag();//重新获取ViewHolder
        }
        viewHolder.type.setText(budgetClass.getType());
        viewHolder.budgetmoney.setText(budgetClass.getBudgetmoney());
        viewHolder.zyc.setText(budgetClass.getZyc());
        viewHolder.resultmoney.setText(budgetClass.getResultmoney());
        if (budgetClass.getZyc().equals("余额") || budgetClass.getZyc().equals("待收")) {
            int process;
            if (budgetClass.getBudgetmoney().equals("0.00")) {
                process = 0;
            } else {
                process = (int) ((1-Double.valueOf(budgetClass.getResultmoney()) / Double.valueOf(budgetClass.getBudgetmoney())) * 100);
            }
            viewHolder.resultmoney.setTextColor(Color.BLACK);
            viewHolder.progressBar.setProgress(process);
        } else if (budgetClass.getZyc().equals("超支") || budgetClass.getZyc().equals("超收")) {
            viewHolder.progressBar.setProgress(100);
            viewHolder.resultmoney.setTextColor(Color.RED);
        } else {
            viewHolder.progressBar.setProgress(0);
            viewHolder.resultmoney.setTextColor(Color.BLACK);
        }
        return view;
    }
    class ViewHolder {
        TextView type;
        TextView budgetmoney;
        TextView zyc;
        TextView resultmoney;
        ProgressBar progressBar;
    }
}
