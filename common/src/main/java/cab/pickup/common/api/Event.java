package cab.pickup.common.api;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class Event {
    public static final int TYPE_USER_ADDED=1;
    public static final int TYPE_USER_PICKED=2;
    public static final int TYPE_USER_DROPPED=3;
    public static final int TYPE_DRIVER_ADDED=4;
    public static final int TYPE_DRIVER_ARRIVED=5;
    public static final int TYPE_USER_CANCELLED=6;

    public Date time;
    public int type;
    public Object data;

    public Event(int type, Object data, long time){
        this.type = type;
        this.data = data;
        this.time=new Date(time);
    }

    public Event(JSONObject json) throws JSONException {
        Log.d("JSONDATA",json.toString());
        type=json.getInt("type");
        time=new Date(json.getLong("time"));
        if(type==TYPE_DRIVER_ADDED){
            data=new Driver(json.getJSONObject("data"));
        }
        if(type==TYPE_DRIVER_ARRIVED){
            data=new Driver(json.getJSONObject("data"));
        }else {
            data=new User(json.getJSONObject("data"));
        }
    }

    public String getTitle() {
        switch (type){
            case TYPE_DRIVER_ADDED:
                return "Driver allocated to your journey";
            case TYPE_DRIVER_ARRIVED:
                return "Driver is about to reach you";
            case TYPE_USER_ADDED:
                return ((User)data).name + " added to your group";
            case TYPE_USER_PICKED:
                return ((User)data).name + " picked from his location";
            case TYPE_USER_DROPPED:
                return ((User)data).name + " dropped to his destination";
        }

        return "Not Init";
    }

    public String getTimeString(){
        long curr = System.currentTimeMillis();
        if(curr-time.getTime() < 60000){
            return "moments ago";
        }else if(curr-time.getTime() < 60*60000){
            return ((curr-time.getTime())/60000) + " minutes ago";
        }else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);

            return  hour+":"+minutes;
        }

    }

    @Override
    public String toString() {
        String json = "{\"type\" : "+type+",\"data\":"+data.toString()+",\"time\":"+time.getTime()+"}";

        return json;
    }
}
