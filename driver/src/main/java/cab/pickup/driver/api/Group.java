package cab.pickup.driver.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cab.pickup.common.api.Driver;
import cab.pickup.common.api.User;
import cab.pickup.common.util.UserDatabaseHandler;

/**
 * Created by prateek on 15/8/15.
 */
public class Group {
    public ArrayList<Journey> journeys = new ArrayList<>();
    public JSONObject json;
    public String group_id;

    public Group(){
    }

    public Group(JSONObject rep) throws JSONException {
        Log.d("GROUP", rep.toString());
        if(rep.has("group_id"))
            group_id = rep.getString("group_id");


        if(rep.has("journeys")) {
            JSONArray journey_array = rep.getJSONArray("journeys");
            for (int i = 0; i < journey_array.length(); i++) {
                journeys.add(new Journey(new JSONObject(journey_array.getString(i))));
            }
            this.json = rep.getJSONObject("json");
        }else if(rep.has("journey_details")){
            JSONArray journey_array = rep.getJSONArray("journey_details");
            for (int i = 0; i < journey_array.length(); i++) {
                journeys.add(new Journey(new JSONObject(journey_array.getString(i))));
            }
            this.json = rep;
        }



    }

    @Override
    public String toString(){
        JSONObject rep=new JSONObject();
        JSONArray journey_array = new JSONArray();


        try {
            for(Journey j : journeys)
                journey_array.put(new JSONObject(j.toString()));

            rep.put("journeys",journey_array);
            rep.put("json",json);
            rep.put("group_id",group_id);
        } catch (JSONException e) {
            e.printStackTrace();

        }

        return rep.toString();
    }
}

