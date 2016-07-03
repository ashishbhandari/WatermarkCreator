package com.watermarkcreator.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;

/**
 * Created by b_ashish on 29-Jun-16.
 */

public class OverlayCreatorTask extends AsyncTask<Void, Void, Bitmap> {

    private Bitmap mBaseBitmap;
    private Bitmap mMergeBitmap;

    private float mScale = 0.5f;
    private int mTopOffset = 0;
    private int mLeftOffset = 0;
    private int mAlpha = 0;
    private OnMergeListener mMergeListener;

    /**
     * Sets the scaling of the overlay image.
     *
     * @param scale - float value from 0.0 to 1.0 represents the scale.
     * @return the related OverlayCreatorTask
     */
    public OverlayCreatorTask setScale(float scale) {
        this.mScale = scale;
        return this;
    }

    public OverlayCreatorTask setAlpha(int alpha) {
        this.mAlpha = alpha;
        return this;
    }


    /**
     * Sets the base bitmap image.
     *
     * @param mBaseBitmap - base bitmap
     * @return the related OverlayCreatorTask
     */
    public OverlayCreatorTask setBaseBitmap(Bitmap mBaseBitmap) {
        this.mBaseBitmap = mBaseBitmap;
        return this;
    }

    /**
     * Sets the overlay bitmap image.
     *
     * @param mMergeBitmap - merging bitmap image.
     * @return the related OverlayCreatorTask
     */
    public OverlayCreatorTask setMergeBitmap(Bitmap mMergeBitmap) {
        this.mMergeBitmap = mMergeBitmap;
        return this;
    }

    /**
     * Sets the merging offset points. Invoking this method will mark the merging mechanism to overlay the mergeBitmap image to the base bitmap image
     * from the top left portion as specified by the params leftOffset and topOffset
     *
     * @param leftOffset pixel offsets from left
     * @param topOffset  pixel offsets from top
     * @return the related OverlayCreatorTask
     */
    public OverlayCreatorTask setOffsets(int leftOffset, int topOffset) {
        this.mTopOffset = topOffset;
        this.mLeftOffset = leftOffset;
        return this;
    }

    public interface OnMergeListener {
        void onMerge(OverlayCreatorTask task, Bitmap mergedBitmap);
    }

    /**
     * Sets the listener for overlay complete.
     *
     * @param listener for overlay completeness.
     * @return the related OverlayCreatorTask
     */
    public OverlayCreatorTask setMergeListener(OnMergeListener listener) {
        this.mMergeListener = listener;
        return this;
    }

    /**
     * Initiates the merging task in the background
     */
    public void overlay() {
        super.execute((Void[]) null);
    }


    private Bitmap overlayBitmapFromTopLeft() {
        return overlayBitmaps(mBaseBitmap, mMergeBitmap, mScale, mLeftOffset, mTopOffset, mAlpha);
    }

    private static Bitmap overlayBitmaps(Bitmap baseBitmap, Bitmap overlayBitmap, float scale, int leftOffset, int topOffset, int alpha) {

        if (scale > 0) {
            Bitmap overlayScaled = Bitmap.createScaledBitmap(overlayBitmap, (int) (overlayBitmap.getWidth() * scale), (int) (overlayBitmap.getHeight() * scale), true);

            Bitmap workingBitmap = Bitmap.createBitmap(baseBitmap);
            Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setAlpha(alpha);
            canvas.drawBitmap(overlayScaled, leftOffset, topOffset, paint);
            return mutableBitmap;
        } else {
            return baseBitmap;
        }

    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        if (mBaseBitmap == null) {
            throw new OverlayCreatorTaskException("Base bitmap not set");
        }

        if (mMergeBitmap == null) {
            throw new OverlayCreatorTaskException("overlay bitmap not set");
        }

        return overlayBitmapFromTopLeft();
    }

    public void onPostExecute(Bitmap bitmap) {
        if (mMergeListener != null) {
            mMergeListener.onMerge(this, bitmap);
        }
    }
}
