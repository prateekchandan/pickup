package cab.pickup.common.api;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.Result;
import cab.pickup.common.util.UserDatabaseHandler;

public class User{
    public String id, fbid, device_id, name, email, gender,company, phone,age,company_email;
    public LatLng position;
    public Location home, office;


    public User(JSONObject user) throws JSONException{
        loadJSONdata(user, false);
    }

    public User(JSONObject user, final UserDatabaseHandler db) throws JSONException{
        loadJSONdata(user,false);
        db.addUser(User.this);
        Log.d("UserLoaded","user loaded in database "+name);
    }

    public User(String id, final OnTaskCompletedListener listener, final UserDatabaseHandler db){
        User temp = null;
        if(db!=null)
            temp = db.findUser(id);

        if(temp!=null){
            JSONObject data=null;
            try {
                data = new JSONObject(temp.toString());
                loadJSONdata(data,false);
            }catch (Exception E){
                E.printStackTrace();
            }

            final Result res = new Result();
            res.statusCode=200;
            res.statusMessage="User Details!!";
            res.data=data;

            //This async task is to make sure that the call to constructor is not stopped
            new AsyncTask<String,Integer,Result>(){
                @Override
                protected Result doInBackground(String... params) {
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException E){
                        E.printStackTrace();
                    }
                    return res;
                }
                @Override
                public void onPostExecute(Result res){
                    if(listener!=null)
                            listener.onTaskCompleted(res);
                }
            }.execute("");
        }else{
            GetTask getUserDetails=new GetTask(){
                @Override
                public void onPostExecute(Result res){
                    if(res.statusCode==200){
                        try {
                            loadJSONdata(res.data, false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    super.onPostExecute(res);
                    db.addUser(User.this);
                }
            };

            getUserDetails.setOnTaskCompletedListener(listener);
            getUserDetails.execute("http://pickup.prateekchandan.me/user/" + id + "?key=9f83c32cf3c9d529e");
        }
    }

    public User(){

    }

    public void loadJSONdata(JSONObject user, boolean hasAddress) throws JSONException{
        id=user.optString("id");
        fbid=user.optString("fbid");
        device_id=user.optString("device_id");
        name=user.optString("first_name");
        email=user.optString("email");
        gender=user.optString("gender");
        company=user.optString("company");
        phone =user.optString("phone");
        age=user.optString("age");
        company_email=user.optString("company_email");

        if(hasAddress){
            home = new Location(user.getJSONObject("home").getDouble("lat"),
                    user.getJSONObject("home").getDouble("lng"),
                    user.getJSONObject("home").getString("text"));
            office = new Location(user.getJSONObject("office").getDouble("lat"),
                    user.getJSONObject("office").getDouble("lng"),
                    user.getJSONObject("office").getString("text"));
        }
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
        json+=",\"phone\":\""+ phone +"\"";
        json+=",\"age\":\""+age+"\"";
        json+=",\"company_email\":\""+company_email+"\"";
        if(home!=null) json+=",\"home\":{\"lat\":"+home.latitude+",\"lng\":"+home.longitude+",\"text\":\""+home.shortDescription+"\"}";
        if(office!=null) json+=",\"office\":{\"lat\":"+office.latitude+",\"lng\":"+office.longitude+",\"text\":\""+office.shortDescription+"\"}";
        json+="}";

        return json;
    }

    public String toString(){
        return getJson();
    }

    public void setPosition(String position) {
        int comma = position.indexOf(',');
        this.position = new LatLng(Double.valueOf(position.substring(0,comma)), Double.valueOf(position.substring(comma+1)));
    }
}
