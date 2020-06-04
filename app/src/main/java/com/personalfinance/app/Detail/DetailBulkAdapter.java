package com.personalfinance.app.Detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.personalfinance.app.R;
import com.personalfinance.app.Sqlite.Node;
import com.personalfinance.app.Sqlite.NodeData;

import java.util.List;


public class DetailBulkAdapter extends TreeListViewAdapter {

    private OnTreeNodeCheckedChangeListener checkedChangeListener;

    public void setCheckedChangeListener(OnTreeNodeCheckedChangeListener checkedChangeListener) {
        this.checkedChangeListener = checkedChangeListener;
    }

    //进行LiistViewAdapter中的内容
    public DetailBulkAdapter(ListView listView, Context context, List<Node> datas, int defaultExpandLevel, int iconExpand, int iconNoExpand) {
        super(listView, context, datas, defaultExpandLevel, iconExpand, iconNoExpand);
    }

    @Override
    public View getConvertView(final Node node, final int position, View convertView, ViewGroup parent) {
        final ViewHoldera holdera;
        final ViewHolderb holderb;
        NodeData nodeData;
        switch (getItemViewType(position)){
            case 1:
                if(convertView==null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.detail_builkeditor_a, null);
                    holdera = new ViewHoldera(convertView);
                    convertView.setTag(holdera);
                }else{
                    holdera=(ViewHoldera)convertView.getTag();
                }
                nodeData= ( NodeData) node.getData();
                holdera.tv.setText(nodeData.getA());
                if (node.getIcon() == -1) {
                    holdera.ivExpand.setVisibility(View.INVISIBLE);
                } else {
                    holdera.ivExpand.setVisibility(View.VISIBLE);
                    holdera.ivExpand.setImageResource(node.getIcon());
                }
                //对空间进行监听
                holdera.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setChecked(node, holdera.checkBox.isChecked());

                        if (checkedChangeListener != null) {//有该回调的具体内容
                            checkedChangeListener.onCheckChange(node, position, holdera.checkBox.isChecked());
                        }
                    }
                });
                if (node.isChecked()) {
                    holdera.checkBox.setChecked(true);
                } else {
                    holdera.checkBox.setChecked(false);
                }
                break;
            case 0:
                if(convertView==null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.detail_builkeditor_b, null);
                    holderb = new ViewHolderb(convertView);
                    convertView.setTag(holderb);
                }else{
                    holderb=(ViewHolderb)convertView.getTag();
                }
                nodeData= ( NodeData) node.getData();
                holderb.tvtype.setText(nodeData.getA().substring(1));
                holderb.tvshowtime.setText(nodeData.getB());
                if(nodeData.getA().substring(0,1).equals("0")){
                    //支出
                    holderb.tvmoney.setTextColor(mContext.getResources().getColor(R.color.colorgreen));
                }
                else{
                    holderb.tvmoney.setTextColor(mContext.getResources().getColor(R.color.colorred));
                }

                holderb.tvmoney.setText(nodeData.getC());

                //对空间进行监听
                holderb.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setChecked(node, holderb.checkBox.isChecked());
                        if (checkedChangeListener != null) {
                            checkedChangeListener.onCheckChange(node, position, holderb.checkBox.isChecked());
                        }
                    }
                });

                if (node.isChecked()) {
                    holderb.checkBox.setChecked(true);
                } else {
                    holderb.checkBox.setChecked(false);
                }

                break;
        }
        return convertView;
    }

    class ViewHoldera {
        private CheckBox checkBox;
        private TextView tv;
        private ImageView ivExpand;

        public ViewHoldera(View convertView) {
            checkBox = convertView.findViewById(R.id.a_builkeditor_checkbox);
            tv = convertView.findViewById(R.id.a_builkeditor_time);
            ivExpand = convertView.findViewById(R.id.a_builkeditor_jiantou);
        }
    }


    static class ViewHolderb {
        private CheckBox checkBox;
        private TextView tvtype, tvshowtime, tvmoney;


        public ViewHolderb(View convertView) {
            checkBox = convertView.findViewById(R.id.b_builkeditor_checkbox);
            tvtype = convertView.findViewById(R.id.b_builkeditor_consumetype);
            tvshowtime = convertView.findViewById(R.id.b_builkeditor_time);
            tvmoney = convertView.findViewById(R.id.b_builkeditor_money);
        }
    }

}