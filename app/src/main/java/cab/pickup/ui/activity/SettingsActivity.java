package cab.pickup.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cab.pickup.R;
import cab.pickup.SettingsFragment;
import cab.pickup.ui.widget.LocationPickerView;


public class SettingsActivity extends MyActivity {
    private static final String TAG = "SettingsActivity";
    int current_fragment_id;

    LocationPickerView homeOfficePicker;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (findViewById(R.id.fragment_container) != null) {

            me.name=getData(getString(R.string.profile_tag_name));
            me.email=getData(getString(R.string.profile_tag_email));
            me.gender=getData(getString(R.string.profile_tag_gender));
            me.fbid=getData(getString(R.string.profile_tag_fbid));

            loadFragment(R.layout.fragment_setting_basic);
        }
    }

    public void nextFragment(View v)
    {
        switch(current_fragment_id){
            case R.layout.fragment_setting_basic:
                loadFragment(R.layout.fragment_setting_company);
                break;
            case R.layout.fragment_setting_company:
                loadFragment(R.layout.fragment_setting_phone);
                break;
            case R.layout.fragment_setting_phone:
                loadFragment(R.layout.fragment_setting_address);
                break;
            case R.layout.fragment_setting_address:
                homeOfficePicker=(LocationPickerView)findViewById(R.id.home_office_picker);
                if(validate(current_fragment_id)) save(current_fragment_id);
                break;
        }
    }

    public void loadFragment(int fragment_id){
        if(validate(current_fragment_id)) {
            save(current_fragment_id);

            Log.d(TAG, "loadFragment : "+fragment_id);

            current_fragment_id = fragment_id;

            SettingsFragment f1 = SettingsFragment.newInstance(fragment_id);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f1).commit();
        }
    }

    /*public void changeFrame()
    {
        Log.d(TAG, "Value of framenumber: "+fragment);
        switch (fragment)
        {

            case 1:
                if (!saved[1] && validate(1)) {
                    saved[fragment]=true;
                    Log.d(TAG, "Case Executed 1, "+fragment);
                    save(1);
                    fragment++;
                    SettingFragment2 newFragment = new SettingFragment2();
                    newFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.fragment_container, newFragment);
                    // transaction.addToBackStack(null);
                    transaction1.commit();
                }
                    break;

            case 2:
                if (!saved[2] && validate(2)) {
                    saved[fragment]=true;
                    fragment++;
                    save(2);
                    SettingFragment3 newFragment = new SettingFragment3();
                    newFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.fragment_container, newFragment);
                    // transaction.addToBackStack(null);
                    transaction1.commit();
                }
                    break;
            case 3:
                if (!saved[3] && validate(3)) {
                    saved[fragment]=true;
                    fragment++;
                    save(3);
                    SettingFragment4 newFragment = new SettingFragment4();
                    newFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.fragment_container, newFragment);
                    // transaction.addToBackStack(null);
                    transaction1.commit();
                }
                break;
            case 4:
                if (!saved[4] && validate(4)) {
                    saved[fragment]=true;
                    fragment++;
                    save(4);

                }
                break;

        }
    }*/
    public void onSectionAttached(int fragment_id)
    {
        Log.d(TAG, "onSectionAttached: "+fragment_id);
        switch(fragment_id) {
            case R.layout.fragment_setting_basic:
                setEditText(getData(getString(R.string.profile_tag_name)), R.id.profile_name);
                setEditText(getData(getString(R.string.profile_tag_email)), R.id.profile_email);
                setEditText(getData(getString(R.string.profile_tag_age)), R.id.profile_age);
                setEditText(getData(getString(R.string.profile_tag_gender)), R.id.profile_gender);
                break;
            case R.layout.fragment_setting_company:
                setEditText(getData(getString(R.string.profile_tag_company)), R.id.profile_company);

                break;
            case R.layout.fragment_setting_phone:
                setEditText(getData(getString(R.string.profile_tag_number)), R.id.profile_number);
                break;

            //setEditText(getData(getString(R.string.profile_tag_home)), R.id.profile_home);
            //setEditText(getData(getString(R.string.profile_tag_office)), R.id.profile_office);
        }
    }

    public boolean validate(int fragment_id)
    {

        Log.d(TAG,"Validate executed with framenumber :"+fragment_id);
        if (fragment_id==R.layout.fragment_setting_basic) {
            if (getEditText(R.id.profile_name).equals("")) {
                Toast.makeText(this,"Name field musn't be empty!",Toast.LENGTH_LONG).show();
                return false;
            } else if (getEditText(R.id.profile_email).equals("")) {
                Toast.makeText(this,"Email field musn't be empty!",Toast.LENGTH_LONG).show();
                return false;
            }
            else if (getEditText(R.id.profile_age).equals(""))
            {
                Toast.makeText(this,"Age field musn't be empty!",Toast.LENGTH_LONG).show();
                return false;
            }
            else if (getEditText(R.id.profile_gender).equals(""))
            {
                Toast.makeText(this,"Gender field musn't be empty!",Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else if (fragment_id==R.layout.fragment_setting_company)
        {
            if (getEditText(R.id.profile_company).equals(""))
            {
                Toast.makeText(this,"Company field musn't be empty!",Toast.LENGTH_LONG).show();
                return false;
            }
            else if (getEditText(R.id.profile_company_email).equals(""))
            {
                Toast.makeText(this,"Company Email Address musn't be empty!",Toast.LENGTH_LONG).show();
                return false;
            }
        } else if (fragment_id == R.layout.fragment_setting_address){
            if(homeOfficePicker.home==null){
                Toast.makeText(this,"You should pick a location for home before proceeding!",Toast.LENGTH_LONG).show();
                return false;
            } else if(homeOfficePicker.office==null){
                Toast.makeText(this,"You should pick a location for office before proceeding!",Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    public void save(int fragment_id){
        Log.d(TAG, "Save executed with framenumber :"+fragment_id);
        if (fragment_id==R.layout.fragment_setting_basic) {

            me.name = getEditText(R.id.profile_name);
            me.email = getEditText(R.id.profile_email);
            me.gender = getEditText(R.id.profile_gender);
            me.age=getEditText(R.id.profile_age);
        }
        else if (fragment_id==R.layout.fragment_setting_company)
        {
            me.company=getEditText(R.id.profile_company);
            me.company_email=getEditText(R.id.profile_company_email);
        }
        else if (fragment_id==R.layout.fragment_setting_phone)
        {
            me.mobile=getEditText(R.id.profile_number);
        }

        else if (fragment_id==R.layout.fragment_setting_address) {
            me.home=homeOfficePicker.home;
            me.office=homeOfficePicker.office;
            SharedPreferences.Editor spe = prefs.edit();

            spe.putString("user_json", me.getJson());
            spe.commit();

            setResult(RESULT_OK);
            finish();
        }
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
