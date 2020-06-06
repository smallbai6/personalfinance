package com.personalfinance.app.Finance.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.personalfinance.app.Finance.Class.Product;
import com.personalfinance.app.Finance.Product_DetailActivity;
import com.personalfinance.app.R;

import java.util.List;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView Picture;
        private TextView Product_Name;
        private TextView Product_Yield;
        private TextView Purchase_Amount;
        private View productView;

        public ViewHolder(View view) {
            super(view);
            productView = view;
            Picture = (ImageView) view.findViewById(R.id.productitem_picture);
            Product_Name = (TextView) view.findViewById(R.id.productitem_name);
            Product_Yield = (TextView) view.findViewById(R.id.productitem_yield);
            Purchase_Amount = (TextView) view.findViewById(R.id.productitem_purchaseamount);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//创建ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.listitem_product, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.productView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//进入该产品的位置
                int position = holder.getAdapterPosition();
                Product product = productList.get(position);
                //跳转进入该产品的详情内
                Intent intent = new Intent(v.getContext(), Product_DetailActivity.class);//进入产品详情表
                intent.putExtra("User_Number",product.getUser_Number());//用户编号传递
                intent.putExtra("Product_Number", product.getProduct_Number());//产品编号传递
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {//绑定ViewHolder
        Product product = productList.get(position);
        holder.Picture.setImageDrawable(product.getPicture());
        holder.Product_Name.setText(product.getProduct_Name());
        holder.Product_Yield.setText(product.getYield()+"%");
        if(product.getPurchase_Amount().equals("")){
            holder.Purchase_Amount.setText("不限起购金额");
        }else{
            holder.Purchase_Amount.setText(product.getPurchase_Amount()+"元起购");
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }


}
