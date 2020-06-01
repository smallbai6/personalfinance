package com.personalfinance.app.Finance.Adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personalfinance.app.Finance.Class.HoldProduct;
import com.personalfinance.app.Finance.HoldProduct_DetailActivity;
import com.personalfinance.app.R;

import java.util.List;

public class HoldProductAdapter extends RecyclerView.Adapter<HoldProductAdapter.ViewHolder> {
    private List<HoldProduct> holdproductList;

    public HoldProductAdapter(List<HoldProduct> holdproductList) {
        this.holdproductList = holdproductList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView Picture;
        private TextView Company;
        private TextView Product_Name;
        private TextView Money,Yesterday_income,Sum_income;
        private View holdproductView;

        public ViewHolder(View view) {
            super(view);
            holdproductView = view;
            Picture = (ImageView) view.findViewById(R.id.holditem_picture);
            Company=(TextView)view.findViewById(R.id.holditem_company);
            Product_Name = (TextView) view.findViewById(R.id.holditem_productname);
            Money=(TextView)view.findViewById(R.id.holditem_money);
            Yesterday_income=(TextView)view.findViewById(R.id.holditem_yesterterday);
            Sum_income=(TextView)view.findViewById(R.id.holditem_hold);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//创建ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.listitem_hold, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.holdproductView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//进入该产品持有详情
                int position = holder.getAdapterPosition();
                HoldProduct holdproduct = holdproductList.get(position);
                //跳转进入该产品的详情内
                Intent intent = new Intent(v.getContext(), HoldProduct_DetailActivity.class);//进入产品详情表
                intent.putExtra("User_Number",holdproduct.getUser_Number());//用户编号传递
                intent.putExtra("Product_Number", holdproduct.getProduct_Number());//产品编号传递
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {//绑定ViewHolder
        HoldProduct holdproduct = holdproductList.get(position);
        Log.d("liangjialing","holdproduct.getCompany");
        holder.Picture.setImageDrawable(holdproduct.getPicture());
        holder.Company.setText(holdproduct.getCompany());
        holder.Product_Name.setText(holdproduct.getProduct_Name());
        holder.Money.setText(holdproduct.getMoney());
        holder.Yesterday_income.setText(holdproduct.getYesterday_income());
        holder.Sum_income.setText(holdproduct.getSum_income());
    }

    @Override
    public int getItemCount() {
        return holdproductList.size();
    }


}
