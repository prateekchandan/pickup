package cab.pickup.driver;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Journey {
    public String datetime, del_time, cab_preference;
    public JSONObject path;
    public ArrayList<Location> navigation=new ArrayList<>();

    public Journey(){
    }

    public Journey(JSONObject path) throws JSONException{
        this.path=path;

        long dist=0, dur=0;

        JSONArray legs = ((JSONObject)(path.has("routes")?
                path.getJSONArray("routes").get(0):
                path)).getJSONArray("legs");

        for(int i=0; i<legs.length(); i++){
            dist+=legs.getJSONObject(i).getJSONObject("distance").getInt("value");
            dur+=legs.getJSONObject(i).getJSONObject("duration").getInt("value");
        }
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
                JSONObject step = steps.getJSONObject(j);
                String polyline = step.getJSONObject("polyline").getString("points");

                String instructions = step.getString("html_instructions");
                Location loc = new Location(step.getJSONObject("start_location").getDouble("lat"),
                                            step.getJSONObject("start_location").getDouble("lng"),
                                            instructions);

                navigation.add(loc);

                for (LatLng p : MapUtil.decodePolyline(polyline)) {
                    lines.add(p);
                }
            }
        }

        return lines;
    }
}
