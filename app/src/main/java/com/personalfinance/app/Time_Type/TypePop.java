package com.personalfinance.app.Time_Type;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.personalfinance.app.R;

public class TypePop extends PopupWindow implements View.OnClickListener {
    private Cursor cursor;
    private Button btncancel;
    private PopupWindow timepop;
    private TypeChoose mTypeChoose;
    private OnTypeSetListener mOnTypeSetListener;//接口
    int select = 0;
    // private List<String> chooseList=new ArrayList<>();
    private String[] chooseList = new String[15];
    private int position = 0;
    private Activity context;
    private SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";

    public TypePop(final Activity context, View parent, int typecategory, String typeposition) {
        super(context);
        this.context = context;
        //  Log.d("liang","数据库");
        try {
            db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            //cursor = db.query("expendtype", null, null, null, null, null, null);
            if (typecategory == 0) {
                cursor = db.query("expendtype", null, null, null, null, null, null);
            } else if (typecategory == 1) {
                cursor = db.query("incometype", null, null, null, null, null, null);
            }
            int truth_long = 0;
            if (cursor.moveToFirst()) {
                do {
                    String name = "";
                    if (typecategory == 0) {
                        name = cursor.getString(cursor.getColumnIndex("Type_Name"));
                    } else if (typecategory == 1) {
                        name = cursor.getString(cursor.getColumnIndex("Type_Name"));
                    }
                    chooseList[truth_long] = name;
                    truth_long++;

                } while (cursor.moveToNext());
            }

            String[] List = new String[truth_long];
            for (int i = 0; i < truth_long; i++) {
                List[i] = chooseList[i];
            }
            for (int i = 0; i < truth_long; i++) {
                if (chooseList[i].equals(typeposition)) {
                    position = i;
                    break;
                }
            }
            select = position;
            mTypeChoose = new TypeChoose(context, List, position);
        } catch (Exception e) {

        } finally {
            cursor.close();
        }
        btncancel = (Button) mTypeChoose.findViewById(R.id.type_cancel);
        timepop = new PopupWindow(mTypeChoose,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timepop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        timepop.setOutsideTouchable(true);
        // timepop.setTouchable(true);
        //timepop.setFocusable(true);
        timepop.showAtLocation(parent, Gravity.BOTTOM, 0, 0);


        timepop.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mOnTypeSetListener != null) {
                    mOnTypeSetListener.OnTypeSet(chooseList[select]);
                }
            }
        });
        mTypeChoose.setOnTypeChangedListener(new TypeChoose.OnTypeChangedListener() {
            @Override
            public void onTypeChanged(TypeChoose view, int type) {
                select = type;
                if (mOnTypeSetListener != null) {
                    mOnTypeSetListener.OnTypeSet(chooseList[type]);
                }
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.type_cancel:
                timepop.dismiss();
                break;
        }
    }

    public interface OnTypeSetListener {//接口

        void OnTypeSet(String date);//方法
    }

    public void setOnTypeSetListener(OnTypeSetListener callBack) {//回调
        mOnTypeSetListener = callBack;
    }
}