package com.personalfinance.app.Finance;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.Class.HoldProduct;
import com.personalfinance.app.R;

public class HoldProduct_DetailActivity extends AppCompatActivity implements View.OnClickListener{
    private Intent intent;
    private SQLiteDatabase db;
    private Drawable drawable;
    private String User_Number,Product_Number;
    private TextView Money,Yesterday_income,Sum_income,Hold_Quotient,Each_Amount,Detail;
    private ImageView Back;
    private RelativeLayout Record,Buy,Sale;
    @Override
    protected void onCreate(Bundle savedInstanceState) {//初始化
        super.onCreate(savedInstanceState);
        setContentView(R.layout.financeholddetail);//持有界面布局
        intent=getIntent();
        //用户编号和产品编号
        User_Number=intent.getStringExtra("User_Number");
        Product_Number=intent.getStringExtra("Product_Number");
        Init();
        setData();
    }
    private void Init(){
        Back=(ImageView)findViewById(R.id.financeholddetail_back);
        //drawable = getResources().getDrawable(R.mipmap.zuojiantou);
       // drawable.setBounds(0, 0, 40, 40);
       // Back.setCompoundDrawables(drawable, null, null, null);
        Back.setOnClickListener(this);
        Detail=(TextView)findViewById(R.id.financeholddetail_detail);
        Detail.setOnClickListener(this);

        //显示信息
        Money=(TextView)findViewById(R.id.financeholddetail_money);
        Yesterday_income=(TextView)findViewById(R.id.financeholddetail_yesterday);
        Sum_income=(TextView)findViewById(R.id.financeholddetail_sum);
        Hold_Quotient=(TextView)findViewById(R.id.financeholddetail_holdquotient);
        Each_Amount=(TextView)findViewById(R.id.financeholddetail_eachamount);

        //交易记录、买入、卖出
        Record=(RelativeLayout)findViewById(R.id.financeholddetail_record);
        Record.setOnClickListener(this);
        Buy=(RelativeLayout)findViewById(R.id.financeholddetail_buy);
        Buy.setOnClickListener(this);
        Sale=(RelativeLayout)findViewById(R.id.financeholddetail_sale);
        Sale.setOnClickListener(this);
    }
    private void setData(){
        //根据用户编号和产品编号进行搜索
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("holdproduct", null, "Product_Number=?",
                new String[]{Product_Number}, null, null, null);
        if(cursor.moveToFirst()){
            do{
                //判断用户编号
                if(User_Number.equals( cursor.getString(cursor.getColumnIndex("User_Number")))){
                    Money.setText(cursor.getString(cursor.getColumnIndex("Money")));
                    Yesterday_income.setText(cursor.getString(cursor.getColumnIndex("Yesterday_income")));
                    Sum_income.setText(cursor.getString(cursor.getColumnIndex("Sum_income")));
                    Hold_Quotient.setText(cursor.getString(cursor.getColumnIndex("Hold_Quotient")));
                    Each_Amount.setText(cursor.getString(cursor.getColumnIndex("Each_Amount")));
                }
            }while(cursor.moveToNext());
        }
        db.close();
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.financeholddetail_back:
                Log.d("liangjialing","返回");
                finish();
                break;
            case R.id.financeholddetail_detail:
                Log.d("liangjialing","进入详情");
                intent=new Intent(HoldProduct_DetailActivity.this,Product_DetailActivity.class);
                intent.putExtra("User_Number",User_Number);
                intent.putExtra("Product_Number",Product_Number);
                startActivity(intent);
                break;
            case R.id.financeholddetail_record:
                Log.d("liangjialing","点击交易记录");
                intent=new Intent(HoldProduct_DetailActivity.this,FinanceRecordActivity.class);
                //传递点信息
                intent.putExtra("User_Number",User_Number);//用户编号
                intent.putExtra("Product_Number",Product_Number);//产品编号
                startActivity(intent);
                break;
            case R.id.financeholddetail_buy:
                Log.d("liangjialing","进入买入界面");
                intent=new Intent(HoldProduct_DetailActivity.this,BuyActivity.class);
                intent.putExtra("User_Number",User_Number) ;
                intent.putExtra("Product_Number",Product_Number);
                startActivity(intent);
                break;
            case R.id.financeholddetail_sale:

                Log.d("liangjialing","进出卖出界面");
                intent=new Intent(HoldProduct_DetailActivity.this,SaleActivity.class);
                intent.putExtra("User_Number",User_Number) ;
                intent.putExtra("Product_Number",Product_Number);
                startActivity(intent);
                break;


        }
    }
}
