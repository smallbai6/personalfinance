package com.personalfinance.app.Statistical;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.personalfinance.app.Detail.OnInnerItemClickListener;
import com.personalfinance.app.Detail.OnInnerItemLongClickListener;
import com.personalfinance.app.Detail.TreeListViewAdapter;
import com.personalfinance.app.R;
import com.personalfinance.app.Sqlite.Node;
import com.personalfinance.app.Sqlite.NodeData;

import java.util.List;

public class StatisticalAdapter extends TreeListViewAdapter {

    private OnInnerItemClickListener listener;
    private OnInnerItemLongClickListener longListener;
    public void setOnInnerItemClickListener(OnInnerItemClickListener listener){
        this.listener=listener;
    }
    public void setOnInnerItemLongClickListener(OnInnerItemLongClickListener longListener){
        this.longListener=longListener;
    }


    //进行ListViewAdapter中的内容
    public StatisticalAdapter(ListView listView, Context context, List<Node> datas, int defaultExpandLevel, int iconExpand, int iconNoExpand) {
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
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.statistical_peditor_a, null);
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
                nodeData=(NodeData)node.getData();
                holdera.tva.setText(nodeData.getA().substring(8,10)+"日");
                holdera.tvb.setText(nodeData.getA().substring(0,7)+nodeData.getA().substring(16));
                break;
            case 0:
                if(convertView==null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.statistical_peditor_b, null);
                    holderb = new ViewHolderb(convertView);
                    convertView.setTag(holderb);
                }else{
                    holderb=(ViewHolderb)convertView.getTag();
                }
                nodeData=(NodeData)node.getData();

                //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tva.getLayoutParams();
                //params.leftMargin = 60;
                //tva.setLayoutParams(params);
                holderb.tva.setText(nodeData.getA().substring(1));
                holderb.tvb.setText(nodeData.getB());
                holderb.tvc.setVisibility((nodeData.getC().equals("")) ? View.GONE : View.VISIBLE);
                holderb.tvc.setText(nodeData.getC());
                if (nodeData.getA().substring(0, 1).equals("0")) {
                    holderb.tvd.setTextColor(mContext.getResources().getColor(R.color.colorgreen));
                } else {
                    holderb.tvd.setTextColor(mContext.getResources().getColor(R.color.colorred));
                }
                holderb.tvd.setText(nodeData.getD());

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
        private TextView tva, tvb;
       // private RelativeLayout relativeLayout;

        public ViewHoldera(View convertView) {
            //relativeLayout = (RelativeLayout) convertView.findViewById(R.id.a_detail_relativeLayout);
            iv = (ImageView) convertView.findViewById(R.id.a_statistical_imageView);
            tva = (TextView) convertView.findViewById(R.id.a_statistical_day);
            tvb = (TextView) convertView.findViewById(R.id.a_statistical_date);
        }
    }


    static class ViewHolderb {
        private TextView tva, tvb, tvc,tvd;
        public ViewHolderb(View convertView) {
            tva = (TextView) convertView.findViewById(R.id.b_statistical_type);
            tvb = (TextView) convertView.findViewById(R.id.b_statistical_time);
            tvc = (TextView) convertView.findViewById(R.id.b_statistical_text);
            tvd=(TextView)convertView.findViewById(R.id.b_statistical_money);
        }
    }
}