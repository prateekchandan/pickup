package cab.pickup.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

import cab.pickup.R;
import cab.pickup.api.User;
import cab.pickup.server.OnTaskCompletedListener;
import cab.pickup.server.Result;
import cab.pickup.ui.activity.MyActivity;

public class UserProfileView extends RelativeLayout implements OnTaskCompletedListener{
    MyActivity mContext;
    ProfilePictureView mPicture;
    TextView mName,mAge;

    User mUser;

    public UserProfileView(Context context) {
        super(context);

        init((MyActivity) context);
    }

    public UserProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init((MyActivity) context);
    }

    private void init(MyActivity context){
        mContext = context;

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.widget_user_profile, this, true);

        mPicture = (ProfilePictureView) view.findViewById(R.id.user_profile_img);
        mName = (TextView) view.findViewById(R.id.user_profile_name);
        mAge = (TextView) view.findViewById(R.id.user_age_sex);

        mName.setHint("Loading...");
    }

    public void setUserId(String id){
        mUser=new User(id,this);
    }

    @Override
    public void onTaskCompleted(Result res) {

            mPicture.setProfileId(mUser.fbid);
            mName.setText(mUser.name);
            String s =String.valueOf(mUser.age);
            if(mUser.gender.equals("male"))
                s+= " M";
            else if(mUser.gender.equals("female"))
                s+= " F";
            mAge.setText(s);
            Log.d("UserProfileView", "Loaded : " + mUser.name);
    }
}
