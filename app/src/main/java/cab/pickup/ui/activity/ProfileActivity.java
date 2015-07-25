package cab.pickup.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import cab.pickup.R;

public class ProfileActivity extends MyActivity{

    ImageView mProfilePic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        try {
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.profile_actionbar_gradient));
        }
        catch (Exception E){
            E.printStackTrace();
        }
        setContentView(R.layout.activity_profile);
        mProfilePic = (ImageView)findViewById(R.id.profile_picture);
        setProfilePicture();
        setProfileInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        setProfileInfo();
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

    public void setProfilePicture(){
        new AsyncTask<String,Void,Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... strings) {
                File img =  new File(getFilesDir()+"/profile.jpg");
                Bitmap bmp= null;
                if(img.exists()){
                    try {
                        bmp = BitmapFactory.decodeFile(img.getAbsolutePath());
                    }
                    catch (Exception E){
                        E.printStackTrace();
                    }
                }else{
                    try {
                        URL img_url = new URL("https://graph.facebook.com/" + String.valueOf(me.fbid) + "/picture?type=large&redirect=true&width=400&height=400");
                        bmp = BitmapFactory.decodeStream(img_url.openConnection().getInputStream());
                        FileOutputStream fOut = new FileOutputStream(img);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                        fOut.flush();
                        fOut.close();
                    }
                    catch (Exception E){
                        E.printStackTrace();
                    }
                }
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap img){
                if(img!=null){
                    mProfilePic.setImageBitmap(img);
                }
            }
        }.execute();
    }

    public void setProfileInfo(){
        ((TextView)findViewById(R.id.profile_name)).setText(me.name);
        ((TextView)findViewById(R.id.profile_name_1)).setText(me.name);
        ((TextView)findViewById(R.id.profile_email)).setText(me.email);
        ((TextView)findViewById(R.id.profile_phone)).setText(me.mobile);
        ((TextView)findViewById(R.id.profile_gender)).setText(me.gender);
        String age_gender = String.valueOf(me.age);
        if(me.gender.equals("male"))
            age_gender+=" M";
        else if(me.gender.equals("female"))
            age_gender+=" F";
        ((TextView)findViewById(R.id.profile_age_gender)).setText(age_gender);
    }

    public void editProfile(View v){
        startActivity(new Intent(this,SettingsActivity.class));
    }
}
