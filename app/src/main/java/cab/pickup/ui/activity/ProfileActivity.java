package cab.pickup.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import cab.pickup.R;
import cab.pickup.common.util.Helper;

public class ProfileActivity extends MyActivity{

    private static final int STATE_VIEW = 1;
    private static final int STATE_EDIT = 2;
    ImageView mProfilePic;
    private int state=STATE_VIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        mProfilePic = (ImageView)findViewById(R.id.profile_picture);
        setProfilePicture();
        setProfileInfo();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadUI();
        setProfileInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(state==STATE_EDIT){
            menu.getItem(R.id.profile_menu_save).setVisible(true);
            menu.getItem(R.id.profile_menu_edit).setVisible(false);

            menu.getItem(android.R.id.home).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
            return true;
        } else if(state==STATE_VIEW){

            menu.getItem(R.id.profile_menu_save).setVisible(false);
            menu.getItem(R.id.profile_menu_edit).setVisible(true);

            menu.getItem(android.R.id.home).setIcon(R.drawable.b_arrow);
            return true;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        } else if(id == R.id.profile_menu_edit){
            state=STATE_EDIT;
            loadUI();
            return true;
        } else if(id == R.id.profile_menu_save){
            me.name=((TextView)findViewById(R.id.profile_name)).getText().toString();
            me.email=((TextView)findViewById(R.id.profile_email)).getText().toString();
            me.age=((TextView)findViewById(R.id.profile_age)).getText().toString();
            me.phone=((TextView)findViewById(R.id.profile_phone)).getText().toString();
            me.gender=((TextView)findViewById(R.id.profile_gender)).getText().toString();

            saveProfile();

            state=STATE_VIEW;
            loadUI();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadUI() {
        if(state==STATE_VIEW) {
            getSupportActionBar().setTitle(getText(R.string.profile_title_view));

            ((EditText)findViewById(R.id.profile_phone)).setInputType(InputType.TYPE_NULL);
            ((EditText)findViewById(R.id.profile_email)).setInputType(InputType.TYPE_NULL);
            ((EditText)findViewById(R.id.profile_gender)).setInputType(InputType.TYPE_NULL);
            ((EditText)findViewById(R.id.profile_age)).setInputType(InputType.TYPE_NULL);
            ((EditText)findViewById(R.id.profile_name)).setInputType(InputType.TYPE_NULL);
        } else if(state==STATE_EDIT){
            getSupportActionBar().setTitle(getText(R.string.profile_title_edit));

            ((EditText)findViewById(R.id.profile_phone)).setInputType(InputType.TYPE_CLASS_PHONE);
            ((EditText)findViewById(R.id.profile_email)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            ((EditText)findViewById(R.id.profile_gender)).setInputType(InputType.TYPE_CLASS_TEXT);
            ((EditText)findViewById(R.id.profile_age)).setInputType(InputType.TYPE_CLASS_NUMBER);
            ((EditText)findViewById(R.id.profile_name)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        }

        invalidateOptionsMenu();
    }

    public void setProfilePicture(){
        Helper.setFBImage(me.fbid,this,mProfilePic);
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
        ((TextView)findViewById(R.id.profile_email)).setText(me.email);
        ((TextView)findViewById(R.id.profile_phone)).setText(me.phone);
        ((TextView)findViewById(R.id.profile_gender)).setText(me.gender);
        ((TextView)findViewById(R.id.profile_age)).setText(me.age);
    }

    @Override
    public void onBackPressed() {
        if(state==STATE_EDIT){
            new AlertDialog.Builder(this).setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            state=STATE_VIEW;
                            loadUI();
                            setProfileInfo();
                        }
                    })
                    .setTitle("Edit")
                    .setMessage("Discard changes?")
                    .create().show();
        } else {
            super.onBackPressed();
        }
    }
}
