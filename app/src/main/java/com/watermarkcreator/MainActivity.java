package com.watermarkcreator;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.watermarkcreator.util.BitmapDecoderTask;
import com.watermarkcreator.util.OverlayCreatorTask;
import com.watermarkcreator.util.WaterMarkCreator;


/**
 * Created by b_ashish on 29-Jun-16.
 */
public class MainActivity extends AppCompatActivity {

    private static final int CHOOSE_BASE_PHOTO = 1001;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 99;

    private Bitmap mSourceBitmap, mWatermarkBitmap;
    private ImageView baseImageView;

    private int mFromTop = 0, mFromLeft = 0;
    private float mScale = 0.5f;
    private OverlayCreatorTask mOverlayBitmapTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText waterMark = (EditText) findViewById(R.id.watermarkText);


        baseImageView = (ImageView) findViewById(R.id.image_holder);

        findViewById(R.id.convert_text_to_bmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String waterMarkText = waterMark.getText().toString();
                if (waterMarkText != null && !waterMarkText.equals("")) {
                    placeTextOnImage(waterMarkText);
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_text_to_convert, Toast.LENGTH_LONG).show();
                }
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.baseImageButton:

                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // Show an explanation to the user *asynchronously* -- don't block
                                // this thread waiting for the user's response! After the user
                                // sees the explanation, try again to request the permission.
                                Toast.makeText(MainActivity.this, "shouldShowRequestPermissionRationale()", Toast.LENGTH_SHORT).show();
                                pickImageFromGallery();
                            } else {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                            }
                        } else {
                            pickImageFromGallery();
                        }
                        break;
                }
            }
        };


        findViewById(R.id.baseImageButton).setOnClickListener(listener);


        int maxWidth = getDimens(R.id.image_holder)[0];
        int maxHeight = getDimens(R.id.image_holder)[1];

        SeekBar leftSeek = (SeekBar) findViewById(R.id.left_seek);

        leftSeek.setMax(maxWidth);
        leftSeek.setProgress(mFromLeft);

        SeekBar fromTopSeek = (SeekBar) findViewById(R.id.top_seek);
        fromTopSeek.setMax(maxHeight);
        fromTopSeek.setProgress(mFromTop);

        SeekBar scaleSeekbar = (SeekBar) findViewById(R.id.scale_size);
        scaleSeekbar.setProgress((int) (mScale * 100));

        ((TextView) findViewById(R.id.left_label)).setText(getString(R.string.from_left, mFromLeft));
        ((TextView) findViewById(R.id.top_label)).setText(getString(R.string.from_top, mFromTop));

        ((TextView) findViewById(R.id.scale_label)).setText(getString(R.string.scale_factor, mScale));


        leftSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mFromLeft = progress;

                ((TextView) findViewById(R.id.left_label)).setText(getString(R.string.from_left, mFromLeft));
                invalidateOverlay();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        fromTopSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mFromTop = progress;

                ((TextView) findViewById(R.id.top_label)).setText(getString(R.string.from_top, mFromTop));
                invalidateOverlay();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        scaleSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mScale = progress / 100f;

                ((TextView) findViewById(R.id.scale_label)).setText(getString(R.string.scale_factor, mScale));
                invalidateOverlay();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        invalidateOverlay();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // storage-related task you need to do.
                    pickImageFromGallery();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "You must enable the permission in order to access this feature", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    private void pickImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, CHOOSE_BASE_PHOTO);
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case CHOOSE_BASE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {

                    final Uri imageUri = imageReturnedIntent.getData();
                    int dimens[] = getDimens(R.id.image_holder);
                    new BitmapDecoderTask()
                            .setDecodingImageReference(imageUri, getContentResolver())
                            .setRequiredWidth(dimens[0])
                            .setRequiredHeight(dimens[1])
                            .setListener(new BitmapDecoderTask.OnDecodeListener() {
                                @Override
                                public void onDecode(BitmapDecoderTask task, Bitmap bitmap) {

                                    if (requestCode == CHOOSE_BASE_PHOTO) {
                                        mSourceBitmap = bitmap;
                                    }
                                    invalidateOverlay();
                                }
                            })
                            .decode();
                }
        }
    }

    private void placeTextOnImage(CharSequence text) {
        int dimens[] = getDimens(R.id.image_holder);

        WaterMarkCreator.createBuilder().setText(text)
                .setTextMaxWidth((int) convertDpToPixel(dimens[0], MainActivity.this))
                .setTextSize((int) (110))
                .setTextColor(Color.RED).build().setListener(new WaterMarkCreator.OnDecodeListener() {
            @Override
            public void onDecode(WaterMarkCreator task, Bitmap bitmap) {
                mWatermarkBitmap = bitmap;
                invalidateOverlay();
            }
        }).execute();
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }


    public void invalidateOverlay() {
        if (mSourceBitmap != null && mWatermarkBitmap != null) {
            int maxWidth = mSourceBitmap.getWidth();
            int maxHeight = mSourceBitmap.getHeight();


            SeekBar fromLeftSeek = (SeekBar) findViewById(R.id.left_seek);


            fromLeftSeek.setMax(maxWidth);
            fromLeftSeek.setProgress(mFromLeft);

            SeekBar fromTopSeek = (SeekBar) findViewById(R.id.top_seek);
            fromTopSeek.setMax(maxHeight);
            fromTopSeek.setProgress(mFromTop);


//          If you're working with larger bitmaps and continuously changing the scale value or angle value, you might notice the lag between slider change
//            and the image position. To get rid of that, uncomment the following codes.
            if (mOverlayBitmapTask != null && mOverlayBitmapTask.getStatus() == OverlayCreatorTask.Status.RUNNING) {
                mOverlayBitmapTask.cancel(true);
            }


            mOverlayBitmapTask = new OverlayCreatorTask();
            mOverlayBitmapTask.setBaseBitmap(mSourceBitmap)
                    .setMergeBitmap(mWatermarkBitmap)
                    .setMergeListener(new OverlayCreatorTask.OnMergeListener() {
                        @Override
                        public void onMerge(OverlayCreatorTask task, Bitmap mergedBitmap) {
                            baseImageView.setImageBitmap(mergedBitmap);
                        }
                    })
                    .setScale(mScale)
                    .setOffsets(mFromLeft, mFromTop)
                    .overlay();

        } else {
            if (mSourceBitmap != null) {
                baseImageView.setImageBitmap(mSourceBitmap);
            }
            if (mWatermarkBitmap != null) {
                baseImageView.setImageBitmap(mWatermarkBitmap);
            }
        }

    }

    public int[] getDimens(int resId) {
        int width = findViewById(resId).getMeasuredWidth();
        int height = findViewById(resId).getMeasuredHeight();
        return new int[]{width, height};
    }
}
