package com.personalfinance.app.Detail;

import com.personalfinance.app.Sqlite.Node;

public interface OnTreeNodeCheckedChangeListener {

    void onCheckChange(Node node, int position, boolean isChecked);
}