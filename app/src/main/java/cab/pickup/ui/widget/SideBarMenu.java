package cab.pickup.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import cab.pickup.R;
import cab.pickup.ui.activity.MyActivity;

/**
 * Created by prateek on 3/8/15.
 */
public class SideBarMenu extends RelativeLayout {

    MyActivity mContext;

    public SideBarMenu(Context context){
        super(context);
        init((MyActivity)context);
    }

    public SideBarMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init((MyActivity)context);
    }

    public SideBarMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init((MyActivity)context);
    }

    private  void init(MyActivity context){
        if(mContext==null) {
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mContext = context;
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = mInflater.inflate(R.layout.side_menu, this, true);

        }
    }
}
