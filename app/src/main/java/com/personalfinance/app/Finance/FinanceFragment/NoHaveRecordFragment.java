package com.personalfinance.app.Finance.FinanceFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.personalfinance.app.R;

public class NoHaveRecordFragment extends Fragment {
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        view = View.inflate(container.getContext(), R.layout.nohave_record, null);
        return view;
    }
}