package com.personalfinance.app.Finance;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.PictureFormatUtil;
import com.personalfinance.app.Util.TimeFormatUtil;

import org.w3c.dom.Text;

public class Record_DetailActivity extends AppCompatActivity implements View.OnClickListener{
    private Intent intent;
    private SQLiteDatabase db;
    private String User_Number, Product_Number, Order_Number;
    private Drawable drawable;

    private TextView back;
    private ImageView backimage;
    private ImageView CompanyPicture;
    private TextView text1, text2, text3, text4, text5;
    private TextView a, b, c, d, e, f, g, h, i;
    private LinearLayout Sure_LinearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.financerecorddetail);//交易记录详情界面
        intent = getIntent();
        //User_Number = intent.getStringExtra("User_Number");
        //Product_Number = intent.getStringExtra("Product_Number");
        Order_Number = intent.getStringExtra("Order_Number");
        Log.d("liangjialing","进入Record_DetailActivity");
        Init();
        //根据进行数据查找判断买入或卖出 判断待确认和已确认
        //首先从数据库中进行查询
        QueryData();

    }

    private void Init() {
        back=(TextView)findViewById(R.id.financerecorddetail_back);
        backimage=(ImageView)findViewById(R.id.financerecorddetail_backimageview);
        back.setOnClickListener(this);
        backimage.setOnClickListener(this);
        CompanyPicture = (ImageView) findViewById(R.id.financerecorddetail_picture);
        text1 = (TextView) findViewById(R.id.financerecorddetail_text1);
        text2 = (TextView) findViewById(R.id.financerecorddetail_text2);
        text3 = (TextView) findViewById(R.id.financerecorddetail_text3);
        text4 = (TextView) findViewById(R.id.financerecorddetail_text4);
        text5 = (TextView) findViewById(R.id.financerecorddetail_text5);

        a = (TextView) findViewById(R.id.financerecorddetail_a);
        b = (TextView) findViewById(R.id.financerecorddetail_b);
        c = (TextView) findViewById(R.id.financerecorddetail_c);
        d = (TextView) findViewById(R.id.financerecorddetail_d);
        e = (TextView) findViewById(R.id.financerecorddetail_e);
        f = (TextView) findViewById(R.id.financerecorddetail_f);
        g = (TextView) findViewById(R.id.financerecorddetail_g);
        h = (TextView) findViewById(R.id.financerecorddetail_h);
        i = (TextView) findViewById(R.id.financerecorddetail_i);

        Sure_LinearLayout=(LinearLayout)findViewById(R.id.financerecorddetail_sureLinearlayout);
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.financerecorddetail_backimageview:
            case R.id.financerecorddetail_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void QueryData() {
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("recordproduct", null, "Order_Number=?",
                new String[]{Order_Number}, null, null, null);
        if (cursor.moveToFirst()) {
            //订单号为主键
            String Product_Number = cursor.getString(cursor.getColumnIndex("Product_Number"));
            //通过产品编号获得产品名字、公司、公司图片
            Drawable Picture = null;
            String Company = "";
            String Product_Name = "";
            Cursor cursor1 = db.query("financeproduct", null, "Product_Number=?",
                    new String[]{Product_Number}, null, null, null);
            if (cursor1.moveToFirst()) {
                Picture = PictureFormatUtil.Bytes2Drawable(getResources(),
                        cursor1.getBlob(cursor1.getColumnIndex("Picture")));
                Company = cursor1.getString(cursor1.getColumnIndex("Company"));
                Product_Name = cursor1.getString(cursor1.getColumnIndex("Product_Name"));
            }

            String Buy_Sale = cursor.getString(cursor.getColumnIndex("Buy_Sale"));
            String BMoney_SQuotient = cursor.getString(cursor.getColumnIndex("BMoney_SQuotient"));
            String Sure_Status = cursor.getString(cursor.getColumnIndex("Sure_Status"));
            String BQuotient_SMoney = cursor.getString(cursor.getColumnIndex("BQuotient_SMoney"));
            String Time = TimeFormatUtil.LongToStringA(cursor.getLong(cursor.getColumnIndex("Time")));
            String Sure_Time = TimeFormatUtil.LongToStringA(cursor.getLong(cursor.getColumnIndex("Sure_Time")));

            if (Sure_Status.equals("0")) {//待确认中
                if (Buy_Sale.equals("0")) {//买入
                    textbuy();
                    data_buy(Picture, Company, BMoney_SQuotient, Product_Name, BQuotient_SMoney, Time, Sure_Time);

                } else if (Buy_Sale.equals("1")) {//卖出
                    textsale();
                    data_sale(Picture, Company, BMoney_SQuotient, Product_Name, BQuotient_SMoney, Time, Sure_Time);
                }
                Sure_LinearLayout.setVisibility(View.GONE);
            } else if (Sure_Status.equals("1")) {//已确认
                if (Buy_Sale.equals("0")) {//买入
                    textbuy();
                    data_buy(Picture, Company, BMoney_SQuotient, Product_Name, BQuotient_SMoney, Time, Sure_Time);
                    CompanyPicture.setImageDrawable(Picture);
                } else if (Buy_Sale.equals("1")) {//卖出
                    textsale();
                    data_sale(Picture, Company, BMoney_SQuotient, Product_Name, BQuotient_SMoney, Time, Sure_Time);
                }
            }


        }
    }

    private void textbuy() {
        text1.setText("买入成功");
        text2.setText("买入信息");
        text3.setText("买入产品");
        text4.setText("买入金额");
        text5.setText("买入时间");
    }

    private void data_buy(Drawable Picture, String Company, String BMoney_SQuotient, String Product_Name, String BQuotient_SMoney
            , String Time, String Sure_Time) {
        CompanyPicture.setImageDrawable(Picture);
        a.setText(Company);
        b.setText(BMoney_SQuotient+"元");
        c.setText(Product_Name);
        d.setText(BMoney_SQuotient+"元");
        e.setText(Time);
        f.setText(BMoney_SQuotient+"元");
        g.setText(BQuotient_SMoney+"份");
        h.setText(Sure_Time);
        i.setText(Order_Number);

    }
    private void data_sale(Drawable Picture, String Company, String BMoney_SQuotient, String Product_Name, String BQuotient_SMoney
            , String Time, String Sure_Time) {
        CompanyPicture.setImageDrawable(Picture);
        a.setText(Company);
        b.setText(BMoney_SQuotient+"份");
        c.setText(Product_Name);
        d.setText(BMoney_SQuotient+"份");
        e.setText(Time);
        f.setText(BQuotient_SMoney+"元");
        g.setText(BMoney_SQuotient+"份");
        h.setText(Sure_Time);
        i.setText(Order_Number);
    }

    private void textsale() {
        text1.setText("卖出成功");
        text2.setText("卖出信息");
        text3.setText("卖出产品");
        text4.setText("卖出份额");
        text5.setText("卖出时间");
    }

}
