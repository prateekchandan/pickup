package cab.pickup.api;


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
}
