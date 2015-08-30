package cab.pickup.common.util;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;


public class LocationProvider implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationProvider";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    FusedLocationProviderApi locApi = LocationServices.FusedLocationApi;
    GoogleApiClient apiClient;
    LocationRequest locRequest;
    Location location;
    long lastUpdateTime;

    public LocationProvider(Context context){
        Log.d(TAG,"Create called");
        apiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locRequest = new LocationRequest();
        locRequest.setInterval(INTERVAL);
        locRequest.setFastestInterval(FASTEST_INTERVAL);
        locRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void connect() {
        apiClient.connect();
    }

    public void disconnect() {
        apiClient.disconnect();
    }

    public Location getLastKnownLocation() {
        if (location == null) {
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
        if (apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    apiClient, this);
        }
        Log.d(TAG, "Location update stopped .......................");
    }

    public Location getLocation() {
        return location;
    }

    public double getLatitude() {
        return location.getLatitude();
    }

    public double getLongitude() {
        return location.getLongitude();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        lastUpdateTime = System.currentTimeMillis();

        Log.v(TAG, "Location: " + getLatitude() + "," + getLongitude() + ", Time: " + DateFormat.getTimeInstance().format(new Date()));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + apiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended: ");

        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());

        stopLocationUpdates();
    }
}
