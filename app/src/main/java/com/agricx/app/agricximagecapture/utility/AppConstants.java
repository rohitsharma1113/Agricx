package com.agricx.app.agricximagecapture.utility;

import android.support.annotation.StringRes;


public class AppConstants {
    @StringRes
    public static final int EMPTY_STRING_RES = -1;

    public static final String TEMP_IMAGE_NAME = "temp.jpg";
    public static final String AGRICX_SAMPLING_IMAGES_DIRECTORY_NAME = "agricx";
    public static final String AGRICX_TRAINING_IMAGES_DIRECTORY_NAME = "agricx_training";
    public static final String DIALOG_FRAGMENT_TAG = "dialog_tag";
    public static final String DEFAULT_ALLOWED_CAMERA_ANGLE = "12";
    public static final String DEFAULT_MARKER_TYPE = "RS175";

    public static final String FLAVOUR_CERTIFICATION = "Certification";
    public static final String FLAVOUR_TRAINING = "Training";

    public static final String EXTRA_IMAGE_FOLDER_NAME = "extra_folder_name";

    public enum DefectType {
        HEALTHY("Healthy"),
        EXTERNAL_DEFECT("External Defect"),
        INTERNAL_DEFECT("Internal Defect");

        private String name;

        DefectType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
