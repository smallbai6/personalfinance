package com.personalfinance.app.Detail;

import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TreeAdapter<T extends Node<T>> extends BaseAdapter {
    private List<T> totalNodes = new ArrayList<>();
    private List<T> showNodes = new ArrayList<>();
    private List<T> firstLevelNodes = new ArrayList<>();
    private SparseIntArray addedChildNodeIds = new SparseIntArray();
    private OnInnerItemClickListener<T> listener;
    //private OnInnerItemLongClickListener<T> longListener;

    public interface OnInnerItemClickListener<T> {
        void onClick(T node);
    }

  /*  public interface OnInnerItemLongClickListener<T> {
        void onLongClick(T node);
    }*/

    public TreeAdapter(List<T> nodes) {
        setNodes(nodes);
    }

    public void setOnInnerItemClickListener(OnInnerItemClickListener<T> listener) {
        this.listener = listener;
    }

 /*   public void setOnInnerItemLongClickListener(OnInnerItemLongClickListener<T> listener) {
        longListener = listener;
    }*/

    public void setNodes(List<T> nodes) {
        if (nodes != null) {
            totalNodes = nodes;
            //过滤出显示的节点
            init();
            super.notifyDataSetChanged();
        }
    }

    private void init() {
        showNodes.clear();
        initNodes();
        addedChildNodeIds.clear();
        showNodes.addAll(firstLevelNodes);
        filterShowAndSortNodes();
    }

    @Override
    public void notifyDataSetChanged() {
        init();
        super.notifyDataSetChanged();
    }

    private void initNodes() {
        firstLevelNodes.clear();
        //先循环一次，获取最小的level
        Integer level = null;
        for (T node : totalNodes) {
            if (level == null || level > node.level) {
                level = node.level;
            }
        }
        for (T node : totalNodes) {
            //过滤出最外层,找出最外层的节点
            if (node.level == level) {
                firstLevelNodes.add(node);
            }
            //清空之前添加的
            if (node.hasChild()) {
                node.childNodes.clear();
            }
            //给节点添加子节点并排序
            for (T t : totalNodes) {
                if (node.id == t.id && node != t) {//不是本身
                    throw new IllegalArgumentException("id cannot be duplicated");
                }
                if (node.id == t.pId && node.level != t.level) {//是子节点且不是同一级别
                    node.addChild(t);
                }
            }
            if (node.hasChild()) {
                Collections.sort(node.childNodes);//子节点排序
            }
        }
        Collections.sort(firstLevelNodes);//进行排序
    }

    private void filterShowAndSortNodes() {
        for (int i = 0; i < showNodes.size(); i++) {
            T node = showNodes.get(i);
            int value = addedChildNodeIds.get(node.id);
            //  Log.d("liang","for//addedChildNodeIds=="+addedChildNodeIds.get(node.id)+
            //          "i="+i);
            if (value == 0 && node.isExpand && node.hasChild()) {//如果有子节点且展开则将子节点加入到showNodes中
                List<T> list = new ArrayList<>(showNodes);
                list.addAll(i + 1, node.childNodes);//父节点下加入子节点
                showNodes = list;
                addedChildNodeIds.put(node.id, 1);
                //   Log.d("liang","if//addedChildNodeIds=="+addedChildNodeIds.get(node.id));
                filterShowAndSortNodes();
                break;
            }
        }
    }

    @Override
    public int getCount() {//不断变化的
        return showNodes.size();
    }

    @Override
    public T getItem(int position) {
        return showNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder<T> holder;

        if (convertView == null) {
           // Log.d("liang", "null");
            holder = getHolder(position);
        } else {
          //  Log.d("liang", "not null");
            holder = (Holder<T>) convertView.getTag();
        }
      //  Log.d("liang", "又回到getView中position= "+position);
        T node = showNodes.get(position);
        holder.setData(node);
        holder.position = position;
      //  Log.d("liang", "holder.position= "+holder.position);
        View view = holder.getConvertView();
        view.setOnClickListener(clickListener);
       /* if (!node.hasChild()) {
            view.setOnLongClickListener(longClickListener);
        }*/
        return view;
    }

    public abstract static class Holder<T> {
        private View convertView;
        int position;

        public Holder() {
            convertView = createConvertView();
            convertView.setTag(this);
        }

        public View getConvertView() {
            return convertView;
        }
        /**
         * 创建界面
         */
        protected abstract View createConvertView();
        /**
         * 设置数据
         */
        protected abstract void setData(T node);


    }

    protected abstract Holder<T> getHolder(int position);//方法

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Holder<T> holder = (Holder<T>) v.getTag();
            T node = showNodes.get(holder.position);
            if (node.hasChild()) {
                node.isExpand = !node.isExpand;
                if (!node.isExpand) {
                    fold(node.childNodes);
                }

                showNodes.clear();
                addedChildNodeIds.clear();
                showNodes.addAll(firstLevelNodes);
                filterShowAndSortNodes();
                TreeAdapter.super.notifyDataSetChanged();//更改列表的数据

            } else if (listener != null) {
                listener.onClick(node);
            }
        }
    };

    /*private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (longListener != null) {
                Holder<T> holder = (Holder<T>) v.getTag();
                longListener.onLongClick(showNodes.get(holder.position));
            }
            return true;
        }
    };*/

    //递归收起节点及子节点
    private void fold(List<T> list) {
        for (T t : list) {
            t.isExpand = false;
            if (t.hasChild()) {
                fold(t.childNodes);
            }
        }
    }
}