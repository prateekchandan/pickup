package cab.pickup.driver.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.widget.*;

import cab.pickup.common.api.User;
import cab.pickup.driver.R;
import cab.pickup.driver.api.Journey;
import cab.pickup.driver.ui.activity.MyActivity;

/**
 * Created by prateek on 16/8/15.
 */
public class UserShortCard extends CardView {
    User user;
    Journey journey;
    MyActivity mContext;
    Dialog dialog;

    public UserShortCard(Context context){
        super(context);
        mContext = (MyActivity)context;

        dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.user_dialogview);
        ((ImageView)dialog.findViewById(R.id.icon_close)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailedCard();
            }
        });
    }

    public UserShortCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (MyActivity)context;
    }

    public void setJourney(Journey j){
        journey=j;
        user  = j.user;
        LayoutInflater mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.user_shortview, this, true);

        ((ProfilePictureView)findViewById(R.id.user_pic)).setProfileId(user.fbid);
        ((TextView)findViewById(R.id.user_name)).setText(user.name);

        ((ProfilePictureView)dialog.findViewById(R.id.user_pic)).setProfileId(user.fbid);
        ((TextView)dialog.findViewById(R.id.user_name)).setText(user.name);
        ((TextView)dialog.findViewById(R.id.user_age)).setText(user.gender+" "+user.age);
        ((TextView)dialog.findViewById(R.id.start_location)).setText(j.start.longDescription);
        ((TextView)dialog.findViewById(R.id.end_location)).setText(j.end.longDescription);


    }

    public void showDetailedCard(){
        dialog.show();
    }

}
