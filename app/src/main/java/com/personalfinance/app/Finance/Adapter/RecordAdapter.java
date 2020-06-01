package com.personalfinance.app.Finance.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personalfinance.app.Finance.Class.Record;
import com.personalfinance.app.Finance.Record_DetailActivity;
import com.personalfinance.app.R;

import org.w3c.dom.Text;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> recordList;

    public RecordAdapter(List<Record> recordList) {
        this.recordList =recordList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
       private TextView  Buy_Sale,Product_Name,Time,M_Q_S;
       private TextView Yuan_Fen;
        private View recordView;

        public ViewHolder(View view) {
            super(view);
            recordView = view;
            Buy_Sale=(TextView)view.findViewById(R.id.recorditem_buysale);
            Product_Name=(TextView)view.findViewById(R.id.recorditem_productname);
            Time=(TextView)view.findViewById(R.id.recorditem_time);
            M_Q_S=(TextView)view.findViewById(R.id.recorditem_MQS);
            Yuan_Fen=(TextView)view.findViewById(R.id.recorditem_yuanfen);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//创建ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.listitem_record, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.recordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//进入该产品持有详情
                int position = holder.getAdapterPosition();
                Record record = recordList.get(position);
                //跳转进入该产品的详情内
                Intent intent = new Intent(v.getContext(), Record_DetailActivity.class);//进入交易记录详情
                intent.putExtra("Order_Number",record.getOrder_Number());//传递订单号，准确锁定交易信息
                //intent.putExtra("User_Number",record.getUser_Number());//用户编号传递
               // intent.putExtra("Product_Number", record.getProduct_Number());//产品编号传递
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {//绑定ViewHolder
        Record record = recordList.get(position);
       // holder.Buy_Sale.setText(record.getBuy_Sale());
        holder.Product_Name.setText(record.getProduct_Name());
        holder.Time.setText(record.getTime());
        holder.M_Q_S.setText(record.getM_Q_S());
        if(record.getBuy_Sale().equals("0")){
            holder.Buy_Sale.setText("买入");
            holder.Buy_Sale.setTextColor(Color.RED);
            if(record.getSure_Status().equals("1")){
                holder.Yuan_Fen.setText("元");
            }

        }else if(record.getBuy_Sale().equals("1")){
            holder.Buy_Sale.setText("卖出");

            holder.Buy_Sale.setTextColor(Color.GREEN);
            if(record.getSure_Status().equals("1")){
               holder.Yuan_Fen.setText("份");
            }

        }
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }


}

