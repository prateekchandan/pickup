package cab.pickup.util;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cab.pickup.MyActivity;
import cab.pickup.server.AddJourneyTask;

// Wrapper class for Journey details json
public class Journey {
    public static final int TYPE_COMMON=0, TYPE_SINGLE=1;

    public ArrayList<User> users=new ArrayList<>();
    public JSONObject path;
    public Address start, end;
    public String id, datetime, del_time, cab_preference;

    public Journey(){}

    public Journey(JSONObject journey, int type) throws JSONException {
        if(type==TYPE_COMMON){
            JSONArray usrs = journey.getJSONArray("users");
            for(int i=0; i<usrs.length(); i++)
                users.add(new User(usrs.getJSONObject(i)));

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

    public Journey(JSONObject path, User user){
        this.path=path;

        users.clear();
        users.add(user);
        //if(users.length>1)
        //    u2=users[1];
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
        new AddJourneyTask(context, this).execute(context.getUrl("/add_journey"), users.get(0).id, context.getKey()
                ,id
                ,start.getLatitude()+""
                ,start.getLongitude()+""
                ,end.getLatitude()+""
                ,end.getLongitude()+""
                ,datetime
                ,del_time
                ,del_time
                ,"1"
                ,MapUtil.stringFromAddress(start)
                ,MapUtil.stringFromAddress(end));
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
}
