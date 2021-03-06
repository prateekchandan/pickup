package cab.pickup.ui.widget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cab.pickup.R;
import cab.pickup.common.api.Event;
import cab.pickup.ui.activity.MyActivity;

public class EventView extends LinearLayout{
    Event mEvent;

    TextView mTitle;
    TextView mTime;
    ImageView mIcon;
   // ProfilePictureView fbImage;

    MyActivity context;

    public EventView(Context context) {
        super(context);

        this.context = (MyActivity) context;
        init();
    }

    private void init() {
        LayoutInflater inflater= context.getLayoutInflater();
        LinearLayout eventView = (LinearLayout) inflater.inflate(R.layout.event_view, this, true);

        mTitle = ((TextView)eventView.findViewById(R.id.event_title));
        mIcon = ((ImageView)eventView.findViewById(R.id.event_icon));
        mTime = ((TextView)eventView.findViewById(R.id.event_time));
      //  fbImage = ((ProfilePictureView)findViewById(R.id.user_pic));
    }

    public void updateView(){
        mTitle.setText(mEvent.getTitle());
        Log.d("Time Event", mEvent.getTimeString());
        mTime.setText(mEvent.getTimeString());
        mIcon.setVisibility(VISIBLE);
        mIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.user));


    }

    public void setEvent(Event e){
        mEvent = e;

        updateView();
    }
}
