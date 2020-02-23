package com.personalfinance.app.Detail.DetailBulk;

import com.personalfinance.app.Detail.Node;

public interface OnTreeNodeCheckedChangeListener {

    void onCheckChange(Node node, int position, boolean isChecked);
}