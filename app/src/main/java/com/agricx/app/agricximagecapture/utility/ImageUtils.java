package com.agricx.app.agricximagecapture.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageUtils {

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri imageUri) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            InputStream iStream = context.getContentResolver().openInputStream(imageUri);
            options.inSampleSize = 4;
            options.inJustDecodeBounds = false;
            options.inDither = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeStream(iStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
