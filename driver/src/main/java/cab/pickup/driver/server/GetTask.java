package cab.pickup.driver.server;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import cab.pickup.driver.ui.activity.MyActivity;
import cab.pickup.driver.util.IOUtil;

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

        URL getUrl;
        URLConnection urlConnection;
        InputStream in;
        try {
            getUrl = new URL(url);
            urlConnection = getUrl.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
        }catch (Exception E){
            Log.d(TAG,E.getMessage());
            return ret;
        }


        try {
            ret = new Result(IOUtil.buildStringFromIS(in));

            Log.d(TAG, ret.statusCode + " : " + url);
            in.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
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