package cab.pickup.common.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cab.pickup.common.util.UserDatabaseHandler;

public class Group {
    public ArrayList<User> mates = new ArrayList<>();
    public JSONObject json;
    public Driver driver;
    public String group_id;

    public Group(){
    }

    public Group(JSONObject rep, UserDatabaseHandler db) throws JSONException{
        Log.d("GROUP", rep.toString());
        if(rep.has("group_id"))
            group_id = rep.getString("group_id");

        if(rep.has("mates")) {
            JSONArray users = rep.getJSONArray("mates");
            for (int i = 0; i < users.length(); i++) {
                mates.add(new User(users.getJSONObject(i)));
            }
            try {
                driver = new Driver(rep.getJSONObject("driver"));
            }
            catch (Exception E){
                Log.w("Group",E.getMessage()+" @ driver load");
            }
            this.json = rep.getJSONObject("json");
        } else {
            JSONArray mate_array = rep.getJSONArray("users_list");
            for (int i = 0; i < mate_array.length(); i++) {
                mates.add(new User(mate_array.getString(i),null,db));
            }

            this.json = rep;
        }
    }

    @Override
    public String toString(){
        JSONObject rep=new JSONObject();
        JSONArray mate_array = new JSONArray();


        try {
            for(User u : mates)
                mate_array.put(new JSONObject(u.toString()));

            rep.put("mates",mate_array);
            rep.put("json",json);
            rep.put("group_id",group_id);
            if(driver!=null)
                rep.put("driver", new JSONObject(driver.toString()));
        } catch (JSONException e) {
            e.printStackTrace();

        }

        return rep.toString();
    }
}
