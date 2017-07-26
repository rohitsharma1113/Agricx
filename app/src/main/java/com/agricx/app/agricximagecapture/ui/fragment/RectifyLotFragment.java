package com.agricx.app.agricximagecapture.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.agricx.app.agricximagecapture.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RectifyLotFragment extends DialogFragment {
    private RecertifyLotIdListener mListener;

    @BindView(R.id.etRecertifyLotId)
    EditText etRecertifyLotId;

    @BindView(R.id.btnRecertify)
    Button bRecertify;

    public RectifyLotFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RecertifyLotIdListener) {
            mListener = (RecertifyLotIdListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rectify_lot, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick(R.id.btnRecertify)
    void onClick(View view) {
        if (mListener != null) {
            mListener.onRecertificationLotIdEntered(etRecertifyLotId.getText().toString().trim());
        }
    }

    public interface RecertifyLotIdListener {
        void onRecertificationLotIdEntered(String lotId);
    }
}
