package com.agricx.app.agricximagecapture.ui.fragment;


import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.utility.AppConstants;
import com.agricx.app.agricximagecapture.utility.Utility;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ImagePreviewFragment extends DialogFragment {

    private static final String TAG = ImagePreviewFragment.class.getSimpleName();
    private static final String EXTRA_IMAGE_URI = "extra_image_uri";

    @BindView(R.id.frag_ip_iv_image) ImageView ivPreview;

    public ImagePreviewFragment() {
    }

    public static ImagePreviewFragment getInstance(Uri imageUri){
        ImagePreviewFragment fragment = new ImagePreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_IMAGE_URI, imageUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_preview, container, false);
        ButterKnife.bind(this, rootView);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        showImage();
        return rootView;
    }

    private void showImage(){
        Uri imageUri = getArguments().getParcelable(EXTRA_IMAGE_URI);
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            if (imageBitmap != null){
                ivPreview.setImageBitmap(imageBitmap);
            } else {
                Toast.makeText(getContext(), getActivity().getString(R.string.could_not_load_image), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), getActivity().getString(R.string.could_not_load_image), Toast.LENGTH_SHORT).show();
        }
    }
}
