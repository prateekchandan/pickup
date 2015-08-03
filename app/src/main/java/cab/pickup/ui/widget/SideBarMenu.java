package cab.pickup.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import cab.pickup.R;
import cab.pickup.ui.activity.MyActivity;
import cab.pickup.ui.activity.ProfileActivity;

/**
 * Created by prateek on 3/8/15.
 */
public class SideBarMenu extends RelativeLayout {

    MyActivity mContext;
    View menu;

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
        init((MyActivity) context);
    }

    private  void init(MyActivity context){
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mContext = context;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        menu = mInflater.inflate(R.layout.side_menu, this, true);
        setOnCLicks();
    }

    private void setOnCLicks(){
        menu.findViewById(R.id.menu_profile).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext,ProfileActivity.class));
            }
        });
    }


}
