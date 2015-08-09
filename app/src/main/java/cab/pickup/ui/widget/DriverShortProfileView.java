package cab.pickup.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cab.pickup.R;
import cab.pickup.common.api.Driver;
import cab.pickup.ui.activity.MyActivity;

/**
 * Created by prateek on 8/8/15.
 */
public class DriverShortProfileView extends RelativeLayout {
    MyActivity mContext;
    ImageView mPicture;
    TextView mName,mModel,mVno;
    ImageView callDriver;

    Driver mDriver;

    public DriverShortProfileView(Context context) {
        super(context);

        init((MyActivity) context);
    }

    public DriverShortProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init((MyActivity) context);
    }

    private void init(MyActivity context){
        mContext = context;

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.widget_driver_short_view, this, true);

        mPicture = (ImageView) view.findViewById(R.id.user_profile_img);
        callDriver = (ImageView) view.findViewById(R.id.call_driver);
        mName = (TextView) view.findViewById(R.id.driver_name);
        mModel= (TextView) view.findViewById(R.id.car_model_name);
        mVno =  (TextView) view.findViewById(R.id.car_number);

        mName.setHint("Loading...");
    }

    public void setUser(Driver d){
        mDriver = d;
        loadProfile();
    }


    private void loadProfile(){
        //mPicture.setImageBitmap(mDriver.getImage(mContext));
        mName.setText(mDriver.driver_name);
        mModel.setText(mDriver.car_model);
        mVno.setText(mDriver.car_number);
        callDriver.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + mDriver.phone.trim() ;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                mContext.startActivity(intent);
            }
        });
    }

}
