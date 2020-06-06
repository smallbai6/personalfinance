package com.personalfinance.app.Finance;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.Class.Product;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.PictureFormatUtil;

import org.w3c.dom.Text;

public class Product_DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;
    private SQLiteDatabase db;
    private Drawable drawable;
    private String User_Number, Product_Number;
    private TextView back;
    private ImageView backimage;
    private RelativeLayout buy;
    private ImageView Picture;
    private TextView Product_Name, Company, Yield, Purchase_Amount, Introduct;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.financeproductdetail);//详情界面
        intent = getIntent();
        User_Number = intent.getStringExtra("User_Number");
        Product_Number = intent.getStringExtra("Product_Number");
        Init();
        //查找数据库
        getProduct();

    }

    private void Init() {
        back = (TextView) findViewById(R.id.productdetail_back);
        backimage=(ImageView)findViewById(R.id.productdetail_backimageview);
        back.setOnClickListener(this);
        backimage.setOnClickListener(this);
        buy = (RelativeLayout) findViewById(R.id.productdetail_buy);
        buy.setOnClickListener(this);
        Picture = (ImageView) findViewById(R.id.productdetail_imageView);
        Product_Name = (TextView) findViewById(R.id.productdetail_productname);
        Company = (TextView) findViewById(R.id.productdetail_company);
        Yield = (TextView) findViewById(R.id.productdetail_yield);
        Purchase_Amount = (TextView) findViewById(R.id.productdetail_purchaseamount);
        Introduct = (TextView) findViewById(R.id.productdetail_introduct);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.productdetail_backimageview:
            case R.id.productdetail_back://退出返回上一个活动
                Log.d("liangjialing","返回");
                finish();
                break;
            case R.id.productdetail_buy://进入买入界面
                //进入买入卖出的界面
                Log.d("liangjialing","进入买入界面");
                intent=new Intent(Product_DetailActivity.this,BuyActivity.class);
                intent.putExtra("User_Number",User_Number) ;
                intent.putExtra("Product_Number",Product_Number);
                startActivity(intent);
                //finish();
                break;
        }
    }

    private void getProduct() {
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("financeproduct", null, "Product_Number=?",
                new String[]{Product_Number}, null, null, null);
        if (cursor.moveToFirst()) {
            //找到该数据
             drawable = PictureFormatUtil.Bytes2Drawable(getResources(),
                    cursor.getBlob(cursor.getColumnIndex("Picture")));
            Picture.setImageDrawable(drawable);
            Product_Name.setText(cursor.getString(cursor.getColumnIndex("Product_Name")));
            Company.setText(cursor.getString(cursor.getColumnIndex("Company")));
            Yield.setText(cursor.getString(cursor.getColumnIndex("Yield"))+"%");
            Log.d("liangjialing",cursor.getString(cursor.getColumnIndex("Purchase_Amount"))+"  kl");
            if(cursor.getString(cursor.getColumnIndex("Purchase_Amount")).equals("")){
                Purchase_Amount.setText("不限");

            }else{
                Purchase_Amount.setText(
                        cursor.getString(cursor.getColumnIndex("Purchase_Amount"))+"元");
            }
           // Purchase_Amount.setText(cursor.getString(cursor.getColumnIndex("Purchase_Amount")));
            Introduct.setText(cursor.getString(cursor.getColumnIndex("Introduct")));
        }
    }
}
