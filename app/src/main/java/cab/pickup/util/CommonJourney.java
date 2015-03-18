package cab.pickup.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommonJourney extends Journey {
    public ArrayList<User> users=new ArrayList<>();

    public CommonJourney(JSONObject journey) throws JSONException {
        JSONArray usrs = journey.getJSONArray("users");
            for(int i=0; i<usrs.length(); i++)
                users.add(new User(usrs.getJSONObject(i)));

            distance =journey.getString("new_distance");
            duration =journey.getString("new_time");
            cost =journey.getString("new_cost");

            path=journey.getJSONObject("path");
    }
}
