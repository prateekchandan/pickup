package cab.pickup.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.R;
import cab.pickup.api.User;
import cab.pickup.gcm.GcmIntentService;


public class RideActivity extends MyActivity{
    ListView user_list_view;
    UserListAdapter user_adapter;
    BroadcastReceiver mUpdateReceiver;

    ArrayList<User> user_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        user_list_view=(ListView)findViewById(R.id.summary_list_user);
        user_adapter=new UserListAdapter(this);
        user_list_view.setAdapter(user_adapter);

        mUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NotificationManager mNotificationManager = (NotificationManager)
                        RideActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.cancel(intent.getIntExtra("notif_id",0));

                if(intent.getAction().equals(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG)){
                    Toast.makeText(RideActivity.this, "User added : "+intent.getStringExtra("id"), Toast.LENGTH_LONG).show();

                    user_adapter.add(new User(intent.getStringExtra("id")));

                } else if(intent.getAction().equals(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG)){
                    Toast.makeText(RideActivity.this, "Driver added : "+intent.getStringExtra("id"), Toast.LENGTH_LONG).show();
                }
            }
        };

        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG));
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mUpdateReceiver);

        super.onDestroy();
    }

    class UserListAdapter extends ArrayAdapter<User> {
        List<User> users = new ArrayList<>();
        Context context;

        public UserListAdapter(Context context, List<User> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);

            this.context=context;
            users = objects;
        }

        public UserListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);

            this.context=context;
        }

        @Override
        public void add(User u){
            users.add(u);
        }

        @Override
        public void clear(){
            users.clear();
        }

        @Override
        public int getCount(){
            return users.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout user_layout = (RelativeLayout)inflater.inflate(R.layout.user_profile, parent, false);

            ((ProfilePictureView)user_layout.findViewById(R.id.user_profile_img)).setProfileId(users.get(position).fbid);
            ((TextView)user_layout.findViewById(R.id.user_profile_name)).setText(users.get(position).name);

            return user_layout;
        }
    }
}
