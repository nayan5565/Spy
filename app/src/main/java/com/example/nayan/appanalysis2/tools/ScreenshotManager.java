package com.example.nayan.appanalysis2.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.nayan.appanalysis2.R;
import com.example.nayan.appanalysis2.database.DBManager;
import com.example.nayan.appanalysis2.model.MScreenshot;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Dev on 1/1/2018.
 */

public class ScreenshotManager {
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    public static final ScreenshotManager INSTANCE = new ScreenshotManager();
    private Intent mIntent;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static final String TAG = ScreenshotManager.class.getName();
    public MediaProjection mediaProjection;
    private MScreenshot mScreenshot;
    public static long id;

    private ScreenshotManager() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestScreenshotPermission(@NonNull Activity activity, int requestId) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), requestId);
    }


    public void onActivityResult(int resultCode, Intent data, Context context) {
        if (resultCode == Activity.RESULT_OK && data != null)
            mIntent = data;
        else mIntent = null;
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
            File storeDirectory = new File(STORE_DIRECTORY);
            if (!storeDirectory.exists()) {
                boolean success = storeDirectory.mkdirs();
                if (!success) {
                    Utils.log(TAG, "failed to create file storage directory.");
                    return;
                }
            }
        } else {
            Utils.log(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @UiThread
    public boolean takeScreenshot(@NonNull final Context context) {
        if (mIntent == null)
            return false;
        final MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (mediaProjection == null) {

        } else {
            mediaProjection.stop();

        }
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mIntent);

        if (mediaProjection == null)
            return false;
        final int density = context.getResources().getDisplayMetrics().densityDpi;
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        final int width = size.x, height = size.y;

        // start capture reader
        final ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        final VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay(SCREENCAP_NAME, width, height, density, VIRTUAL_DISPLAY_FLAGS, imageReader.getSurface(), null, null);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(final ImageReader reader) {
                Utils.log("AppLog", "onImageAvailable");
                if (mediaProjection == null) {

                } else {
                    mediaProjection.stop();
                }

                Image image = null;
                Bitmap bitmap = null;
                FileOutputStream fos = null;
                File externalFilesDir = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride(), rowStride = planes[0].getRowStride(), rowPadding = rowStride - pixelStride * width;
                        bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);

                        String gmail = Utils.getPhoneGmailAcc(context);
                        String device = Utils.getDeviceId(context);
                        String gn = gmail.split("@")[0];
                        String gm = gn.replace('.', '_');
                        // write bitmap to a file
                        String now = Utils.getToday();
                        fos = new FileOutputStream(STORE_DIRECTORY + "/" + device + "_" + gn + "_" + now + ".png");
                        mScreenshot = new MScreenshot();
                        mScreenshot.setImgName("/" + device + "_" + gn + "_" + now + ".png");

                        id = DBManager.getInstance().addScreenshot(mScreenshot);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos);
//                        getImage(context, mScreenshot.getImgName(), gmail, device, id);
                        IMAGES_PRODUCED++;
                        Utils.log(TAG, "captured image: " + mScreenshot.getImgName());
//                        Utils.toastShow("captured image:" + mScreenshot.getImgName());

                    }
                } catch (Exception e) {
                    if (bitmap != null)
                        bitmap.recycle();
                    e.printStackTrace();
                }
                if (image != null)
                    image.close();
//                reader.close();

            }
        }, null);

        mediaProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
                Utils.log("ScreenCapture", "stopping projection.");
                if (virtualDisplay != null)
                    virtualDisplay.release();
                imageReader.setOnImageAvailableListener(null, null);
                mediaProjection.unregisterCallback(this);
                mediaProjection = null;
            }
        }, null);
        return true;
    }

    public void getImage(Context context, String image, String email, String device, long id) {
        File externalFilesDir = MainApplication.context.getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath() + "/screenshots/" + image;
        Utils.log("getImage", " from " + path);
        File file = new File(path);
        if (file.exists()) {
//            Toast.makeText(context, "file exists", Toast.LENGTH_SHORT).show();
            Utils.log("TEST", "FILE EXISTS");
            sendImageToServer(context, file, id, image, email, device);
        } else {
//            Toast.makeText(context, "file is not exists", Toast.LENGTH_SHORT).show();
            Utils.log("TEST", "FILE NOT EXISTS");
        }
    }

    public void sendImageToServer(final Context context, File file, final long id, final String image, final String email, final String device) {
        if (!Utils.isInternetOn())
            return;
        Utils.log("TEST", "call screenshot server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("device_id", device);
        params.put("app_name", "TestApp");
        params.put("duration", "");
        try {
            params.put("screenshot", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post("http://www.swapnopuri.com/app/calllog/api/upload_screenshot/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Utils.log("Image ", "response " + response.toString());
                    if (response.has("status") && response.getString("status").equals("success")) {
                        Utils.log("Image", "  uploaded");
                        deleteImageFromSdcard(id, image);
//                        Toast.makeText(context, "image uploaded", Toast.LENGTH_SHORT).show();


                    } else {
                        Utils.log("Image", " not uploaded");
                        Toast.makeText(context, "image uploaded failed", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Utils.log("IMAGE ", "failure " + responseString);
            }
        });
    }

    private void deleteImageFromSdcard(long id, String image) {
//        mImages = DBManager.getInstance().getScreenshot();

        File externalFilesDir = MainApplication.context.getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath() + "/screenshots/" + image;
        Utils.log("file  :", " " + path + image);
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                DBManager.getInstance().deleteData(DBManager.TABLE_SCREENSHOT, "id", id + "");
                Utils.log("file Deleted :", " " + path + image);
            } else {
                Utils.log("file Not Deleted :", " " + path + image);
            }
        }
    }
}
