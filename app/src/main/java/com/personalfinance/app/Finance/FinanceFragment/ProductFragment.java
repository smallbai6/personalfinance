package com.personalfinance.app.Finance.FinanceFragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Finance.Adapter.ProductAdapter;
import com.personalfinance.app.Finance.Class.Product;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.PictureFormatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 理财产品显示界面Fragment,从数据库中显示
 */
public class ProductFragment extends Fragment {
    View view;
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        view = View.inflate(container.getContext(), R.layout.recyclerlist, null);

        Bundle bundle = this.getArguments();//得到从Activity传来的数据
        String UserNumber = bundle.getString("UserNumber");//根据用户数据进行在数据库中进行查找
        InitProduct(UserNumber);
        return view;
    }
    private List<Product> productList = new ArrayList<>();

    private void InitProduct(String usernumber) {//显示列表适配
        InitList(usernumber);//初始化列表
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.Recyclerlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        ProductAdapter adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);
    }


    private void InitList(String usernumber) {//列表的建立,从SQLite数据库中查找
        db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.query("financeproduct", null, null,
                null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Drawable Picture = PictureFormatUtil.Bytes2Drawable(getResources()
                        , cursor.getBlob(cursor.getColumnIndex("Picture")));
                String Product_Name = cursor.getString(cursor.getColumnIndex("Product_Name"));
                String Yield = cursor.getString(cursor.getColumnIndex("Yield"));
                String Purchase_Amount = cursor.getString(cursor.getColumnIndex("Purchase_Amount"));
                String Product_Number = cursor.getString(cursor.getColumnIndex("Product_Number"));
                //String UserName=cursor.getString(cursor.getColumnIndex("User_Name"));
                Product product = new Product(Picture, Product_Name, Yield, Purchase_Amount, Product_Number, usernumber);
                productList.add(product);
            } while (cursor.moveToNext());
        }
        db.close();
    }
}