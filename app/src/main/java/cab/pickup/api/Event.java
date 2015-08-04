package cab.pickup.api;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cab.pickup.server.OnTaskCompletedListener;

public class Event {
    public static final int TYPE_USER_ADDED=1;
    public static final int TYPE_USER_PICKED=2;
    public static final int TYPE_USER_DROPPED=3;
    public static final int TYPE_DRIVER_ADDED=4;

    public Date time;
    public int type;
    public Object data;

    public Event(int type, Object data){
        this.type = type;
        this.data = data;
    }

    public Event(JSONObject json) throws JSONException {
        type=json.getInt("type");
        if(type==TYPE_DRIVER_ADDED){
            data=json.getString("data");
        } else {
            data=new User(json.getJSONObject("data"));
        }
    }

    public String getTitle() {
        switch (type){
            case TYPE_DRIVER_ADDED:
                return "Driver allocated to your journey";
            case TYPE_USER_ADDED:
                return ((User)data).name + " added to your group";
            case TYPE_USER_PICKED:
                return ((User)data).name + " picked from his location";
            case TYPE_USER_DROPPED:
                return ((User)data).name + " dropped to his destination";
        }

        return "Not Init";
    }

    @Override
    public String toString() {
        String json = "{\"type\" : "+type+",\"data\":"+data.toString()+"}";

        return json;
    }
}
