package cab.pickup.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import cab.pickup.MyActivity;
import cab.pickup.util.IOUtil;

public abstract class PostTask extends AsyncTask<String, Integer, Result> {
    private static final String TAG = "PostTask";
    public MyActivity context;

    public PostTask(MyActivity context){
        this.context=context;
    }

    public PostTask(){}

    @Override
    protected Result doInBackground(String... params) {
        String url = params[0];
        Result res=new Result();

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpPost httppost = new HttpPost(url);

        try {
            List<NameValuePair> nameValuePairs = getPostData(params, 1);

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            res.statusCode = response.getStatusLine().getStatusCode();
            res.statusMessage=response.getStatusLine().getReasonPhrase();

            Log.d(TAG, res.statusCode + " : " + response.getStatusLine().getReasonPhrase());

            res.data=IOUtil.buildStringFromIS(response.getEntity().getContent());
        } catch (IOException e) {
            onFail(e.getMessage());
        }

        httpclient.close();

        return res;
    }

    public abstract List<NameValuePair> getPostData(String[] params, int i);

    public void onFail(String message){
        Log.e(TAG, "Task failed : "+message);
    }

    @Override
    public void onPostExecute(Result res) {
        if(res.statusCode !=200){
            String toast;
            try {
                JSONObject result = new JSONObject(res.data);

                toast=result.get("message").toString();
            } catch (JSONException e){
                toast=res.statusMessage;
            }

            Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
        }
    }
}
