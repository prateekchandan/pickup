package cab.pickup.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Group {
    public ArrayList<User> mates = new ArrayList<>();
    public JSONObject json;

    public Group(){
    }

    public Group(JSONObject rep) throws JSONException{
        if(rep.has("mates")) {
            JSONArray users = rep.getJSONArray("users_list");
            for (int i = 0; i < users.length(); i++) {
                mates.add(new User(users.getString(i), null));
            }
        } else {
            JSONArray mate_array = rep.getJSONArray("mates");
            for (int i = 0; i < mate_array.length(); i++) {
                mates.add(new User(mate_array.getJSONObject(i)));
            }

            this.json = rep.getJSONObject("json");
        }
    }

    @Override
    public String toString(){
        JSONObject rep=new JSONObject();
        JSONArray mate_array = new JSONArray();

        for(User u : mates) mate_array.put(u);

        try {
            rep.put("mates",mate_array);
            rep.put("json",json);
        } catch (JSONException e) {
            e.printStackTrace();

        }

        return rep.toString();
    }
}
