package cab.pickup.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommonJourney extends Journey {
    public ArrayList<User> users=new ArrayList<>();

    public CommonJourney(JSONObject journey) throws JSONException {
        JSONArray usrs = journey.getJSONArray("users");
            for(int i=0; i<usrs.length(); i++)
                users.add(new User(usrs.getJSONObject(i), false));

            distance =journey.getString("new_distance");
            duration =journey.getString("new_time");
            cost =journey.getString("new_cost");

            path=journey.getJSONObject("path");

        JSONArray legs = path.getJSONArray("legs");

        JSONObject start_leg = legs.getJSONObject(0), end_leg = legs.getJSONObject(legs.length()-1);

        start = new Location(start_leg.getJSONObject("start_location").getDouble("lat"),
                start_leg.getJSONObject("start_location").getDouble("lng"),
                start_leg.getString("start_address"));

        end = new Location(end_leg.getJSONObject("end_location").getDouble("lat"),
                end_leg.getJSONObject("end_location").getDouble("lng"),
                end_leg.getString("end_address"));
    }
}
