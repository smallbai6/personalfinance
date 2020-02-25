package com.personalfinance.app.Detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.personalfinance.app.R;

import java.util.List;

public class DetailAdapter extends TreeListViewAdapter {

    private OnInnerItemClickListener listener;
    private OnInnerItemLongClickListener longListener;
    public void setOnInnerItemClickListener(OnInnerItemClickListener listener){
        this.listener=listener;
    }
    public void setOnInnerItemLongClickListener(OnInnerItemLongClickListener longListener){
        this.longListener=longListener;
    }


    //进行ListViewAdapter中的内容
    public DetailAdapter(ListView listView, Context context, List<Node> datas, int defaultExpandLevel, int iconExpand, int iconNoExpand) {
        super(listView, context, datas, defaultExpandLevel, iconExpand, iconNoExpand);
    }

    @Override
    public View getConvertView(final Node node, final int position, View convertView, ViewGroup parent) {
        final ViewHoldera holdera;
        final ViewHolderb holderb;
        DetailNodeData detailNodeData;
        switch (getItemViewType(position)){
            case 1:
                if(convertView==null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.detail_type_a, null);
                    holdera = new ViewHoldera(convertView);
                    convertView.setTag(holdera);
                }else{
                    holdera=(ViewHoldera)convertView.getTag();
                }
                if (node.getIcon() == -1) {
                    holdera.iv.setVisibility(View.INVISIBLE);
                } else {
                    holdera.iv.setVisibility(View.VISIBLE);
                    holdera.iv.setImageResource(node.getIcon());
                }
                detailNodeData=(DetailNodeData)node.getData();
                holdera.tva.setText(detailNodeData.a);
                holdera.tvb.setText(detailNodeData.b);
                holdera.tvc.setText(detailNodeData.c);
                holdera.tvd.setText(detailNodeData.d);
                holdera.tve.setText(detailNodeData.e);
                break;
            case 0:
                if(convertView==null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.detail_type_b, null);
                    holderb = new ViewHolderb(convertView);
                    convertView.setTag(holderb);
                }else{
                    holderb=(ViewHolderb)convertView.getTag();
                }
                detailNodeData=(DetailNodeData)node.getData();
                holderb.tvf.setVisibility((detailNodeData.f.equals("")) ? View.GONE : View.VISIBLE);
                //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tva.getLayoutParams();
                //params.leftMargin = 60;
                //tva.setLayoutParams(params);
                holderb.tva.setText(detailNodeData.a);
                holderb.tvb.setText(detailNodeData.b);
                if (detailNodeData.c.substring(0, 1).equals("0")) {
                    holderb.tve.setTextColor(mContext.getResources().getColor(R.color.colorgreen));
                } else {
                    holderb.tve.setTextColor(mContext.getResources().getColor(R.color.colorred));
                }
                holderb.tvc.setText(detailNodeData.c.substring(1));
                holderb.tvd.setText(detailNodeData.d);
                holderb.tve.setText(detailNodeData.e);
                holderb.tvf.setText(detailNodeData.f);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(listener!=null){
                            listener.onClick(mNodes.get(position), position);
                        }
                    }
                });
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(longListener!=null){
                            longListener.onClick(mNodes.get(position),position);
                        }
                        return true;
                    }
                });
                break;
        }
        return convertView;
    }

    class ViewHoldera {
        private ImageView iv;
        private TextView tva, tvb, tvc, tvd, tve;
        private RelativeLayout relativeLayout;

        public ViewHoldera(View convertView) {
            relativeLayout = (RelativeLayout) convertView.findViewById(R.id.a_detail_relativeLayout);
            iv = (ImageView) convertView.findViewById(R.id.a_detail_jiantou);
            tva = (TextView) convertView.findViewById(R.id.a_detail_month);
            tvb = (TextView) convertView.findViewById(R.id.a_detail_year);
            tvc = (TextView) convertView.findViewById(R.id.a_detail_totalmoney);
            tvd = (TextView) convertView.findViewById(R.id.a_detail_incomemoney);
            tve = (TextView) convertView.findViewById(R.id.a_detail_expendmoney);
        }
    }


    static class ViewHolderb {
        private TextView tva, tvb, tvc, tvd, tve, tvf;
        public ViewHolderb(View convertView) {
            tva = (TextView) convertView.findViewById(R.id.b_detail_day);
            tvb = (TextView) convertView.findViewById(R.id.b_detail_week);
            tvc = (TextView) convertView.findViewById(R.id.b_detail_consumetype);
            tvd = (TextView) convertView.findViewById(R.id.b_detail_time);
            tve = (TextView) convertView.findViewById(R.id.b_detail_money);
            tvf = (TextView) convertView.findViewById(R.id.b_detail_text);
        }
    }
}