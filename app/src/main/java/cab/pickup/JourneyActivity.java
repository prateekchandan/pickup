package cab.pickup;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cab.pickup.util.IOUtil;
import cab.pickup.util.Journey;
import cab.pickup.util.User;


public class JourneyActivity extends MapsActivity {

    LinearLayout profile_list;

    List<String> locations=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        profile_list = (LinearLayout)findViewById(R.id.fb_profile_list);

        new FetchJourneyTask().execute(getUrl("/journey"), getKey(), getIntent().getStringExtra("journey_id"));
    }

    public void showProfile(User user){
        LinearLayout ll = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.user_profile, profile_list);
        ProfilePictureView pp = (ProfilePictureView)ll.findViewById(R.id.user_profile_img);
        pp.setProfileId(user.fbid);

        ((TextView)ll.findViewById(R.id.user_profile_name)).setText(user.name+"\n"+user.gender);
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
                Journey result = new Journey(new JSONObject(ret), Journey.TYPE_COMMON);

                for(User user : result.users)
                    if(!user.id.equals(me.id))
                        showProfile(user);

                addPath(result.getPath(), result.getLatLngBounds(), "Distance:"+result.distance+", Duration:"+result.duration+", Cost:"+result.cost);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
