package cab.pickup.api;

import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.server.GetTask;
import cab.pickup.server.OnTaskCompletedListener;
import cab.pickup.server.PostTask;
import cab.pickup.server.Result;
import cab.pickup.ui.activity.MainActivity;
import cab.pickup.ui.activity.MyActivity;

public class SingleJourney extends Journey{
    public User user;
    public String datetime, del_time, cab_preference;

    public SingleJourney(){
    }

    public SingleJourney(JSONObject journey) throws JSONException{
        id=journey.getString("journey_id");
        datetime=journey.getString("journey_time");

        start= new Location(journey.getDouble("start_lat"), journey.getDouble("start_long"), journey.getString("start_text"));
        end=new Location(journey.getDouble("end_lat"),journey.getDouble("end_long"),journey.getString("end_text"));

        del_time=journey.getString("margin_before");
        cab_preference=journey.getString("preference");
    }

    public SingleJourney(JSONObject path, User user) throws JSONException{
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

        this.user=user;
    }

    public SingleJourney(User user, Location start,Location end, String datetime, String del_time, String cab_preference){
        this.user=user;
        this.start=start;
        this.end=end;
        this.datetime=datetime;
        this.del_time=del_time;
        this.cab_preference=cab_preference;
    }

    public void addToServer(MyActivity context, OnTaskCompletedListener listener){
        AddJourneyTask task = new AddJourneyTask(context);
        task.setOnTaskCompletedListener(listener);
        task.execute(context.getUrl("/add_journey"));
    }

    @Override
    public String toString(){
        String json="{";

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
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);

            nameValuePairs.add(new BasicNameValuePair("user_id", user.id));
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
            nameValuePairs.add(new BasicNameValuePair("end_text",end.longDescription));

            return nameValuePairs;
        }

        @Override
        public void onPostExecute(Result ret){
            super.onPostExecute(ret);
            if(ret.statusCode==200){
                id = ret.data.optString("journey_id");

                Log.d(TAG, ret.statusMessage);

                Toast.makeText(context, ret.statusMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
