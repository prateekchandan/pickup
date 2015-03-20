package cab.pickup;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cab.pickup.server.GetTask;
import cab.pickup.server.PostTask;
import cab.pickup.server.Result;
import cab.pickup.util.CommonJourney;
import cab.pickup.util.Journey;
import cab.pickup.util.User;


public class JourneyActivity extends MapsActivity {
    HashMap<String, Marker> markers = new HashMap<>();

    private static final String TAG = "JourneyActivity";
    LinearLayout profile_list;

    List<String> locations=new ArrayList<>();

    CommonJourney journey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        profile_list = (LinearLayout)findViewById(R.id.fb_profile_list);

        new FetchJourneyTask(this, getUrl("/journey/"+getIntent().getStringExtra("journey_id"))+"/"+me.id+"?key="+getKey()).execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        tracker.connect();
    }

    @Override
    public void onStop() {
        tracker.stopLocationUpdates();
        tracker.disconnect();
        super.onStop();
    }

    public void showProfile(User user){
        LinearLayout ll = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.user_profile, profile_list);
        ProfilePictureView pp = (ProfilePictureView)ll.findViewById(R.id.user_profile_img);
        pp.setProfileId(user.fbid);

        ((TextView)ll.findViewById(R.id.user_profile_name)).setText(user.name+"\n"+user.gender);
    }

    @Override
    public void onLocationUpdate(Location location){
            new UpdateLocationTask().execute(getUrl("/modify_location/"+getIntent().getStringExtra("journey_id")), location.getLatitude()+","+location.getLongitude());
    }

    public void saveLocation(String s) {
        locations.add(s+","+new Date().getTime());
    }

    class FetchJourneyTask extends GetTask {
        private static final String TAG = "FetchJourney";

        public FetchJourneyTask(MyActivity context, String url){
            super(context);
            this.url=url;
        }

        @Override
        public void onPostExecute(Result ret){
            super.onPostExecute(ret);
            if(ret.statusCode==200) {
                Log.d(TAG, ret.data);

                try {
                    journey = new CommonJourney(new JSONObject(ret.data));

                    for (User user : journey.users)
                        if (!user.id.equals(me.id))
                            showProfile(user);

                    addPath(journey.getPath(), journey.getLatLngBounds(), "Distance:" + journey.distance + ", Duration:" + journey.duration + ", Cost:" + journey.cost);

                    new UberDataTask(JourneyActivity.this,UberDataTask.DATA_PRICE).execute();
                    new UberDataTask(JourneyActivity.this,UberDataTask.DATA_TIME).execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class UberDataTask extends GetTask {
        public static final String TAG = "FetchJourney",
                                DATA_PRICE="price",
                                DATA_TIME="time";

        String data_type;

        public UberDataTask(MyActivity context, String data){
            super(context);
            this.url="https://api.uber.com/v1/estimates/"+data+"?"+
                        "start_latitude="+journey.start.getLatitude()+
                        "&start_longitude="+journey.start.getLongitude()+
                        "&end_latitude="+journey.end.getLatitude()+
                        "&end_longitude="+journey.end.getLongitude();

            data_type=data;
        }

        @Override
        public void onPostExecute(Result ret){
            super.onPostExecute(ret);
            if(ret.statusCode==200) {
                Log.d(TAG, ret.data);

                try {
                    JSONObject result = new JSONObject(ret.data);
                    JSONArray arr=result.getJSONArray(data_type+"s");

                    String display="";

                    for(int i=0; i<arr.length(); i++){
                        JSONObject elem = arr.getJSONObject(i);

                        display+=elem.getString("display_name")+":"+elem.getString("estimate")+",";
                    }

                    if(data_type.equals(DATA_PRICE)){
                        ((TextView)findViewById(R.id.uber_price_estimate)).setText(display);
                    } else if(data_type.equals(DATA_TIME)) {
                        ((TextView) findViewById(R.id.uber_time_estimate)).setText(display);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class UpdateLocationTask extends PostTask {

        @Override
        public List<NameValuePair> getPostData(String[] params, int i) {
            List<NameValuePair> data = new ArrayList<>();

            data.add(new BasicNameValuePair("user_id", me.id));
            data.add(new BasicNameValuePair("key", getKey()));
            data.add(new BasicNameValuePair("position", params[i++]));
            return data;
        }

        @Override
        public void onPostExecute(Result result){
            super.onPostExecute(result);
            if(result.statusCode==200) {
                try {
                    JSONObject positions = new JSONObject(result.data);

                    for (User user : journey.users) {
                        Log.d(TAG, "User: " + user.id);

                        user.setPosition(positions.getString(user.id));

                        if (!markers.containsKey(user.id)) {
                            markers.put(user.id, map.addMarker(new MarkerOptions().position(user.position)));
                        } else {
                            markers.get(user.id).setPosition(user.position);
                        }
                    }

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

}
