package cab.pickup.server;


import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class SendMessageTask extends PostTask {
    private static final String TAG = "SendMessageTask";

    public SendMessageTask(){
        super(null);
    }

    @Override
    public List<NameValuePair> getPostData(String[] params, int i) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("message", params[1]));
        nameValuePairs.add(new BasicNameValuePair("device_id", params[2]));
        nameValuePairs.add(new BasicNameValuePair("key", params[3]));

        return nameValuePairs;
    }

    @Override
    public void onPostExecute(Result ret){
        Log.d(TAG, "Send response : " + ret.data);
    }
}
