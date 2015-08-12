//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cab.pickup.driver.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.facebook.FacebookException;
import com.facebook.LoggingBehavior;
import com.facebook.android.R.dimen;
import com.facebook.android.R.drawable;
import com.facebook.android.R.styleable;
import com.facebook.internal.ImageDownloader;
import com.facebook.internal.ImageRequest;
import com.facebook.internal.ImageRequest.Builder;
import com.facebook.internal.ImageRequest.Callback;
import com.facebook.internal.ImageResponse;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;

import java.net.URISyntaxException;

public class ProfilePictureView extends FrameLayout {
    public static final String TAG = ProfilePictureView.class.getSimpleName();
    public static final int CUSTOM = -1;
    public static final int SMALL = -2;
    public static final int NORMAL = -3;
    public static final int LARGE = -4;
    private static final int MIN_SIZE = 1;
    private static final boolean IS_CROPPED_DEFAULT_VALUE = true;
    private static final String SUPER_STATE_KEY = "ProfilePictureView_superState";
    private static final String PROFILE_ID_KEY = "ProfilePictureView_profileId";
    private static final String PRESET_SIZE_KEY = "ProfilePictureView_presetSize";
    private static final String IS_CROPPED_KEY = "ProfilePictureView_isCropped";
    private static final String BITMAP_KEY = "ProfilePictureView_bitmap";
    private static final String BITMAP_WIDTH_KEY = "ProfilePictureView_width";
    private static final String BITMAP_HEIGHT_KEY = "ProfilePictureView_height";
    private static final String PENDING_REFRESH_KEY = "ProfilePictureView_refresh";
    private String profileId;
    private int queryHeight = 0;
    private int queryWidth = 0;
    private boolean isCropped = true;
    private Bitmap imageContents;
    private ImageView image;
    private int presetSizeType = -1;
    private ImageRequest lastRequest;
    private ProfilePictureView.OnErrorListener onErrorListener;
    private Bitmap customizedDefaultProfilePicture = null;

    public ProfilePictureView(Context context) {
        super(context);
        this.initialize(context);
    }

    public ProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(context);
        this.parseAttributes(attrs);
    }

    public ProfilePictureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initialize(context);
        this.parseAttributes(attrs);
    }

    public final int getPresetSize() {
        return this.presetSizeType;
    }

    public final void setPresetSize(int sizeType) {
        switch(sizeType) {
            case -4:
            case -3:
            case -2:
            case -1:
                this.presetSizeType = sizeType;
                this.requestLayout();
                return;
            default:
                throw new IllegalArgumentException("Must use a predefined preset size");
        }
    }

    public final boolean isCropped() {
        return this.isCropped;
    }

    public final void setCropped(boolean showCroppedVersion) {
        this.isCropped = showCroppedVersion;
        this.refreshImage(false);
    }

    public final String getProfileId() {
        return this.profileId;
    }

    public final void setProfileId(String profileId) {
        boolean force = false;
        if(Utility.isNullOrEmpty(this.profileId) || !this.profileId.equalsIgnoreCase(profileId)) {
            this.setBlankProfilePicture();
            force = true;
        }

        this.profileId = profileId;
        this.refreshImage(force);
    }

    public final ProfilePictureView.OnErrorListener getOnErrorListener() {
        return this.onErrorListener;
    }

    public final void setOnErrorListener(ProfilePictureView.OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public final void setDefaultProfilePicture(Bitmap inputBitmap) {
        this.customizedDefaultProfilePicture = inputBitmap;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        android.widget.LinearLayout.LayoutParams params =  (android.widget.LinearLayout.LayoutParams)this.getLayoutParams();
        boolean customMeasure = false;
        int newHeight = MeasureSpec.getSize(heightMeasureSpec);
        int newWidth = MeasureSpec.getSize(widthMeasureSpec);
        if(MeasureSpec.getMode(heightMeasureSpec) != 1073741824 && params.height == -2) {
            newHeight = this.getPresetSizeInPixels(true);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, 1073741824);
            customMeasure = true;
        }

        if(MeasureSpec.getMode(widthMeasureSpec) != 1073741824 && params.width == -2) {
            newWidth = this.getPresetSizeInPixels(true);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(newWidth, 1073741824);
            customMeasure = true;
        }

        if(customMeasure) {
            this.setMeasuredDimension(newWidth, newHeight);
            this.measureChildren(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.refreshImage(false);
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle instanceState = new Bundle();
        instanceState.putParcelable("ProfilePictureView_superState", superState);
        instanceState.putString("ProfilePictureView_profileId", this.profileId);
        instanceState.putInt("ProfilePictureView_presetSize", this.presetSizeType);
        instanceState.putBoolean("ProfilePictureView_isCropped", this.isCropped);
        instanceState.putParcelable("ProfilePictureView_bitmap", this.imageContents);
        instanceState.putInt("ProfilePictureView_width", this.queryWidth);
        instanceState.putInt("ProfilePictureView_height", this.queryHeight);
        instanceState.putBoolean("ProfilePictureView_refresh", this.lastRequest != null);
        return instanceState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if(state.getClass() != Bundle.class) {
            super.onRestoreInstanceState(state);
        } else {
            Bundle instanceState = (Bundle)state;
            super.onRestoreInstanceState(instanceState.getParcelable("ProfilePictureView_superState"));
            this.profileId = instanceState.getString("ProfilePictureView_profileId");
            this.presetSizeType = instanceState.getInt("ProfilePictureView_presetSize");
            this.isCropped = instanceState.getBoolean("ProfilePictureView_isCropped");
            this.queryWidth = instanceState.getInt("ProfilePictureView_width");
            this.queryHeight = instanceState.getInt("ProfilePictureView_height");
            this.setImageBitmap((Bitmap)instanceState.getParcelable("ProfilePictureView_bitmap"));
            if(instanceState.getBoolean("ProfilePictureView_refresh")) {
                this.refreshImage(true);
            }
        }

    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.lastRequest = null;
    }

    private void initialize(Context context) {
        this.removeAllViews();
        this.image = new ImageView(context);
        LayoutParams imageLayout = new LayoutParams(-1, -1);
        this.image.setLayoutParams(imageLayout);
        this.image.setScaleType(ScaleType.CENTER_INSIDE);
        this.addView(this.image);
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = this.getContext().obtainStyledAttributes(attrs, styleable.com_facebook_profile_picture_view);
        this.setPresetSize(a.getInt(0, -1));
        this.isCropped = a.getBoolean(1, true);
        a.recycle();
    }

    private void refreshImage(boolean force) {
        boolean changed = this.updateImageQueryParameters();
        if(this.profileId == null || this.profileId.length() == 0 || this.queryWidth == 0 && this.queryHeight == 0) {
            this.setBlankProfilePicture();
        } else if(changed || force) {
            this.sendImageRequest(true);
        }

    }

    private void setBlankProfilePicture() {
        if(this.customizedDefaultProfilePicture == null) {
            int scaledBitmap = this.isCropped()?drawable.com_facebook_profile_picture_blank_square:drawable.com_facebook_profile_picture_blank_portrait;
            this.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), scaledBitmap));
        } else {
            this.updateImageQueryParameters();
            Bitmap scaledBitmap1 = Bitmap.createScaledBitmap(this.customizedDefaultProfilePicture, this.queryWidth, this.queryHeight, false);
            this.setImageBitmap(scaledBitmap1);
        }

    }

    private void setImageBitmap(Bitmap imageBitmap) {
        if(imageBitmap!=null){
            imageBitmap=getRoundedBitmap(imageBitmap);
        }
        if(this.image != null && imageBitmap != null) {
            this.imageContents = imageBitmap;
            this.image.setImageBitmap(imageBitmap);
        }

    }

    private void sendImageRequest(boolean allowCachedResponse) {
        try {
            Builder e = new Builder(this.getContext(), ImageRequest.getProfilePictureUrl(this.profileId, this.queryWidth, this.queryHeight));
            ImageRequest request = e.setAllowCachedRedirects(allowCachedResponse).setCallerTag(this).setCallback(new Callback() {
                public void onCompleted(ImageResponse response) {
                    ProfilePictureView.this.processResponse(response);
                }
            }).build();
            if(this.lastRequest != null) {
                ImageDownloader.cancelRequest(this.lastRequest);
            }

            this.lastRequest = request;
            ImageDownloader.downloadAsync(request);
        } catch (URISyntaxException var4) {
            Logger.log(LoggingBehavior.REQUESTS, 6, TAG, var4.toString());
        }

    }

    private void processResponse(ImageResponse response) {
        if(response.getRequest() == this.lastRequest) {
            this.lastRequest = null;
            Bitmap responseImage = response.getBitmap();
            Exception error = response.getError();
            if(error != null) {
                ProfilePictureView.OnErrorListener listener = this.onErrorListener;
                if(listener != null) {
                    listener.onError(new FacebookException("Error in downloading profile picture for profileId: " + this.getProfileId(), error));
                } else {
                    Logger.log(LoggingBehavior.REQUESTS, 6, TAG, error.toString());
                }
            } else if(responseImage != null) {
                this.setImageBitmap(responseImage);
                if(response.isCachedRedirect()) {
                    this.sendImageRequest(false);
                }
            }
        }

    }

    private boolean updateImageQueryParameters() {
        int newHeightPx = this.getHeight();
        int newWidthPx = this.getWidth();
        if(newWidthPx >= 1 && newHeightPx >= 1) {
            int presetSize = this.getPresetSizeInPixels(false);
            if(presetSize != 0) {
                newWidthPx = presetSize;
                newHeightPx = presetSize;
            }

            if(newWidthPx <= newHeightPx) {
                newHeightPx = this.isCropped()?newWidthPx:0;
            } else {
                newWidthPx = this.isCropped()?newHeightPx:0;
            }

            boolean changed = newWidthPx != this.queryWidth || newHeightPx != this.queryHeight;
            this.queryWidth = newWidthPx;
            this.queryHeight = newHeightPx;
            return changed;
        } else {
            return false;
        }
    }

    private int getPresetSizeInPixels(boolean forcePreset) {
        int dimensionId;
        switch(this.presetSizeType) {
            case -4:
                dimensionId = dimen.com_facebook_profilepictureview_preset_size_large;
                break;
            case -3:
                dimensionId = dimen.com_facebook_profilepictureview_preset_size_normal;
                break;
            case -2:
                dimensionId = dimen.com_facebook_profilepictureview_preset_size_small;
                break;
            case -1:
                if(!forcePreset) {
                    return 0;
                }

                dimensionId = dimen.com_facebook_profilepictureview_preset_size_normal;
                break;
            default:
                return 0;
        }

        return this.getResources().getDimensionPixelSize(dimensionId);
    }

    public interface OnErrorListener {
        void onError(FacebookException var1);
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
