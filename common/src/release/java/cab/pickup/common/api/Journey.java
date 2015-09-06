package cab.pickup.common.api;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.common.Constants;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.PostTask;
import cab.pickup.common.server.Result;
import cab.pickup.common.util.MapUtil;
import cab.pickup.common.util.UserDatabaseHandler;

// Wrapper class for Journey details json
public class Journey {
    public String id, duration;

    public Location start, end;
    public Double distance=0.0,cost=0.0;
    public String user_id;
    public Group group;

    public String datetime, del_time, cab_preference;


    public Journey(){
    }

    public Journey(JSONObject journey, UserDatabaseHandler db) throws JSONException{
        id=journey.getString("journey_id");
        datetime=journey.getString("journey_time");

        start= new Location(journey.getDouble("start_lat"), journey.getDouble("start_long"), journey.getString("start_text"));
        end=new Location(journey.getDouble("end_lat"),journey.getDouble("end_long"),journey.getString("end_text"));

        del_time=journey.getString("margin_before");
        cab_preference=journey.getString("preference");

        if(journey.has("distance"))
            distance = journey.getDouble("distance");

        if(journey.has("cost"))
            cost  = journey.getDouble("cost");

        group = new Group(new JSONObject(journey.getString("group")),db);
    }

    public Journey(User user, Location start, Location end, String datetime, String del_time, String cab_preference){
        //this.user=user;
        this.start=start;
        this.end=end;
        this.datetime=datetime;
        this.del_time=del_time;
        this.cab_preference=cab_preference;
    }

    public static LatLngBounds getLatLngBounds(JSONObject path) throws JSONException{

        JSONObject bounds = ((JSONObject)(path.has("routes")?
                path.getJSONArray("routes").get(0):
                path)).getJSONObject("bounds");

        JSONObject ne = bounds.getJSONObject("northeast");
        JSONObject sw = bounds.getJSONObject("southwest");

        return new LatLngBounds(new LatLng(sw.getDouble("lat"), sw.getDouble("lng")),
                new LatLng(ne.getDouble("lat"), ne.getDouble("lng")));
    }

    public static ArrayList<LatLng> getPath(JSONObject path) throws JSONException {

        ArrayList<LatLng> lines = new ArrayList<LatLng>();

        JSONArray legs = ((JSONObject)(path.has("routes")?
                path.getJSONArray("routes").get(0):
                path)).getJSONArray("legs");
        Log.d("DISTANCECALC",legs.toString());
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

    public static Double getPathDistance(JSONObject path) throws JSONException {

        Double dist = 0.0;
        Long distC = 0l;

        JSONArray legs = ((JSONObject)(path.has("routes")?
                path.getJSONArray("routes").get(0):
                path)).getJSONArray("legs");

        for (int i = 0; i < legs.length(); i++) {
            JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");

            for(int j=0; j<steps.length(); j++) {
                distC += steps.getJSONObject(j).getJSONObject("distance").getLong("value");
            }
        }
        dist = distC/1000.0;
        DecimalFormat df = new DecimalFormat("0.0");
        return Double.parseDouble(df.format(dist));
    }



    public void addToServer(Context context, OnTaskCompletedListener listener){
        AddJourneyTask task = new AddJourneyTask(context,"Searching..");
        task.setOnTaskCompletedListener(listener);
        task.execute(Constants.getUrl("/add_journey"));
    }

    @Override
    public String toString(){
        JSONObject journey = new JSONObject();
        try {
            journey.put("journey_id",id);
            journey.put("journey_time",datetime);

            journey.put("start_lat",start.latitude);
            journey.put("start_long",start.longitude);
            journey.put("start_text",start.longDescription);

            journey.put("end_lat",end.latitude);
            journey.put("end_long",end.longitude);
            journey.put("end_text",end.longDescription);

            journey.put("margin_before", del_time);
            journey.put("preference", cab_preference);
            journey.put("distance", distance);
            journey.put("cost", cost);

            journey.put("group", group);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return journey.toString();
    }


    class AddJourneyTask extends PostTask {
        private static final String TAG = "AddJourneyTask";

        public AddJourneyTask(Context context){
            super(context);
        }
        public AddJourneyTask(Context context,String message){
            super(context);
            dialogMessage = message;
        }


        @Override
        public List<NameValuePair> getPostData(String[] params, int i) {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);

            nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
            nameValuePairs.add(new BasicNameValuePair("key", Constants.KEY));

            //nameValuePairs.add(new BasicNameValuePair("journey_id", id));
            nameValuePairs.add(new BasicNameValuePair("start_lat", start.latitude+""));
            nameValuePairs.add(new BasicNameValuePair("start_long", start.longitude+""));
            nameValuePairs.add(new BasicNameValuePair("end_lat", end.latitude+""));
            nameValuePairs.add(new BasicNameValuePair("end_long", end.longitude+""));

            nameValuePairs.add(new BasicNameValuePair("journey_time", datetime));
            nameValuePairs.add(new BasicNameValuePair("margin_before", del_time));
            nameValuePairs.add(new BasicNameValuePair("margin_after", del_time));
            nameValuePairs.add(new BasicNameValuePair("preference","1"));

            nameValuePairs.add(new BasicNameValuePair("start_text",start.longDescription));
            nameValuePairs.add(new BasicNameValuePair("end_text", end.longDescription));

            return nameValuePairs;
        }

        @Override
        public void onPostExecute(Result ret){
            if(ret.statusCode==200){
                id = ret.data.optString("journey_id");
                Log.d(TAG, ret.statusMessage);
            }
            super.onPostExecute(ret);
        }
    }
}
