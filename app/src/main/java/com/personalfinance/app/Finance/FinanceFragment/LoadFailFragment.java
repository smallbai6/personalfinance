package com.personalfinance.app.Finance.FinanceFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.personalfinance.app.R;


/**
 * 加载失败界面
 */
public class LoadFailFragment extends Fragment {
    View view;
    RelativeLayout relativeLayout;
    private OnfragmentClick onfragmentClick;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        view = View.inflate(container.getContext(), R.layout.load_fail, null);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.loadfail_relativeLayout);
        Log.d("liangjialing","LoadFailFragment");
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击进行重新刷新
                if(onfragmentClick!=null){
                  //  Log.d("liangjialing","LoadFailonclickb");
                    onfragmentClick.onClick(relativeLayout);
                }
            }
        });
        //加载失败界面
        return view;
    }//使用回调方法

    //定义接口变量的get方法
    public OnfragmentClick getOnfragmentClick() {
        return onfragmentClick;
    }

    //定义接口变量的set方法
    public void setOnfragmentClick(OnfragmentClick onfragmentClick) {
        this.onfragmentClick = onfragmentClick;
    }
    public interface OnfragmentClick{
       public  void onClick(View view);
    }
}