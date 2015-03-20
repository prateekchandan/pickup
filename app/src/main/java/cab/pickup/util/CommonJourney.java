package cab.pickup.util;

import android.location.Address;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

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

        JSONArray legs = path.getJSONArray("legs");

        JSONObject start_leg = legs.getJSONObject(0), end_leg = legs.getJSONObject(legs.length()-1);

        start=new Address(Locale.getDefault());
        end=new Address(Locale.getDefault());

        start.setAddressLine(0, start_leg.getString("start_address"));
        start.setLatitude(start_leg.getJSONObject("start_location").getDouble("lat"));
        start.setLongitude(start_leg.getJSONObject("start_location").getDouble("lng"));

        end.setAddressLine(0, end_leg.getString("end_address"));
        end.setLatitude(end_leg.getJSONObject("end_location").getDouble("lat"));
        end.setLongitude(end_leg.getJSONObject("end_location").getDouble("lng"));
    }
}
