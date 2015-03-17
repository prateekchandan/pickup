package cab.pickup.util;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class User{
    public String id, fbid, device_id, name, email, gender,company,mobile,age,company_email;
    public LatLng position;


    public User(JSONObject user) throws JSONException{
        id=user.getString("id");
        fbid=user.getString("fbid");
        device_id=user.getString("device_id");
        name=user.getString("first_name");
        email=user.getString("email");
        gender=user.getString("gender");
        company="";
        mobile="";
        age="";
        company_email="";

    }

    public User(){

    }

    public String getJson(){
        String json="{";

        json+="\"id\":\""+id+"\",";
        json+="\"fbid\":\""+fbid+"\",";
        json+="\"device_id\":\""+device_id+"\",";
        json+="\"first_name\":\""+name+"\",";
        json+="\"email\":\""+email+"\",";
        json+="\"gender\":\""+gender+"\",";
        json+="\"comapany\":\""+company+"\",";
        json+="\"mobile\":\""+mobile+"\",";
        json+="\"age\":\""+age+"\",";
        json+="\"company_email\":\""+company_email+"\",";
        json+="}";

        return json;
    }

    public void setPosition(String position) {
        int comma = position.indexOf(',');
        this.position = new LatLng(Double.valueOf(position.substring(0,comma)), Double.valueOf(position.substring(comma+1)));
    }
}
