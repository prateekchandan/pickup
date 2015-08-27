package cab.pickup.driver.api;

import android.content.Intent;
import android.os.Binder;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.common.api.Location;
import cab.pickup.common.api.User;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.Result;
import cab.pickup.driver.MyApplication;
import cab.pickup.driver.util.DistanceCalculator;

/**
 * Created by prateek on 15/8/15.
 */// Wrapper class for Journey details json
public class Journey {
    public String id, duration;

    public Location start, end;
    public Double distance=0.0,cost=0.0;
    public User user;

    public String datetime, del_time, cab_preference;
    public Double distance_travelled=0.0;
    public int journey_started=0,journey_ended=0;
    public boolean onRide = false;

    public Journey(){
    }

    public Journey(JSONObject journey,OnTaskCompletedListener listener) throws JSONException {
        loadFromJSON(journey);
        if(journey.has("user"))
            user = new User(new JSONObject(journey.getString("user")), MyApplication.getDB());
        else if(journey.has("id"))
            user = new User(journey.getString("id"),listener,MyApplication.getDB());

    }

    public Journey(JSONObject journey) throws JSONException {
        loadFromJSON(journey);
        if(journey.has("user"))
            user = new User(new JSONObject(journey.getString("user")), MyApplication.getDB());
        else if(journey.has("id"))
            user = new User(journey.getString("id"),null,MyApplication.getDB());

    }

    private void loadFromJSON(JSONObject journey) throws JSONException{
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

        if(journey.has("journey_ended"))
            journey_ended = journey.getInt("journey_ended");

        if(journey.has("journey_started"))
            journey_started = journey.getInt("journey_started");

        if(journey.has("distance_travelled"))
            distance_travelled = journey.getDouble("distance_travelled");

        if(journey.has("onRide"))
            onRide=journey.getBoolean("onRide");

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
            journey.put("journey_ended", journey_ended);
            journey.put("journey_started", journey_started);
            journey.put("distance_travelled ", distance_travelled );
            journey.put("onRide", onRide );
            journey.put("user", user.toString() );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return journey.toString();
    }


}

