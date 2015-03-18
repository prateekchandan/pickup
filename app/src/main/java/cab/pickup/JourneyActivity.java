package cab.pickup;

import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cab.pickup.server.PostTask;
import cab.pickup.server.Result;
import cab.pickup.util.CommonJourney;
import cab.pickup.util.IOUtil;
import cab.pickup.util.Journey;
import cab.pickup.util.LocationTracker;
import cab.pickup.util.User;


public class JourneyActivity extends MapsActivity {
    HashMap<String, Marker> markers = new HashMap<String, Marker>();

    private static final String TAG = "JourneyActivity";
    LinearLayout profile_list;

    List<String> locations=new ArrayList<>();

    CommonJourney journey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        profile_list = (LinearLayout)findViewById(R.id.fb_profile_list);

        new FetchJourneyTask().execute(getUrl("/journey"), getKey(), getIntent().getStringExtra("journey_id"));
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

    class FetchJourneyTask extends AsyncTask<String, Integer, String> {
        private static final String TAG = "FetchJourney";

        @Override
        protected String doInBackground(String... params) {
            String ret_value=null;

            String url = params[0],
                    access_key = params[1];

            AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
            url+="/"+params[2]+"/"+me.id+"?key="+access_key;
            HttpGet httpget= new HttpGet(url);
            int statusCode = 0;

            try {
                HttpResponse response = httpclient.execute(httpget);
                statusCode = response.getStatusLine().getStatusCode();

                Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());

                if(statusCode==200){
                    ret_value = IOUtil.buildStringFromIS(response.getEntity().getContent());
                } else {
                    Log.e(TAG, "url : "+url+"\n"+IOUtil.buildStringFromIS(response.getEntity().getContent()));
                }
            } catch (ClientProtocolException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            httpclient.close();

            return ret_value;
        }

        @Override
        protected void onPostExecute(String ret){
            if(ret==null) {
                Toast.makeText(getApplicationContext(), "Error while getting path from server!", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, ret);

            try {
                journey = new CommonJourney(new JSONObject(ret));

                for(User user : journey.users)
                    if(!user.id.equals(me.id))
                        showProfile(user);

                addPath(journey.getPath(), journey.getLatLngBounds(), "Distance:"+journey.distance+", Duration:"+journey.duration+", Cost:"+journey.cost);
            } catch (JSONException e) {
                e.printStackTrace();
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
