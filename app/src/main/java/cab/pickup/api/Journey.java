package cab.pickup.api;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cab.pickup.server.OnTaskCompletedListener;
import cab.pickup.server.PostTask;
import cab.pickup.server.Result;
import cab.pickup.ui.activity.MyActivity;
import cab.pickup.util.MapUtil;

// Wrapper class for Journey details json
public class Journey {
    public String id, distance, duration, cost;

    public Location start, end;

    public String user_id;
    public JSONObject group;
    public String datetime, del_time, cab_preference;


    public Journey(){
    }

    public Journey(JSONObject journey) throws JSONException{
        id=journey.getString("journey_id");
        datetime=journey.getString("journey_time");

        start= new Location(journey.getDouble("start_lat"), journey.getDouble("start_long"), journey.getString("start_text"));
        end=new Location(journey.getDouble("end_lat"),journey.getDouble("end_long"),journey.getString("end_text"));

        del_time=journey.getString("margin_before");
        cab_preference=journey.getString("preference");

        group=journey.getJSONObject("group");
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



    public void addToServer(MyActivity context, OnTaskCompletedListener listener){
        AddJourneyTask task = new AddJourneyTask(context,"Searching..");
        task.setOnTaskCompletedListener(listener);
        task.execute(context.getUrl("/add_journey"));
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

            journey.put("group", group);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*String json="{";

        json+="\"journey_id\":\""+id+"\",";
        json+="\"journey_time\":\""+datetime+"\",";

        json+="\"start_lat\":\""+start.latitude+"\",";
        json+="\"start_long\":\""+start.longitude+"\",";
        json+="\"start_text\":\""+start.longDescription+"\",";

        json+="\"end_lat\":\""+end.latitude+"\",";
        json+="\"end_long\":\""+end.longitude+"\",";
        json+="\"end_text\":\""+end.longDescription+"\",";

        json+="\"margin_before\":\""+del_time+"\",";
        json+="\"preference\":\""+cab_preference+"\"";

        json+="}";*/
        return journey.toString();
    }


    class AddJourneyTask extends PostTask {
        private static final String TAG = "AddJourneyTask";

        public AddJourneyTask(MyActivity context){
            super(context);
        }
        public AddJourneyTask(MyActivity context,String message){
            super(context);
            dialogMessage = message;
        }


        @Override
        public List<NameValuePair> getPostData(String[] params, int i) {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);

            nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
            nameValuePairs.add(new BasicNameValuePair("key", context.getKey()));

            nameValuePairs.add(new BasicNameValuePair("journey_id", id));
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

                Toast.makeText(context, ret.statusMessage, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(ret);
        }
    }
}
