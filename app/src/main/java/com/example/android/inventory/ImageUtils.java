package com.example.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pallavi J on 24-06-2017.
 */

public class ImageUtils {

    private static final String LOG_TAG = ImageUtils.class.getSimpleName();

    public static final int PICK_IMAGE_REQUEST = 0;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String FILE_PROVIDER_AUTHORITY = "com.example.android.inventory.myfileprovider";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String CAMERA_DIR = "/dcim/";

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Uri getShareableImageUri(Context context, Uri uri, Bitmap bitmap) {
        Uri imageUri;
        String filename = getFilePath(context, uri);
        saveBitmapToFile(context.getCacheDir(), filename, bitmap, Bitmap.CompressFormat.JPEG, 100);
        File imageFile = new File(context.getCacheDir(), filename);

        imageUri = FileProvider.getUriForFile(
                context, FILE_PROVIDER_AUTHORITY, imageFile);
        return imageUri;
    }

    private static String getFilePath(Context context, Uri uri) {
        /*
         * Get the file's content URI from the incoming Intent,
         * then query the server app to get the file's display name
         * and size.
         */
        Cursor returnCursor =
                context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);

        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

        return fileName;
    }

    /*
    * Bitmap.CompressFormat can be PNG,JPEG or WEBP.
    *
    * quality goes from 1 to 100. (Percentage).
    *
    * dir you can get from many places like Environment.getExternalStorageDirectory() or mContext.getFilesDir()
    * depending on where you want to save the image.
    */
    private static boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                            Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format, quality, fos);
            fos.close();

            return true;
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return false;
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir(context);
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private static File getAlbumDir(Context context) {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = new File(Environment.getExternalStorageDirectory()
                    + CAMERA_DIR
                    + context.getString(R.string.app_name));

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        return null;
                    }
                }
            }

        } else {
            Log.v(context.getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }
}
