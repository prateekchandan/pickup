package cab.pickup.util;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

import cab.pickup.ui.activity.MyActivity;

public class LocationTracker extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "LocationTracker";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    FusedLocationProviderApi locApi = LocationServices.FusedLocationApi;
    GoogleApiClient apiClient;
    LocationRequest locRequest;
    Location location;
    String lastUpdateTime;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"Create called");
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locRequest = new LocationRequest();
        locRequest.setInterval(INTERVAL);
        locRequest.setFastestInterval(FASTEST_INTERVAL);
        locRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public LocationTracker(){
    }

    public void connect(){
        apiClient.connect();
    }

    public void disconnect(){
        apiClient.disconnect();
    }

    public Location getLastKnownLocation(){
        if(location==null){
            location = locApi.getLastLocation(apiClient);
            return location;
        } else {
            return location;
        }
    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                apiClient, locRequest, this);
        Log.d(TAG, "Location update started .......................");
    }

    public void stopLocationUpdates() {
        if(apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    apiClient, this);
        }
        Log.d(TAG, "Location update stopped .......................");
    }

    public Location getLocation(){ return location; }

    public double getLatitude(){
        return location.getLatitude();
    }

    public double getLongitude(){
        return location.getLongitude();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        lastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        Log.d(TAG, "Location: " + getLatitude() + "," + getLongitude() + ", Time: " + lastUpdateTime);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + apiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended: ");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        connect();

        return START_STICKY;
    }

    @Override
    public void onDestroy(){

        stopLocationUpdates();
        disconnect();

        Toast.makeText(this, "Location tracker done", Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public LocationTracker getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationTracker.this;
        }
    }

}