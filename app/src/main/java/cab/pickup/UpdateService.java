package cab.pickup;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.common.Constants;
import cab.pickup.common.api.User;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.Result;
import cab.pickup.common.util.LocationProvider;

public class UpdateService extends Service {
    private static final long UPDATE_TIME = 300000;

    private LocalBinder mBinder = new LocalBinder();

    private LocationProvider mLocationProvider;

    private SharedPreferences prefs;
    private User me;

    private Handler mUpdateHandler = new Handler();
    private Runnable mUpdateTask = new Runnable() {
        @Override
        public void run() {
            Log.d("UpdateTask","UpdateTask running at time :"+ System.currentTimeMillis());

            new GetTask(){
                @Override
                public void onPostExecute(Result res) {
                    super.onPostExecute(res);
                    if(res.statusCode==200){
                        // TODO add event data to prefs

                        Log.d("UpdateTask","Add event data to prefs");
                        mUpdateHandler.postDelayed(mUpdateTask, UPDATE_TIME);
                    }
                }
            }.execute(Constants.getUrl("periodic_route/"+me.id));
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationProvider = new LocationProvider(this);

        prefs = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        try {
            me = new User(new JSONObject(prefs.getString("user_json","")));
            Log.d("UpdateService", me.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
            me=new User();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public UpdateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return UpdateService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(this, "Location tracker done", Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }

    public void startEventUpdates(){
        stopEventUpdates();
        mUpdateHandler.post(mUpdateTask);
    }

    public void stopEventUpdates(){
        mUpdateHandler.removeCallbacks(mUpdateTask);
    }
}
