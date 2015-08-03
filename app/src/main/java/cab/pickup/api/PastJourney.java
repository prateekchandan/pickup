package cab.pickup.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by prateek on 4/8/15.
 */
// Wrapper class for journey in history
public class PastJourney {
    public String status,start_text,end_text;
    public float distance;
    public int fare;
    public float start_lat,start_long,end_lat,end_long;
    public String time;
    public ArrayList<UserTemp> users;

    public PastJourney(JSONObject data){
        try {
            status = data.getString("status");
            start_text = data.getString("start_text");
            end_text = data.getString("end_text");
            distance = Float.parseFloat(data.getString("distance"));
            fare = data.getInt("fare");
            start_lat = Float.parseFloat(data.getString("start_lat"));
            start_long = Float.parseFloat(data.getString("start_long"));
            end_lat = Float.parseFloat(data.getString("end_lat"));
            end_long = Float.parseFloat(data.getString("end_long"));
            time = data.getString("journey_time");
            JSONArray usersArr = data.getJSONArray("mates");
            for (int i = 0;i<usersArr.length();i++){
                users.add(new UserTemp(usersArr.getJSONObject(i)));
            }
        }catch (Exception E){
            E.printStackTrace();
        }
    }


}

class UserTemp{
    public int user_id;
    public String fbid,name,gender,age;
    public UserTemp(JSONObject user){
        try {
            user_id = user.getInt("user_id");
            fbid = user.getString("fbid");
            name = user.getString("user_name");
            age = user.getString("age");
            gender = user.getString("gender");
        }catch (Exception E){
            E.printStackTrace();
        }
    }
}