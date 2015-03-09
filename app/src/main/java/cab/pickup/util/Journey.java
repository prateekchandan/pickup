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
    public User u1, u2;
    public JSONObject path;
    public Address start, end;
    public String json, datetime, del_time, cab_preference;

    public Journey(JSONObject journey) throws JSONException {
        json=journey.toString();

        u1=new User(journey.getJSONObject("u1"));
        u2=new User(journey.optJSONObject("u2"));

        path=journey.getJSONObject("path");
    }

    public Journey(JSONObject path, User... users){
        this.path=path;

        u1=users[0];
        if(users.length>1)
            u2=users[1];
    }

    public Journey(User user, Address start, Address end, String datetime, String del_time, String cab_preference){
        u1=user;
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
        new AddJourneyTask(context).execute(context.getUrl("/add_journey"), u1.id, context.getKey()
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
}
