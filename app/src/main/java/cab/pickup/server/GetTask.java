package cab.pickup.server;

import android.app.ProgressDialog;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

import cab.pickup.ui.activity.MyActivity;
import cab.pickup.util.IOUtil;

public class GetTask extends AsyncTask<String, Integer, Result> {
    private static final String TAG = "GetTask";
    public MyActivity context;
    public String url;
    private OnTaskCompletedListener listener;
    ProgressDialog dialog;
    protected String dialogMessage;

    public GetTask(){
        dialogMessage = "";
    }

    public GetTask(MyActivity context) {
        this.context=context;
        dialogMessage = "";
    }

    public GetTask(MyActivity context,String message) {
        this.context=context;
        dialogMessage = message;
    }

    @Override
    protected void onPreExecute(){
        if(!dialogMessage.equals(""))
            dialog = ProgressDialog.show(context, "", dialogMessage);
    }

    @Override
    protected Result doInBackground(String... params) {
        Result ret = new Result();

        if(url==null) url = params[0];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpGet httpget = new HttpGet(url);
        try {
            HttpResponse response = httpclient.execute(httpget);

            ret = new Result(IOUtil.buildStringFromIS(response.getEntity().getContent()));

            Log.d(TAG, ret.statusCode + " : " + url + " : "+ response.getStatusLine().getReasonPhrase());
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        httpclient.close();

        return ret;
    }

    @Override
    public void onPostExecute(Result res){
        if(!dialogMessage.equals(""))
            dialog.dismiss();

        if(res.statusCode !=200){
            if(context!=null) Toast.makeText(context, res.statusMessage, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error "+String.valueOf(res.statusCode
            )+": "+res.statusMessage);
        } else {
            if(listener!=null){
                Log.d(TAG, "Listener called");
                listener.onTaskCompleted(res);
            } else {
                Log.d(TAG, "Listener is null");
            }
        }
    }

    public void setOnTaskCompletedListener(OnTaskCompletedListener listener) {
        this.listener = listener;
    }
}