package cab.pickup.ui.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import cab.pickup.R;
import cab.pickup.api.User;
import cab.pickup.ui.activity.MyActivity;
import cab.pickup.ui.activity.ProfileActivity;
import cab.pickup.ui.activity.SettingsActivity;
import cab.pickup.util.Helper;

/**
 * Created by prateek on 3/8/15.
 */
public class SideBarMenu extends RelativeLayout {

    MyActivity mContext;
    View menu;
    User mUser;

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
        mUser=mContext.me;

        ImageView userImg = ((ImageView) findViewById(R.id.user_photo));
                ((TextView) findViewById(R.id.user_name)).setText(mUser.name);
        ((TextView)findViewById(R.id.user_email)).setText(mUser.email);
        Helper.setFBImage(mUser.fbid, mContext,userImg);

        setOnCLicks();
    }

    private void setOnCLicks(){
        menu.findViewById(R.id.menu_profile).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext,ProfileActivity.class));
            }
        });

        menu.findViewById(R.id.profile_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext,ProfileActivity.class));
            }
        });

        menu.findViewById(R.id.menu_share).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Download pickup from this URL";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pickup App");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                mContext.startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        menu.findViewById(R.id.menu_settings).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext,SettingsActivity.class));
            }
        });

        menu.findViewById(R.id.menu_rate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    mContext.startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + mContext.getPackageName())));
                }
            }
        });

        menu.findViewById(R.id.menu_history).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        menu.findViewById(R.id.menu_payment).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"Payment to be updated soon!",Toast.LENGTH_SHORT).show();
            }
        });
    }


}
