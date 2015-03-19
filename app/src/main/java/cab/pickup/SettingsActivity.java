package cab.pickup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cab.pickup.widget.LocationSearchBar;


public class SettingsActivity extends MyActivity implements SettingFragment1.OnButtonPressedListener{

    int fragment=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            SettingFragment1 f1=new SettingFragment1();
            f1.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, f1).commit();
            me.name=getData(getString(R.string.profile_tag_name));
            me.email=getData(getString(R.string.profile_tag_email));
            me.gender=getData(getString(R.string.profile_tag_gender));
            me.fbid=getData(getString(R.string.profile_tag_fbid));


        }

        /*setEditText(getData(getString(R.string.profile_tag_name)),R.id.profile_name);
        setEditText(getData(getString(R.string.profile_tag_email)),R.id.profile_email);
        setEditText(getData(getString(R.string.profile_tag_company)),R.id.profile_company);
        setEditText(getData(getString(R.string.profile_tag_number)),R.id.profile_number);
        setEditText(getData(getString(R.string.profile_tag_age)),R.id.profile_age);
        setEditText(getData(getString(R.string.profile_tag_gender)),R.id.profile_gender);
        setEditText(getData(getString(R.string.profile_tag_home)),R.id.profile_home);
        setEditText(getData(getString(R.string.profile_tag_office)),R.id.profile_office);*/


    }
    public void onNextPressedFrame(View v)
    {
        fragment++;
        onSectionAttach(fragment);
    }
    public void changeFrame(int framenumber)
    {
        switch (framenumber)
        {

            case 2:
                if (validate(1)) {
                    save(1);
                    SettingFragment2 newFragment = new SettingFragment2();
                    newFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.fragment_container, newFragment);
                    // transaction.addToBackStack(null);
                    transaction1.commit();
                }
                    break;

            case 3:
                if (validate(2)) {
                    save(2);
                    SettingFragment3 newFragment = new SettingFragment3();
                    newFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.fragment_container, newFragment);
                    // transaction.addToBackStack(null);
                    transaction1.commit();
                }
                    break;
            case 4:
                if (validate(3)) {
                    save(3);
                    SettingFragment4 newFragment = new SettingFragment4();
                    newFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.fragment_container, newFragment);
                    // transaction.addToBackStack(null);
                    transaction1.commit();
                }
                break;
            case 5:
                if (validate(4)) {
                    save(4);

                }
                break;

        }
    }
    public void onSectionAttach(int i)
    {
        changeFrame(i);
    }
    public boolean validate(int fragmentnumber)
    {
        if (fragmentnumber==1) {
            if (getEditText(R.id.profile_name).equals("")) {
                Context context = getApplicationContext();
                CharSequence text = "Name field musn't be empty!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return false;
            } else if (getEditText(R.id.profile_email).equals("")) {
                Context context = getApplicationContext();
                CharSequence text = "Email field musn't be empty!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return false;
            }
            else if (getEditText(R.id.profile_age).equals(""))
            {
                Context context = getApplicationContext();
                CharSequence text = "Age field musn't be empty!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return false;
            }
            else if (getEditText(R.id.profile_gender).equals(""))
            {
                Context context = getApplicationContext();
                CharSequence text = "Gender field musn't be empty!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return false;
            }
            return true;
        }
        else if (fragmentnumber==2)
        {
            if (getEditText(R.id.profile_company).equals(""))
            {
                Context context = getApplicationContext();
                CharSequence text = "Company field musn't be empty!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else if (getEditText(R.id.profile_company_email).equals(""))
            {
                Context context = getApplicationContext();
                CharSequence text = "Company Email Address musn't be empty!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
        return true;
    }
    public void save(int fragmentnumber){
        SharedPreferences.Editor spe = prefs.edit();
        if (fragmentnumber==1) {

            spe.clear();
            me.name = getEditText(R.id.profile_name);
            me.email = getEditText(R.id.profile_email);
            me.gender = getEditText(R.id.profile_gender);
            me.age=getEditText(R.id.profile_age);
        }
        else if (fragmentnumber==2)
        {
            me.company=getEditText(R.id.profile_company);
            me.company_email=getEditText(R.id.profile_company_email);
        }
        else if (fragmentnumber==3)
        {
            me.mobile=getEditText(R.id.profile_number);
        }

        else if (fragmentnumber==4) {



            spe.putString("user_json", me.getJson());
        /*spe.putString(getString(R.string.profile_tag_name),getEditText(R.id.profile_name));
        spe.putString(getString(R.string.profile_tag_email),getEditText(R.id.profile_email));
        spe.putString(getString(R.string.profile_tag_company),getEditText(R.id.profile_company));
        spe.putString(getString(R.string.profile_tag_number),getEditText(R.id.profile_number));
        spe.putString(getString(R.string.profile_tag_age),getEditText(R.id.profile_age));
        spe.putString(getString(R.string.profile_tag_gender),getEditText(R.id.profile_gender));

        spe.putString(getString(R.string.profile_tag_home), getEditText(R.id.profile_home));
        spe.putString(getString(R.string.profile_tag_office),getEditText(R.id.profile_office));

        spe.putString(getString(R.string.profile_tag_fbid),getData(getString(R.string.profile_tag_fbid)));*/

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

    public void getAddress(int id){
        ((LocationSearchBar)findViewById(id)).getAddress();
    }

    public String getData(String key){
       return getIntent().getStringExtra(key);
    }
}
