package cab.pickup.api;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.R;
import cab.pickup.server.GetTask;
import cab.pickup.server.OnTaskCompletedListener;
import cab.pickup.server.Result;

/**
 * Created by prateek on 6/8/15.
 */
public class Driver{
    public String driver_id, driver_name, driver_address, phone,car_model,car_number,picUrl;
    public LatLng position;
    Bitmap image;



    public Driver(JSONObject driver) throws JSONException {
        loadJSONdata(driver);
    }

    public Driver(String driver_id, OnTaskCompletedListener listener){
        GetTask getUserDetails=new GetTask(){
            @Override
            public void onPostExecute(Result res){
                if(res.statusCode==200){
                    try {
                        loadJSONdata(res.data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                super.onPostExecute(res);
            }
        };

        getUserDetails.setOnTaskCompletedListener(listener);
        getUserDetails.execute("http://pickup.prateekchandan.me/get_driver/" + driver_id + "?key=9f83c32cf3c9d529e");
    }

    public Driver(){

    }

    public void loadJSONdata(JSONObject user) throws JSONException{
        driver_id=user.optString("driver_id");
        driver_name=user.optString("driver_name");
        driver_address=user.optString("driver_address");
        phone =user.optString("phone");
        car_model=user.optString("car_model");
        car_number=user.optString("car_number");
    }

    public String getJson(){
        String json="{";

        json+="\"driver_id\":\""+driver_id+"\"";
        json+=",\"driver_name\":\""+driver_name+"\"";
        json+=",\"driver_address\":\""+driver_address+"\"";
        json+=",\"phone\":\""+phone+"\"";
        json+=",\"car_model\":\""+car_model+"\"";
        json+=",\"car_number\":\""+ car_number +"\"";
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

    public Bitmap getImage(Context context){
        if(image != null)
            return image;

        // TODO : Fetch Image from server if not present
        image = BitmapFactory.decodeResource(context.getResources(),R.drawable.car);

        return image;
    }
}
