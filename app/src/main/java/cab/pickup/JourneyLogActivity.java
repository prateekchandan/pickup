package cab.pickup;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cab.pickup.util.User;

//import com.google.android.gms.plus.model.people.Person;


public class JourneyLogActivity extends MyActivity {
    LinearLayout steps;
    private ArrayList<JourneySteps> stepList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_log);

        steps = (LinearLayout) findViewById(R.id.JourneyList);

        initList();
        //ListAdapter adapter = new CustomAdapter(this, stepList);
        displayLog();
        //setListAdapter(adapter);

    }

    // public void JourneyLogActivity() {}
    private void displayLog() {
        for (int position = 0; position < stepList.size(); position++) {
            ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_journey_log_row_layout, steps, false);
            JourneySteps step = stepList.get(position);
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(step.getName());
            ImageView img = (ImageView) view.findViewById(R.id.avatar);
            img.setImageBitmap(step.getImage());
            view.setTag(step.getUser());

            steps.addView(view);
        }

    }

    private void initList() {

        stepList = new ArrayList<JourneySteps>();
        addItem("arbit", null);
        addItem("arbit2", null);

    }

    private void addItem(String message, User user) {
        Bitmap avatar = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_accept);
        JourneySteps step = new JourneySteps(message);
        step.setUser(user);
        step.setImage(avatar);
        stepList.add(step);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_journey_log, menu);
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

    public String generateNotification() {
        String notification = "";
        return notification;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void sendNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("PickUp")
                        .setContentText(generateNotification());
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(45, mBuilder.build());
    }


    class JourneySteps {
        private String message;
        private Bitmap image;
        private User user;

        public JourneySteps(String name) {
            message = name;
        }

        public String getName() {
            return message;
        }


        public Bitmap getImage() {
            return image;
        }

        public void setImage(Bitmap avatar) {
            image = avatar;
        }

        public void setUser(User u) {
            user = u;
        }

        public User getUser() {
            return user;
        }
    }

/*class CustomAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<JourneySteps> step;

    public CustomAdapter(Context context, List<JourneySteps> steps1) {
        mInflater = LayoutInflater.from(context);
        step=steps1;
    }

    @Override
    public int getCount() {
        return step.size();
    }

    @Override
    public Object getItem(int position) {
        return step.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.activity_journey_log_row_layout, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView)view.findViewById(R.id.avatar);
            holder.message = (TextView)view.findViewById(R.id.name);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        JourneySteps person = step.get(position);
        holder.image.setImageBitmap(person.getImage());
        holder.message.setText(person.getName());


        return view;
    }

    private class ViewHolder {
        public ImageView image;
        public TextView message;
    }
}*/
}


