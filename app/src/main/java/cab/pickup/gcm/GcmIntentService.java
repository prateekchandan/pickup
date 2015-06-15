package cab.pickup.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.R;
import cab.pickup.ui.activity.MainActivity;
import cab.pickup.ui.activity.RideActivity;

public class GcmIntentService extends IntentService {

    public static final int TYPE_USER_ADDED=10,
                            TYPE_DRIVER_ADDED=11;

    static final String TAG = "Intent Service";
    public static final String MSG_REC_INTENT_TAG = "MESSAGE_RECIEVED";
    public static final String JOURNEY_ADD_USER_INTENT_TAG = "JOURNEY_ADD_USER";
    public static final String JOURNEY_ADD_DRIVER_INTENT_TAG = "JOURNEY_ADD_USER";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.e(TAG,"Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.e(TAG,"Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                Log.d(TAG, extras.toString());

                JSONObject msg = null;
                try {
                    msg = new JSONObject(extras.getString("message"));

                    int msg_type = msg.getInt("type");
                    JSONObject data = msg.getJSONObject("data");

                    if(msg_type==TYPE_USER_ADDED){
                        sendJourneyUpdate(JOURNEY_ADD_USER_INTENT_TAG, data.getString("user_id"));
                    } else if(msg_type==TYPE_DRIVER_ADDED){
                        sendJourneyUpdate(JOURNEY_ADD_DRIVER_INTENT_TAG, data.getString("user_id"));
                    }

                    Log.i(TAG, msg.getString("user_name"));
                    Log.i(TAG, msg.get("journey_id").toString());
                    Log.i(TAG, msg.get("type").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.i(TAG, "Received: " + extras.getString("message"));

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, String id) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        // TODO change to some other activity
        Intent i=new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Journey matched")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void sendMsg(String msg){
        Intent i = new Intent(MSG_REC_INTENT_TAG);

        i.putExtra("message", msg);

        sendBroadcast(i);
    }

    private void sendJourneyUpdate(String intent_tag, String user_id){
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i=new Intent(this, RideActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Journey matched")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Journey updated. Please check"));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(user_id.hashCode(), mBuilder.build());

        Intent broadcast = new Intent(intent_tag);
        broadcast.putExtra("id",user_id);
        broadcast.putExtra("notif_id",user_id.hashCode());

        sendBroadcast(broadcast);
    }
}
