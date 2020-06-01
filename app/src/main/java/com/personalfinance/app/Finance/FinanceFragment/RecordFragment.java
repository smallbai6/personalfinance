package com.personalfinance.app.Finance.FinanceFragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.Adapter.RecordAdapter;
import com.personalfinance.app.Finance.Class.Record;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.PictureFormatUtil;
import com.personalfinance.app.Util.TimeFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends Fragment {
    View view;
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        view = View.inflate(container.getContext(), R.layout.recyclerlist, null);
        Bundle bundle = this.getArguments();//得到从Activity传来的数据
        String User_Number = bundle.getString("User_Number");
        String Product_Number=bundle.getString("Product_Number");
        String Buy_Sale=bundle.getString("Buy_Sale");
        String Sure_Status=bundle.getString("Sure_Status");
        Log.d("liangjialing",User_Number+"  "+Product_Number+"  "+Buy_Sale+"  "+Sure_Status);
        InitHoldProduct(User_Number,Product_Number,Buy_Sale,Sure_Status);
        return view;
    }

    private List<Record> recordList = new ArrayList<>();

    private void InitHoldProduct(String usernumber,String productnumber,String buysale,String surestatus) {//显示列表适配
        InitList(usernumber,productnumber,buysale,surestatus);//初始化列表
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.Recyclerlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        RecordAdapter adapter=new RecordAdapter(recordList);
        recyclerView.setAdapter(adapter);
    }


    private void InitList(String usernumber,String productnumber,String buysale,String surestatus){
    //根据用户编号在holdproduct数据库中进行查找
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor=null;
        if (productnumber.equals("")) {
            if (buysale.equals("") && surestatus.equals("")) {//全部
                cursor = db.query("recordproduct", null, "User_Number=?",
                        new String[]{usernumber}, null, null, null);
            } else if (surestatus.equals("1")) {//买入/卖出
                cursor = db.query("recordproduct", null, "User_Number=? AND Buy_Sale=? AND Sure_Status=?",
                        new String[]{usernumber, buysale, surestatus}, null, null, null);
            } else if (surestatus.equals("0")) {//进行中
                cursor = db.query("recordproduct", null, "User_Number=? AND Sure_Status=?",
                        new String[]{usernumber, surestatus}, null, null, null);
            }

        }
        else {//某产品所有的交易记录
            if (buysale.equals("") && surestatus.equals("")) {//全部
                cursor = db.query("recordproduct", null, "User_Number=? AND Product_Number=?",
                        new String[]{usernumber,productnumber}, null, null, null);
            } else if ( surestatus.equals("1")) {//买入/卖出
                cursor = db.query("recordproduct", null, "User_Number=? AND Product_Number=? AND Buy_Sale=? AND Sure_Status=?",
                        new String[]{usernumber,productnumber,buysale,surestatus}, null, null, null);
            } else if (surestatus.equals("0")) {//进行中
                cursor = db.query("recordproduct", null, "User_Number=? AND Product_Number=? AND Sure_Status=?",
                        new String[]{usernumber,productnumber,surestatus}, null, null, null);
            }
        }
        if (cursor.moveToFirst()) {
            do {Log.d("liangjialing","cursor.moveToFirst()");
                //公司图标根据产品编号进行查找
                //首先的得到产品编号，根据产品编号在financeproduct数据表中查找公司图标和公司名
                String Product_Number=cursor.getString(cursor.getColumnIndex("Product_Number"));
                Log.d("liangjialing",Product_Number);
                Cursor cursor1 = db.query("financeproduct", null, "Product_Number=?",
                        new String[]{Product_Number}, null, null, null);
                String Product_Name="";
                if(cursor1.moveToFirst()){
                    Product_Name = cursor1.getString(cursor1.getColumnIndex("Product_Name"));
                }

                String Buy_Sale=cursor.getString(cursor.getColumnIndex("Buy_Sale"));
                String Time= TimeFormatUtil.LongToStringA(cursor.getLong(cursor.getColumnIndex("Time")));
                String Order_Number=cursor.getString(cursor.getColumnIndex("Order_Number"));
                String M_Q_S="";
                String Sure_Status=cursor.getString(cursor.getColumnIndex("Sure_Status"));
                if(Sure_Status.equals("0")){//待确认
                     M_Q_S="进行中";
                }else if(Sure_Status.equals("1")){//已确认
                    M_Q_S=cursor.getString(cursor.getColumnIndex("BMoney_SQuotient"));
                }
                Record record=new Record(Buy_Sale,Product_Name,Time,M_Q_S,Order_Number,Sure_Status);
                recordList.add(record);
            } while (cursor.moveToNext());
        }
        db.close();

    }

}