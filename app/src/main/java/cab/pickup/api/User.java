package cab.pickup.api;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class User{
    public String id, fbid, device_id, name, email, gender,company,mobile,age,company_email;
    public LatLng position;
    public LatLng home, office;


    public User(JSONObject user, boolean hasAddress) throws JSONException{
        id=user.optString("id");
        fbid=user.optString("fbid");
        device_id=user.optString("device_id");
        name=user.optString("first_name");
        email=user.optString("email");
        gender=user.optString("gender");
        company=user.optString("company");
        mobile=user.optString("mobile");
        age=user.optString("age");
        company_email=user.optString("company_email");

        if(hasAddress){
            home = new LatLng(user.getJSONObject("home").getDouble("lat"),user.getJSONObject("home").getDouble("lng"));
            office = new LatLng(user.getJSONObject("office").getDouble("lat"),user.getJSONObject("office").getDouble("lng"));
        }
    }

    public User(){

    }

    public String getJson(){
        String json="{";

        json+="\"id\":\""+id+"\"";
        json+=",\"fbid\":\""+fbid+"\"";
        json+=",\"device_id\":\""+device_id+"\"";
        json+=",\"first_name\":\""+name+"\"";
        json+=",\"email\":\""+email+"\"";
        json+=",\"gender\":\""+gender+"\"";
        json+=",\"company\":\""+company+"\"";
        json+=",\"mobile\":\""+mobile+"\"";
        json+=",\"age\":\""+age+"\"";
        json+=",\"company_email\":\""+company_email+"\"";
        if(home!=null) json+=",\"home\":{\"lat\":"+home.latitude+",\"lng\":"+home.longitude+"}";
        if(office!=null) json+=",\"office\":{\"lat\":"+office.latitude+",\"lng\":"+office.longitude+"}";
        json+="}";

        return json;
    }

    public void setPosition(String position) {
        int comma = position.indexOf(',');
        this.position = new LatLng(Double.valueOf(position.substring(0,comma)), Double.valueOf(position.substring(comma+1)));
    }
}