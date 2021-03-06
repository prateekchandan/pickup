package cab.pickup.driver.ui.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cab.pickup.common.Constants;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.PostTask;
import cab.pickup.common.server.Result;
import cab.pickup.driver.R;
import cab.pickup.driver.api.Group;
import cab.pickup.driver.api.Journey;
import cab.pickup.driver.gcm.GcmIntentService;
import cab.pickup.driver.ui.widget.UserShortCard;

public class RideActivity extends MapsActivity implements LocationListener {

    private boolean rideEnded=false;
    Button nextEventBtn;
    TextView addressText;
    ArrayList<UserShortCard> userCards = new ArrayList<>();
    Location previousLocation = null;
    LocationManager locationManager;


    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM", "update Reciever called");

            NotificationManager mNotificationManager = (NotificationManager)
                    RideActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(intent.getIntExtra("notif_id", 0));
            createScreen();
        }
    };

    BroadcastReceiver mCancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM", "Driver allocated to new person");

            NotificationManager mNotificationManager = (NotificationManager)
                    RideActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(intent.getIntExtra("notif_id", 0));
            finish();
        }
    };

    private final LocationListener gpsLocationListener =new LocationListener(){

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(networkLocationListener);
            updateDistances(location);
        }
    };
    private final LocationListener networkLocationListener =
            new LocationListener(){

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras){

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }

                @Override
                public void onLocationChanged(Location location) {
                    updateDistances(location);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nextEventBtn = (Button)findViewById(R.id.nextEventButton);
        addressText = (TextView)findViewById(R.id.address_Text);

        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(crit, true);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateDistances(location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_USER_CANCELLED_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG));
        registerReceiver(mCancelReceiver, new IntentFilter(GcmIntentService.JOURNEY_ALLOCATED_TAG));
    }

    public void picked_user(final Journey j){

        if(tracker==null){
            Toast.makeText(this,"Please check your GPS",Toast.LENGTH_LONG).show();
            return;
        }

        new PostTask(this) {
            @Override
            public List<NameValuePair> getPostData(String[] params, int i) {
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("journey_id", j.id));
                nameValuePairs.add(new BasicNameValuePair("pickup_location", String.valueOf(tracker.getLatitude())+","+String.valueOf(tracker.getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("key", getKey()));
                return nameValuePairs;
            }

            @Override
            public void onPostExecute(Result res){
                Toast.makeText(RideActivity.this,res.statusMessage,Toast.LENGTH_LONG).show();
                if(res.statusCode==200){
                    for (UserShortCard u : userCards){
                        if(u.journey.id.equals(j.id)) {
                            u.disablePicking();
                            for (Journey journey : group.journeys)
                                if(j.id.equals(journey.id)) {
                                    journey.journey_started = 1;
                                    journey.onRide=true;
                                    journey.startTime = new Date();
                                }
                            for(int i = 0;i<group.event_order.size();i++){
                                Group.Order order = group.event_order.get(i);
                                if(order.type==0 && j.id.equals(order.journey_id))
                                    group.event_order.remove(i);
                            }
                        }
                    }
                    showNextEventBtn();
                }
            }
        }.execute(Constants.getUrl("/picked_up_person/" + group.group_id));
    }

    public void dropped_user(final Journey j){
        if(tracker==null){
            Toast.makeText(this,"Please check your GPS",Toast.LENGTH_LONG).show();
            return;
        }
        Double tempDist=0.0;
        for (Journey journey : group.journeys)
            if(j.id.equals(journey.id)) {
                tempDist = tracker.calcDistance(journey.startTime,new Date());
            }
        final Double distance = tempDist;
        new PostTask(this) {
            @Override
            public List<NameValuePair> getPostData(String[] params, int i) {
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("journey_id", j.id));
                nameValuePairs.add(new BasicNameValuePair("drop_location", String.valueOf(tracker.getLatitude())+","+String.valueOf(tracker.getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("app_distance", String.valueOf(distance)));
                nameValuePairs.add(new BasicNameValuePair("key", getKey()));
                return nameValuePairs;
            }

            @Override
            public void onPostExecute(Result res){
                Toast.makeText(RideActivity.this,"Distance : "+String.valueOf(j.distance_travelled)+ " : " + "Distance1 : "+String.valueOf(distance),Toast.LENGTH_LONG).show();
                Toast.makeText(RideActivity.this,res.statusMessage,Toast.LENGTH_LONG).show();
                if(res.statusCode==200){
                    String fare = "null";
                    try {
                        fare = res.data.getString("fare");
                    }catch (Exception E){
                        E.printStackTrace();
                    }
                    Toast.makeText(RideActivity.this,res.statusMessage+" and Fare = Rs "+fare,Toast.LENGTH_LONG).show();
                    for (UserShortCard u : userCards){
                        if(u.journey.id.equals(j.id)) {
                            u.disableDropping();
                            for (Journey journey : group.journeys)
                                if(j.id.equals(journey.id)) {
                                    journey.journey_ended = 1;
                                    journey.onRide=false;

                                }
                            for(int i = 0;i<group.event_order.size();i++){
                                Group.Order order = group.event_order.get(i);
                                if(order.type==1 && j.id.equals(order.journey_id))
                                    group.event_order.remove(i);
                            }
                                                                                                                                                                                                                                                                            }

                    }
                    checkIfRideEnded();
                    showNextEventBtn();
                }
            }
        }.execute(Constants.getUrl("/end_journey/" + group.group_id));
    }                                                                                                                                                                                                                                                                                                                                                                                                                                                       

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume(){
        super.onResume();

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0,
                networkLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, gpsLocationListener);


        createScreen();
        checkIfRideEnded();
        showNextEventBtn();
    }

    @Override
    public void onPause(){
        super.onPause();

        locationManager.removeUpdates(networkLocationListener);
        locationManager.removeUpdates(gpsLocationListener);

        if(!rideEnded){
            SharedPreferences.Editor spe = prefs.edit();
            spe.putString("group", group.toString());
            spe.apply();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateDistances(location);
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateReceiver);
        unregisterReceiver(mCancelReceiver);
        super.onDestroy();
    }

    public void checkIfRideEnded(){
        boolean ended = true;
        for(Journey j : group.journeys){
            if(j.journey_ended==0)
                ended=false;
        }
        if(ended)
            endRide();
    }

    public void endRide(){
        new AlertDialog.Builder(this)
                .setTitle("Journey Ended")
                .setMessage("Here you will see option to Rate!!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().remove("group").apply();
                        startActivity(new Intent(RideActivity.this, MainActivity.class));
                        rideEnded=true;
                        finish();
                    }
                })
                .show();
    }

    public void showNextEventBtn(){
        nextEventBtn.setOnClickListener(null);

        if(group.event_order.size()<=0){
            nextEventBtn.setText(getString(R.string.this_ride_is_finish));
            nextEventBtn.setEnabled(false);
            return;
        }

        final Group.Order order = group.event_order.get(0);
        Journey j_temp=null;
        for (Journey j : group.journeys){
            if(order.journey_id.equals(j.id)){
                j_temp = j;
                break;
            }
        }
        final Journey journey = j_temp;

        if(journey==null)
            Log.e("ERROR","journey in event is not in group");

        if(order.type==0){
            addressText.setText(journey.start.longDescription);
            moveToLocation(new LatLng(journey.start.latitude,journey.start.longitude));
            nextEventBtn.setText(String.format(getString(R.string.picked_user), journey.user.name));
        }
        else{
            addressText.setText(journey.end.longDescription);
            moveToLocation(new LatLng(journey.end.latitude, journey.end.longitude));
            nextEventBtn.setText(String.format(getString(R.string.dropped_user),journey.user.name));
        }

        nextEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order.type == 0)
                    picked_user(journey);
                else
                    dropped_user(journey);
            }
        });
    }

    private void moveToLocation(LatLng currentLocation)
    {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }

    public void createScreen(){
        try {
            group=new Group(new JSONObject(prefs.getString("group","")));
            Log.d("MyAct Group", group.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LinearLayout user_view = ((LinearLayout)findViewById(R.id.people_cards));
        user_view.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1f);
        params.setMargins(4, 4, 4, 4);
        for(Journey j : group.journeys){
            Log.d("UserAdded", j.user.id);
            UserShortCard u = new UserShortCard(this);
            u.setLayoutParams(params);
            u.setPadding(10, 10, 10, 10);
            u.setJourney(j);
            user_view.addView(u);
            if(j.journey_started==1)
                u.disablePicking();
            if(j.journey_ended==1)
                u.disableDropping();
            userCards.add(u);
        }
    }


    public void updateDistances(Location location){
        Log.d("HERE", "YOYOYO");
        if(previousLocation==null){
            previousLocation=location;
        }else if(location!=null){
            for(Journey j:group.journeys)
            {
                if(j.onRide){
                    Log.d("DISTANC_SPEED",String.valueOf(location.getSpeed()));
                    j.distance_travelled+=previousLocation.distanceTo(location);
                    Log.d("DISTANCE_CALC","Updated distance :"+j.distance_travelled);
                }
            }
            previousLocation=location;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
}
