package cab.pickup.server;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Result {
    public int statusCode;
    public String statusMessage;
    public JSONObject data;

    public Result(String json){
        try{
            JSONObject res=new JSONObject(json);

            if(res.getInt("error")==0) {
                data=res;
                statusCode = 200;
                statusMessage=res.getString("message");
            } else {
                statusCode = 412;
                statusMessage=res.getString("message");
            }
        } catch (JSONException e) {
            statusCode=412;
            statusMessage="Bad reply from server!";
            Log.e("ResultError",e.getMessage());
        }
    }

    public Result(){
        statusCode=404;
        statusMessage="Cannot connect to server!";
    }
}
