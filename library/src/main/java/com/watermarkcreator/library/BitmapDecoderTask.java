package com.watermarkcreator.library;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by b_ashish on 29-Jun-16.
 */
public class BitmapDecoderTask extends AsyncTask<Void, Void, Bitmap> {

    enum DecodingOptions {
        DECODE_FROM_RESOURCE,
        DECODE_FROM_DISK,
        DECODING_NOT_SPECIFIED;
    }

    public interface OnDecodeListener {

        void onDecode(BitmapDecoderTask task, Bitmap output);
    }

    private int mReqHeight, mReqWidth;
    private OnDecodeListener mListener;
    private Uri mContentProviderUri; // if getting bitmap from external storage or from disk
    private ContentResolver mResolver;
    private int mDrawableId; // if getting bitmap from drawable.
    private Resources mResources;
    private DecodingOptions mDecodingOptions = DecodingOptions.DECODING_NOT_SPECIFIED;


    public BitmapDecoderTask setListener(OnDecodeListener listener) {
        this.mListener = listener;
        return this;
    }

    public BitmapDecoderTask setDecodingImageReference(int drawableId, Resources resources) {
        this.mDrawableId = drawableId;
        this.mResources = resources;
        this.mDecodingOptions = DecodingOptions.DECODE_FROM_RESOURCE;
        return this;
    }

    public BitmapDecoderTask setDecodingImageReference(Uri contentProviderUri, ContentResolver mResolver) {
        this.mResolver = mResolver;
        this.mContentProviderUri = contentProviderUri;
        this.mDecodingOptions = DecodingOptions.DECODE_FROM_DISK;
        return this;
    }


    public BitmapDecoderTask setRequiredWidth(int width) {
        this.mReqWidth = width;
        return this;
    }

    public BitmapDecoderTask setRequiredHeight(int height) {
        this.mReqHeight = height;
        return this;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        switch (mDecodingOptions) {
            case DECODE_FROM_DISK:
                return decodeSampledBitmapFromDisk();
            case DECODE_FROM_RESOURCE:
                return decodeSampledBitmapFromResource();
            default:
                throw new OverlayCreatorTaskException("Did not specify the image reference with setDecodingTextReference()");
        }
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
    public void decode() {
        super.execute((Void[]) null);
    }

    private Bitmap decodeSampledBitmapFromDisk() {

        try {

            if (mResolver == null || mContentProviderUri == null) {
                throw new OverlayCreatorTaskException("Did not provide the uri reference or resolver");
            }

            if (mReqWidth <= 0) {
                throw new OverlayCreatorTaskException("Did not provide a valid required width. Should be > 0");
            }

            if (mReqHeight <= 0) {
                throw new OverlayCreatorTaskException("Did not provide a valid required height. Should be > 0");
            }

            InputStream sampleStream = mResolver.openInputStream(mContentProviderUri);
            InputStream samplingStream = mResolver.openInputStream(mContentProviderUri);


            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(sampleStream, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, mReqWidth, mReqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(samplingStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new OverlayCreatorTaskException("Did not provide a valid Uri.");

        }
    }


    private Bitmap decodeSampledBitmapFromResource() {

        if (mResources == null) {
            throw new OverlayCreatorTaskException("Did not provide the resources");
        }

        if (mReqWidth <= 0) {
            throw new OverlayCreatorTaskException("Did not provide a valid required width. Should be > 0");
        }

        if (mReqHeight <= 0) {
            throw new OverlayCreatorTaskException("Did not provide a valid required height. Should be > 0");
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mResources, mDrawableId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, mReqWidth, mReqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(mResources, mDrawableId, options);
    }


    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
