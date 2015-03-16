package cab.pickup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cab.pickup.widget.LocationSearchBar;


public class SettingsActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        me.name=getData(getString(R.string.profile_tag_name));
        me.email=getData(getString(R.string.profile_tag_email));
        me.gender=getData(getString(R.string.profile_tag_gender));
        me.fbid=getData(getString(R.string.profile_tag_fbid));

        /*setEditText(getData(getString(R.string.profile_tag_name)),R.id.profile_name);
        setEditText(getData(getString(R.string.profile_tag_email)),R.id.profile_email);
        setEditText(getData(getString(R.string.profile_tag_company)),R.id.profile_company);
        setEditText(getData(getString(R.string.profile_tag_number)),R.id.profile_number);
        setEditText(getData(getString(R.string.profile_tag_age)),R.id.profile_age);
        setEditText(getData(getString(R.string.profile_tag_gender)),R.id.profile_gender);
        setEditText(getData(getString(R.string.profile_tag_home)),R.id.profile_home);
        setEditText(getData(getString(R.string.profile_tag_office)),R.id.profile_office);*/

        setEditText(me.name,R.id.profile_name);
        setEditText(me.email,R.id.profile_email);
        setEditText(me.gender,R.id.profile_gender);
    }

    public void save(View v){
        SharedPreferences.Editor spe = prefs.edit();

        spe.clear();
        /*String email=getEditText(R.id.profile_email);
        boolean validEmail=true;
        if (email.length()==0) validEmail=false;
        int n=0;
        int i=0;
        while (validEmail && email.charAt(i)!='@')
        {
            n++;
            i++;
            if (email.length()==i)
            {
                validEmail=false;
                break;
            }
        }

        if (n==0) validEmail=false;
        else n=0;
        while (validEmail && email.charAt(i)!='.')
        {
            if (email.charAt(i)=='@')
            {
                validEmail=false;
                break;
            }
            n++;
            i++;
            if (email.length()==i)
            {
                validEmail=false;
                break;
            }
        }
        if (n==0) validEmail=false;
        else n=0;
        while(validEmail && i<email.length())
        {
            i++;
            n++;
        }
        if (n==0) validEmail=false;
        else n=0;*/
        if (getEditText(R.id.profile_name).equals(""))
        {
            Context context = getApplicationContext();
            CharSequence text = "Name field musn't be empty!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else if (getEditText(R.id.profile_email).equals(""))
        {
            Context context = getApplicationContext();
            CharSequence text = "Email field musn't be empty!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        /*else if (!validEmail)
        {
            Context context = getApplicationContext();
            CharSequence text = "Please enter a valid email address";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }*/
        else if (getEditText(R.id.profile_company).equals(""))
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
        else if (getEditText(R.id.profile_age).equals(""))
        {
            Context context = getApplicationContext();
            CharSequence text = "Age field musn't be empty!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else if (getEditText(R.id.profile_gender).equals(""))
        {
            Context context = getApplicationContext();
            CharSequence text = "Gender field musn't be empty!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else {
            me.name = getEditText(R.id.profile_name);
            me.email = getEditText(R.id.profile_email);
            me.gender = getEditText(R.id.profile_gender);
            me.age=getEditText(R.id.profile_age);
            me.company=getEditText(R.id.profile_company);
            me.company_email=getEditText(R.id.profile_company_email);
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
