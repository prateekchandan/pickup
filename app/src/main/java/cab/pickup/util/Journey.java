package cab.pickup.util;

import android.location.Address;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.MyActivity;
import cab.pickup.server.PostTask;

// Wrapper class for Journey details json
public class Journey {
    public static final int TYPE_COMMON=0, TYPE_SINGLE=1;

    public ArrayList<User> users=new ArrayList<>();
    public JSONObject path;
    public Address start, end;
    public String id, datetime, del_time, cab_preference, distance, duration, cost;

    public Journey(){}

    public Journey(JSONObject journey, int type) throws JSONException {
        if(type==TYPE_COMMON){
            JSONArray usrs = journey.getJSONArray("users");
            for(int i=0; i<usrs.length(); i++)
                users.add(new User(usrs.getJSONObject(i)));

            distance =journey.getString("new_distance");
            duration =journey.getString("new_time");
            cost =journey.getString("new_cost");

            path=journey.getJSONObject("path");
        } else if(type==TYPE_SINGLE){
            id=journey.getString("journey_id");
            datetime=journey.getString("journey_time");

            start=MapUtil.addressFrom(journey.getDouble("start_lat"),journey.getDouble("start_long"),journey.getString("start_text"));
            end=MapUtil.addressFrom(journey.getDouble("end_lat"),journey.getDouble("end_long"),journey.getString("end_text"));

            del_time=journey.getString("margin_before");
            cab_preference=journey.getString("preference");
        }

    }

    public Journey(JSONObject path, User user) throws JSONException{
        this.path=path;

        long dist=0, dur=0;

        JSONArray legs = ((JSONObject)(path.has("routes")?
                path.getJSONArray("routes").get(0):
                path)).getJSONArray("legs");

        for(int i=0; i<legs.length(); i++){
            dist+=legs.getJSONObject(i).getJSONObject("distance").getInt("value");
            dur+=legs.getJSONObject(i).getJSONObject("duration").getInt("value");
        }

        distance=String.valueOf((double)dist/1000)+" kms";
        duration=String.valueOf((double)dur/60)+" mins";
        cost="0";

        users.clear();
        users.add(user);
    }

    public Journey(User user, Address start, Address end, String datetime, String del_time, String cab_preference){
        users.clear();users.add(user);
        this.start=start;
        this.end=end;
        this.datetime=datetime;
        this.del_time=del_time;
        this.cab_preference=cab_preference;
    }

    public LatLngBounds getLatLngBounds() throws JSONException{

        JSONObject bounds = ((JSONObject)(path.has("routes")?
                path.getJSONArray("routes").get(0):
                path)).getJSONObject("bounds");

        JSONObject ne = bounds.getJSONObject("northeast");
        JSONObject sw = bounds.getJSONObject("southwest");

        return new LatLngBounds(new LatLng(sw.getDouble("lat"), sw.getDouble("lng")),
                new LatLng(ne.getDouble("lat"), ne.getDouble("lng")));
    }

    public ArrayList<LatLng> getPath() throws JSONException {

        ArrayList<LatLng> lines = new ArrayList<LatLng>();

        JSONArray legs = ((JSONObject)(path.has("routes")?
                path.getJSONArray("routes").get(0):
                path)).getJSONArray("legs");

        for (int i = 0; i < legs.length(); i++) {
            JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");
            for(int j=0; j<steps.length(); j++) {
                String polyline = steps.getJSONObject(j).getJSONObject("polyline").getString("points");

                for (LatLng p : MapUtil.decodePolyline(polyline)) {
                    lines.add(p);
                }
            }
        }

        return lines;
    }

    public void addToServer(MyActivity context){
        new AddJourneyTask(context).execute(context.getUrl("/add_journey"));
    }

    public String getJson(){
        String json="{";

        json+="\"journey_id\":\""+id+"\",";
        json+="\"journey_time\":\""+datetime+"\",";

        json+="\"start_lat\":\""+start.getLatitude()+"\",";
        json+="\"start_long\":\""+start.getLongitude()+"\",";
        json+="\"start_text\":\""+MapUtil.stringFromAddress(start)+"\",";

        json+="\"end_lat\":\""+end.getLatitude()+"\",";
        json+="\"end_long\":\""+end.getLongitude()+"\",";
        json+="\"end_text\":\""+MapUtil.stringFromAddress(end)+"\",";

        json+="\"margin_before\":\""+del_time+"\",";
        json+="\"preference\":\""+cab_preference+"\"";

        json+="}";
        return json;
    }

    class AddJourneyTask extends PostTask {
        private static final String TAG = "AddJourneyTask";

        public AddJourneyTask(MyActivity context){
            super(context);
        }

        @Override
        public List<NameValuePair> getPostData(String[] params, int i) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("user_id", users.get(0).id));
            nameValuePairs.add(new BasicNameValuePair("key", context.getKey()));

            nameValuePairs.add(new BasicNameValuePair("journey_id", id));

            nameValuePairs.add(new BasicNameValuePair("start_lat", start.getLatitude()+""));
            nameValuePairs.add(new BasicNameValuePair("start_long", start.getLongitude()+""));
            nameValuePairs.add(new BasicNameValuePair("end_lat", end.getLatitude()+""));
            nameValuePairs.add(new BasicNameValuePair("end_long", end.getLongitude()+""));

            nameValuePairs.add(new BasicNameValuePair("journey_time", datetime));
            nameValuePairs.add(new BasicNameValuePair("margin_before", del_time));
            nameValuePairs.add(new BasicNameValuePair("margin_after", del_time));
            nameValuePairs.add(new BasicNameValuePair("preference","1"));

            nameValuePairs.add(new BasicNameValuePair("start_text",MapUtil.stringFromAddress(start)));
            nameValuePairs.add(new BasicNameValuePair("end_text",MapUtil.stringFromAddress(end)));

            return nameValuePairs;
        }

        @Override
        protected void onPostExecute(String ret){
            String toast="";
            if(ret == null){
                toast="There was an error in adding journey";
            } else {
                try {
                    JSONObject result = new JSONObject(ret);
                    toast = result.get("message").toString();

                    id = result.getString("journey_id");

                    Log.d(TAG, toast);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
        }
    }
}
