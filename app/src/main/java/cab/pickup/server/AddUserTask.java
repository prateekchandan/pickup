package cab.pickup.server;

import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.LoginActivity;
import cab.pickup.MyActivity;

public class AddUserTask extends ServerTask{
    private static final String TAG = "AddUserTask";

    GoogleCloudMessaging gcm;

    String SENDER_ID = "1032273645702",
            gcm_id;

    public AddUserTask(MyActivity context){
        super(context);
    }

    @Override
    protected String doInBackground(String... params) {
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
        }
        try {
            gcm_id = gcm.register(SENDER_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return super.doInBackground(params);
    }

    @Override
    public List<NameValuePair> getPostData(String[] params, int i) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("user_id", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("key", params[i++]));

        nameValuePairs.add(new BasicNameValuePair("gcm_id", gcm_id));

        nameValuePairs.add(new BasicNameValuePair("device_id", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("fbid", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("name", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("email", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("gender", params[i++]));

        return nameValuePairs;
    }


    @Override
    protected void onPostExecute(String ret){
        if(ret==null){
           ret="Server registration failed! Check your internet connection!";
        } else

        try {
            JSONObject result = new JSONObject(ret);

            ret = result.get("message").toString();

            ((LoginActivity) context).addDataToPrefs(result.get("user_id").toString(), gcm_id);
        } catch (JSONException e){
            e.printStackTrace();
        }

        Toast.makeText(context, ret, Toast.LENGTH_LONG).show();

        context.finish();
    }
}
