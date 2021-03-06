package cab.pickup.ui.activity;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cab.pickup.R;


public class SettingsActivity extends MyActivity {
    private static final String TAG = "SettingsActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch (Exception E){
            E.printStackTrace();
        }
        setContentView(R.layout.activity_settings);
        Log.d(TAG,String.valueOf(me.id));

        if(me.id==null ) {
            Log.d("SettingsDebug","ID us NULL " + getData(getString(R.string.profile_tag_name)));
            me.name = getData(getString(R.string.profile_tag_name));
            me.email = getData(getString(R.string.profile_tag_email));
            me.gender = getData(getString(R.string.profile_tag_gender));
            me.fbid = getData(getString(R.string.profile_tag_fbid));

            setEditText(getData(getString(R.string.profile_tag_name)), R.id.profile_name);
            setEditText(getData(getString(R.string.profile_tag_email)), R.id.profile_email);
            setEditText(getData(getString(R.string.profile_tag_age)), R.id.profile_age);
            setEditText(getData(getString(R.string.profile_tag_gender)), R.id.profile_gender);
            setEditText(getData(getString(R.string.profile_tag_company)), R.id.profile_company);
            setEditText(getData(getString(R.string.profile_tag_number)), R.id.profile_number);
        }
        else
        {
            Log.d("SettingsDebug", "ID is " + me.id);
            ((TextView) findViewById(R.id.profile_name)).setText(me.name);
            ((TextView)findViewById(R.id.profile_age)).setText(me.age);
            ((TextView)findViewById(R.id.profile_gender)).setText(me.gender);
            ((TextView)findViewById(R.id.profile_email)).setText(me.email);
            ((TextView)findViewById(R.id.profile_company)).setText(me.company);
            ((TextView)findViewById(R.id.profile_number)).setText(me.phone);
            ((TextView)findViewById(R.id.profile_company_email)).setText(me.company_email);

        }

        Button mDoneButton = (Button) findViewById(R.id.done_btn);
        mDoneButton.getBackground().setColorFilter(getResources().getColor(R.color.theme_color), PorterDuff.Mode.MULTIPLY);
        mDoneButton.setTextColor(getResources().getColor(R.color.text_color_light));
    }


    public boolean validate()
    {
        boolean check = true;
        String emptyErrorMsg = getResources().getString(R.string.empty_error);
        int[] checksArray = {R.id.profile_name,R.id.profile_age,R.id.profile_number,R.id.profile_email,R.id.profile_gender,R.id.profile_company,R.id.profile_company_email};
        for(int id : checksArray){
            if (getEditText(id).equals("")) {
                ((EditText)findViewById(id)).setError(emptyErrorMsg);
                check = false;
            }
        }
        if(check){
            if(!Patterns.EMAIL_ADDRESS.matcher(((EditText)findViewById(R.id.profile_email)).getText()).matches()){
                ((EditText)findViewById(R.id.profile_email)).setError("Invalid email");
                check= false;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(((EditText)findViewById(R.id.profile_company_email)).getText()).matches()){
                ((EditText)findViewById(R.id.profile_company_email)).setError("Invalid email");
                check= false;
            }
            String phone = ((EditText)findViewById(R.id.profile_number)).getText().toString();
            String phoneregex = "^[7-9][0-9]{9}$";
            if(!phone.matches(phoneregex)){
                ((EditText)findViewById(R.id.profile_number)).setError("Please type 10 digit phone number");
                check= false;
            }
        }

        return check;
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void save(View v){
            if(!validate())
                return;
            me.fbid = getData("fbid");
            me.name = getEditText(R.id.profile_name);
            me.email = getEditText(R.id.profile_email);
            me.gender = getEditText(R.id.profile_gender);
            me.age=getEditText(R.id.profile_age);
            me.phone =getEditText(R.id.profile_number);
            me.company=getEditText(R.id.profile_company);
            me.company_email=getEditText(R.id.profile_company_email);

            SharedPreferences.Editor spe = prefs.edit();

            spe.putString("user_json", me.getJson());
            Log.d("Settings",me.getJson());
            spe.commit();

            setResult(RESULT_OK);
            finish();
    }

    public String getEditText(int id){
        return ((TextView)findViewById(id)).getText().toString();
    }

    public void setEditText(String text, int id){
        ((TextView)findViewById(id)).setText(text);
    }

    public String getData(String key){
       return getIntent().getStringExtra(key);
    }
}
