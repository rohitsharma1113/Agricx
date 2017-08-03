package com.agricx.app.agricximagecapture.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.data.TrainingDataUtil;
import com.agricx.app.agricximagecapture.utility.AppConstants.DefectType;

import java.util.ArrayList;

public class DefectVarietySelectionFragment extends DialogFragment {
    private static final String KEY_SELECTION_TYPE = "type";
    public static final int EXTRA_DEFECTS_SELECTION = 1;
    public static final int EXTRA_VARIETY_SELECTION = 2;

    private SelectionActionListener listener;
    private int selectionType;

    public static DefectVarietySelectionFragment newInstance(int selectionType) {
        Bundle args = new Bundle();
        args.putInt(KEY_SELECTION_TYPE, selectionType);
        DefectVarietySelectionFragment fragment = new DefectVarietySelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionActionListener) {
            listener = (SelectionActionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_defect_variety_selection, container, false);
        LinearLayout listContainer = (LinearLayout) rootView.findViewById(R.id.listContainer);

        View titleView;
        TextView tvTitle;
        View rowView;
        TextView tvRow;
        Bundle args = getArguments();
        selectionType = args.getInt(KEY_SELECTION_TYPE);
        if (selectionType == EXTRA_DEFECTS_SELECTION) {
            ArrayList<TrainingDataUtil.BaseDefect> defectsListForType;
            for (DefectType defectType : DefectType.values()) {
                titleView = inflater.inflate(R.layout.view_defect_title, listContainer, false);
                listContainer.addView(titleView);
                tvTitle = (TextView) titleView.findViewById(R.id.item_tv_title);
                tvTitle.setText(defectType.getName());
                defectsListForType = TrainingDataUtil.getDefectListFromDefectType(defectType);
                for (TrainingDataUtil.BaseDefect defect : defectsListForType) {
                    rowView = inflater.inflate(R.layout.view_defect_variety_row, listContainer, false);
                    listContainer.addView(rowView);
                    tvRow = (TextView) rowView.findViewById(R.id.item_tv_row);
                    tvRow.setText(defect.getDefectFullName());
                    tvRow.setTag(defect);
                    tvRow.setOnClickListener(onItemClick);
                }
            }
        } else if (selectionType == EXTRA_VARIETY_SELECTION) {
            ArrayList<TrainingDataUtil.BaseVariety> baseVarietyList = TrainingDataUtil.getVarietyList();
            titleView = inflater.inflate(R.layout.view_defect_title, listContainer, false);
            listContainer.addView(titleView);
            tvTitle = (TextView) titleView.findViewById(R.id.item_tv_title);
            tvTitle.setText("Variety");
            for (TrainingDataUtil.BaseVariety variety : baseVarietyList) {
                rowView = inflater.inflate(R.layout.view_defect_variety_row, listContainer, false);
                listContainer.addView(rowView);
                tvRow = (TextView) rowView.findViewById(R.id.item_tv_row);
                tvRow.setText(variety.getVarietyFullName());
                tvRow.setTag(variety);
                tvRow.setOnClickListener(onItemClick);
            }
        }

        return rootView;
    }

    private View.OnClickListener onItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onDefectVarietyItemSelected(selectionType, v.getTag());
                dismiss();
            }
        }
    };

    public interface SelectionActionListener {
        <T> void onDefectVarietyItemSelected(int selectionType, T item);
    }
}
