package cab.pickup.driver.util;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.LocationListener;


/**
 * Created by prateek on 27/8/15.
 */
public class DistanceCalculator extends Service implements LocationListener{

    private final IBinder mBinder = new LocalBinder();
    int distance = 0;
    Location previousLocation=null;

    @Override
    public void onCreate() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(previousLocation==null){
            previousLocation=location;
        }else if(location!=null){
            distance+=previousLocation.distanceTo(location);
        }
    }

    public class LocalBinder extends Binder {
        public DistanceCalculator getService() {
            // Return this instance of LocalService so clients can call public methods
            return DistanceCalculator.this;
        }
    }

    public int getDistance(){
        return distance;
    }
}
