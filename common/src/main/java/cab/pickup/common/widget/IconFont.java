package cab.pickup.common.widget;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by prateek on 21/9/15.
 */
public class IconFont  extends TextView {
    String fontPath = "icons.ttf";
    public IconFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
    }
    public IconFont(Context context, AttributeSet attrs) {
        super(context, attrs);


        try {
            super.setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
        }catch (Exception E){
            E.printStackTrace();
        }
    }
    public IconFont(Context context) {
        super(context);
        super.setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
    }
    public void setTypeface(Typeface tf, int style) {
        super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), fontPath));
    }
}