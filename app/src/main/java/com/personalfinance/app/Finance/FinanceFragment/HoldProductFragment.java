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

import com.personalfinance.app.Finance.Adapter.HoldProductAdapter;
import com.personalfinance.app.Finance.Class.HoldProduct;
import com.personalfinance.app.Finance.Class.Product;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.PictureFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class HoldProductFragment extends Fragment {
    View view;
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        view = View.inflate(container.getContext(), R.layout.recyclerlist, null);
        Bundle bundle = this.getArguments();//得到从Activity传来的数据
        Log.d("liangjialing","holdproductFragment");
        String UserNumber = bundle.getString("User_Number");
        Log.d("liangjialing",UserNumber);
        InitHoldProduct(UserNumber);
        return view;
    }

    private List<HoldProduct> holdproductList = new ArrayList<>();

    private void InitHoldProduct(String usernumber) {//显示列表适配
        InitList(usernumber);//初始化列表
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.Recyclerlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        HoldProductAdapter adapter=new HoldProductAdapter(holdproductList);
        recyclerView.setAdapter(adapter);
    }


    private void InitList(String usernumber) {//列表的建立,从SQLite数据库中查找
        //根据用户编号在holdproduct数据库中进行查找
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("holdproduct", null, "User_Number=?",
                new String[]{usernumber}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Log.d("liangjialing","有数据");
                //公司图标根据产品编号进行查找
                //首先的得到产品编号，根据产品编号在financeproduct数据表中查找公司图标和公司名
                String Money=cursor.getString(cursor.getColumnIndex("Money"));
                String Yesterday_income=cursor.getString(cursor.getColumnIndex("Yesterday_income"));
                String Sum_income=cursor.getString(cursor.getColumnIndex("Sum_income"));
                String Product_Number=cursor.getString(cursor.getColumnIndex("Product_Number"));
                Log.d("liangjialing","Product_Number=  "+Product_Number);

                Cursor cursor1 = db.query("financeproduct", null, "Product_Number=?",
                        new String[]{Product_Number}, null, null, null);
                String Company="";
                String Product_Name="";
                Drawable Picture=null;
                if(cursor1.moveToFirst()){
                    Log.d("liangjialing","在financeproduct中查找");
                     Company = cursor1.getString(cursor1.getColumnIndex("Company"));
                    Log.d("liangjialing","Company=  "+Company);

                    Picture = PictureFormatUtil.Bytes2Drawable(getResources()
                            , cursor1.getBlob(cursor1.getColumnIndex("Picture")));

                    Product_Name = cursor1.getString(cursor1.getColumnIndex("Product_Name"));
                    Log.d("liangjialing","Product_Name=  "+Product_Name);
                }
                else{
                    Log.d("liangjialing","cursor中没有");
                }

                HoldProduct holdProduct=new HoldProduct(Picture,Company,Product_Name,Money,Yesterday_income,Sum_income,
                        usernumber, Product_Number);
                Log.d("liangjialing","picture= "+holdProduct.getPicture());
                Log.d("liangjialing","holdprpoductList.add");
                holdproductList.add(holdProduct);
            } while (cursor.moveToNext());
        }
        db.close();
    }
}