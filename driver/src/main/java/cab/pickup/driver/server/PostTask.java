package cab.pickup.driver.server;

import android.app.ProgressDialog;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.util.List;

import cab.pickup.driver.ui.activity.MyActivity;
import cab.pickup.driver.util.IOUtil;

/**
 * Created by prateek on 4/8/15.
 */
public abstract class PostTask extends AsyncTask<String, Integer, Result> {
    private static final String TAG = "PostTask";
    public MyActivity context;
    private OnTaskCompletedListener listener;
    ProgressDialog dialog;
    protected String dialogMessage;

    public PostTask(){
        dialogMessage = "";
    }

    public PostTask(MyActivity context) {
        this.context=context;
        dialogMessage = "";
    }

    public PostTask(MyActivity context,String message) {
        this.context=context;
        dialogMessage = message;
    }

    @Override
    protected void onPreExecute(){
        if(!dialogMessage.equals(""))
            dialog = ProgressDialog.show(context,"",dialogMessage);
    }

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

            res = new Result(IOUtil.buildStringFromIS(response.getEntity().getContent()));

            Log.d(TAG, res.statusCode + " : " + response.getStatusLine().getReasonPhrase());

        } catch (IOException e) {
            onFail(e.getMessage());
        }

        httpclient.close();

        return res;
    }

    public abstract List<NameValuePair> getPostData(String[] params, int i);

    public void onFail(String message){
        Log.e(TAG, "Task failed : " + message);
        if(!dialogMessage.equals(""))
            dialog.dismiss();
    }

    @Override
    public void onPostExecute(Result res) {
        if(!dialogMessage.equals(""))
            dialog.dismiss();
        if(res.statusCode !=200){
            Toast.makeText(context, res.statusMessage, Toast.LENGTH_LONG).show();
        } else {
            if(listener!=null){
                listener.onTaskCompleted(res);
            }
        }
    }

    public void setOnTaskCompletedListener(OnTaskCompletedListener listener) {
        this.listener = listener;
    }
}
