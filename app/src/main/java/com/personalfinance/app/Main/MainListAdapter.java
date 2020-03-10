package com.personalfinance.app.Main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.personalfinance.app.R;

import java.util.List;


public class MainListAdapter extends ArrayAdapter<MainListClass> {
    private int resourceId;

    public MainListAdapter(Context context, int textViewRexourceId,
                         List<MainListClass> objects) {
        super(context,textViewRexourceId,objects);
        resourceId = textViewRexourceId;
    }

    @Override
    public View getView(int position, View converView, ViewGroup parent) {
        MainListClass listClass = getItem(position);//确定某一项
        View view;
        ViewHolder viewHolder;
        if (converView == null) {
            view = LayoutInflater.from(getContext()).
                    inflate(resourceId, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = converView;
            viewHolder = (ViewHolder) view.getTag();//重新获取ViewHolder
        }
        viewHolder.name.setText(listClass.getName());
        viewHolder.time.setText(listClass.getTime());
        viewHolder.expend.setText(listClass.getExpend());
        viewHolder.income.setText(listClass.getIncome());
        return view;
    }
    class ViewHolder {
        TextView name,time,expend,income;
        public ViewHolder(View view){
            name=(TextView)view.findViewById(R.id.main_center_name);
            time=(TextView)view.findViewById(R.id.main_center_time);
            expend=(TextView)view.findViewById(R.id.main_center_expendmoney);
            income=(TextView)view.findViewById(R.id.main_center_incomemoney);
        }
    }
}
