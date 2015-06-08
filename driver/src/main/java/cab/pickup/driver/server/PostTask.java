package cab.pickup.driver.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.driver.IOUtil;


public class PostTask extends AsyncTask<String, Integer, Result> {
    private static final String TAG = "PostTask";
    OnServerTaskCompletedListener completedListener;

    List<NameValuePair> postData;
    private static final String baseUrl="http://pickup.prateekchandan.me/";
    private static final String apiKey="9f83c32cf3c9d529e";


    String url;

    public PostTask(OnServerTaskCompletedListener lstnr){
        completedListener=lstnr;
        postData=new ArrayList<>();

        addPostData("key",apiKey);
    }

    @Override
    protected Result doInBackground(String... params) {
        String url = params[0];
        Result res=new Result();

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpPost httppost = new HttpPost(url);

        try {
            List<NameValuePair> nameValuePairs=postData;

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            Log.d(TAG, res.statusCode + " : " + response.getStatusLine().getReasonPhrase());

            res = new Result(IOUtil.buildStringFromIS(response.getEntity().getContent()));
        } catch (IOException e) {
            onFail(e.getMessage());
        }

        httpclient.close();

        return res;
    }

    public void addPostData(String name, String value){
        postData.add(new BasicNameValuePair(name, value));
    }

    public void setUrl(String api){
        url=baseUrl+api;
    }

    public void onFail(String message){
        Log.e(TAG, "Task failed : "+message);
    }

    @Override
    public void onPostExecute(Result res) {
        if(res.statusCode!=200)
            Log.e(TAG, res.statusCode+" : "+res.statusMessage);

        if(completedListener!=null) completedListener.onServerTaskCompleted(res);
    }
}
