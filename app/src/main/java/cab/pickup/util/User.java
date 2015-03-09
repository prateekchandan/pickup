package cab.pickup.util;

import org.json.JSONException;
import org.json.JSONObject;

public class User{
    public String id, fbid, device_id, name, email, gender;


    public User(JSONObject user) throws JSONException{
        id=user.getString("id");
        fbid=user.getString("fbid");
        device_id=user.getString("device_id");
        name=user.getString("name");
        email=user.getString("email");
        gender=user.getString("gender");
    }

    public User(){

    }

    public String getJson(){
        String json="{";

        json+="\"id\":\""+id+"\",";
        json+="\"fbid\":\""+fbid+"\",";
        json+="\"device_id\":\""+device_id+"\",";
        json+="\"name\":\""+name+"\",";
        json+="\"email\":\""+email+"\",";
        json+="\"gender\":\""+gender+"\"";

        json+="}";

        return json;
    }
}
