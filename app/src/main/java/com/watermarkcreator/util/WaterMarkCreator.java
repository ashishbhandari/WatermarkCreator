package com.watermarkcreator.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

/**
 * Created by b_ashish on 29-Jun-16.
 */

public class WaterMarkCreator extends BaseWaterMarkAsyncTask {

    public static final int MARGIN_RIGHT = 20;
    private CharSequence mText;
    private int mTextColor;
    private int mTextSize;
    private int mTextStrokeWidth;
    private boolean mTextAntiAlias;
    private int mTextBgColor;
    private int mTextMaxWidth;


    private Typeface mTextTypeFace;
    private Paint.Align mTextAlign;

    private WaterMarkCreator(WaterMarkBuilder waterMarkBuilder) {

        this.mText = waterMarkBuilder.mText;
        this.mTextColor = waterMarkBuilder.mTextColor;
        this.mTextSize = waterMarkBuilder.mTextSize;
        this.mTextStrokeWidth = waterMarkBuilder.mTextStrokeWidth;
        this.mTextAntiAlias = waterMarkBuilder.mTextAntiAlias;
        this.mTextBgColor = waterMarkBuilder.mTextBgColor;
        this.mTextMaxWidth = waterMarkBuilder.mTextMaxWidth;

    }

    public interface OnDecodeListener {

        void onDecode(WaterMarkCreator task, Bitmap output);
    }

    private OnDecodeListener mListener;


    class WaterMarkCreatorException extends RuntimeException {
        public WaterMarkCreatorException(String msg) {
            super(msg);
        }
    }

    public static WaterMarkBuilder createBuilder() {
        return new WaterMarkBuilder();
    }

    /**
     * Set the listener for listening to the background task of decoding and sampling the image.
     *
     * @param listener - listener for decode complete.
     * @return the related WaterMarkCreator
     */
    public WaterMarkCreator setListener(OnDecodeListener listener) {
        this.mListener = listener;
        return this;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        return convertToBitmapFromText();
    }

    @Override
    public void onPostExecute(Bitmap bitmap) {
        if (mListener != null) {
            mListener.onDecode(this, bitmap);
        }
    }

    /**
     * Initiates the background process to decoding and sampling the image.
     */
    public void execute() {
        super.execute((Void[]) null);
    }


    public Bitmap convertToBitmapFromText() {
        if (mTextSize == 0.0F) {
            throw new WaterMarkCreatorException("Did not provide the text size");
        }
        if (mTextColor == 0) {
            throw new WaterMarkCreatorException("Did not provide the text color");
        }

        TextPaint paint = new TextPaint();
        paint.setColor(mTextColor);
        paint.setTextSize(mTextSize);
        paint.setStrokeWidth(5);
        paint.setTypeface(Typeface.MONOSPACE);

        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.LEFT);

        //ascent : The recommended distance above the baseline for singled spaced text
        float baseline = (int) (-paint.ascent() + 3f); // ascent() is negative
        Log.e("test", " " + paint.ascent() + " baseline: " + baseline);

        // First decode with Rect to check dimensions
        Rect bounds = new Rect();
        paint.getTextBounds(mText.toString(), 0, mText.length(), bounds);

        int boundWidth = bounds.width() + MARGIN_RIGHT;
        // mRequestWidth must be in pixels
        if (boundWidth > mTextMaxWidth) {
            boundWidth = mTextMaxWidth;
        }
        StaticLayout staticLayout = new StaticLayout(mText, 0, mText.length(),
                paint, mTextMaxWidth, android.text.Layout.Alignment.ALIGN_NORMAL, 1.0f,
                1.0f, false);

        int lineCount = staticLayout.getLineCount();
        //Descent: The recommended distance below the baseline for singled spaced text
        int height = (int) (baseline + paint.descent() + 3) * lineCount + 10;

        Bitmap image = Bitmap.createBitmap(boundWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF);
        staticLayout.draw(canvas);
        return image;
    }

    protected CharSequence getText() {
        return mText;
    }

    protected int getTextColor() {
        return mTextColor;
    }

    protected int getTextSize() {
        return mTextSize;
    }

    public static class WaterMarkBuilder extends BaseWaterMarkBuilder {

        private CharSequence mText;
        private int mTextColor;
        private int mTextSize;
        private int mTextStrokeWidth;
        private boolean mTextAntiAlias;
        private int mTextMaxWidth;

        private Typeface mTextTypeFace;
        private Paint.Align mTextAlign;

        private int mTextBgColor;

        public WaterMarkBuilder() {

        }

        public WaterMarkBuilder setText(CharSequence text) {
            mText = text;
            return this;
        }

        public WaterMarkBuilder setTextColor(int textColor) {
            mTextColor = textColor;
            return this;
        }

        public WaterMarkBuilder setTextSize(int textSize) {
            mTextSize = textSize;
            return this;
        }

        public WaterMarkBuilder setTextStrokeWidth(int textStrokeWidth) {
            mTextStrokeWidth = textStrokeWidth;
            return this;
        }

        public WaterMarkBuilder setTextTypeFace(Typeface textTypeFace) {
            mTextTypeFace = textTypeFace;
            return this;
        }

        public WaterMarkBuilder setTextAntiAlias(boolean textAntiAlias) {
            mTextAntiAlias = textAntiAlias;
            return this;
        }

        public WaterMarkBuilder setTextAlign(Paint.Align textAlign) {
            mTextAlign = textAlign;
            return this;
        }

        public WaterMarkBuilder setTextBackgroundColor(int textBgColor) {
            mTextBgColor = textBgColor;
            return this;
        }

        public WaterMarkBuilder setTextMaxWidth(int textMaxWidth) {
            mTextMaxWidth = textMaxWidth;
            return this;
        }

        @Override
        protected BaseWaterMarkBuilder self() {
            return this;
        }

        public WaterMarkCreator build() {
            return new WaterMarkCreator(this);
        }
    }

}
