package cab.pickup.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by prateek on 14/8/15.
 */public class AdjustableLinearLayout extends LinearLayout {

    public AdjustableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdjustableLinearLayout(Context context) {
        super(context);
    }

    private OnSoftKeyboardListener onSoftKeyboardListener;

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (onSoftKeyboardListener != null) {
            final int newSpec = MeasureSpec.getSize(heightMeasureSpec);
            final int oldSpec = getMeasuredHeight();
            if (oldSpec > newSpec){
                onSoftKeyboardListener.onShown();
            } else {
                onSoftKeyboardListener.onHidden();
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public final void setOnSoftKeyboardListener(final OnSoftKeyboardListener listener) {
        this.onSoftKeyboardListener = listener;
    }

    public interface OnSoftKeyboardListener {
        public void onShown();
        public void onHidden();
    }

}