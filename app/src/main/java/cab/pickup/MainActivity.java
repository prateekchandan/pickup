package cab.pickup;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

import cab.pickup.server.AddJourneyTask;
import cab.pickup.widget.LocationSearchBar;

public class MainActivity extends MyActivity {

    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent();
        i.setClass(this,LoginActivity.class);
        startActivityForResult(i, 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent();
            i.setClass(this, SettingsActivity.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int req, int res, Intent data){
        super.onActivityResult(req,res,data);

        Log.d(TAG, "onActivityResult");
        user_id=prefs.getString("user_id",null);
    }

    private TextView getListItem(Address a){
        TextView tv = new TextView(this);
        tv.setText(a.getFeatureName()+" ,"+a.getLocality()+" ,"+a.getLatitude()+","+a.getLongitude());
        tv.setTag(a);
        return tv;
    }

    public void addJourney(View v) {
        Address start = ((LocationSearchBar)findViewById(R.id.field_start)).getAddress();
        Address end = ((LocationSearchBar)findViewById(R.id.field_start)).getAddress();

        TimePicker journey_time = (TimePicker)findViewById(R.id.journey_time);
        String time = journey_time.getCurrentHour()+":"+journey_time.getCurrentMinute()+":00";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
        String currentDate = sdf.format(new Date());

        new AddJourneyTask().execute(getUrl("/add_journey"), user_id, getKey()
                ,start.getLatitude()+""
                ,start.getLongitude()+""
                ,end.getLatitude()+""
                ,end.getLongitude()+""
                ,currentDate+time
                ,"30"
                ,"30"
                ,"1");
    }

    public void openChat(View v){
        Intent i = new Intent();
        i.setClass(this, ChatActivity.class);

        startActivity(i);
    }

    private LatLng getLatLng(Address a){
        return new LatLng(a.getLatitude(),a.getLongitude());
    }
}
