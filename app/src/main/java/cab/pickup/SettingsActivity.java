package cab.pickup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cab.pickup.widget.LocationSearchBar;


public class SettingsActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setEditText(getData(getString(R.string.profile_tag_name)),R.id.profile_name);
        setEditText(getData(getString(R.string.profile_tag_company)),R.id.profile_company);
        setEditText(getData(getString(R.string.profile_tag_number)),R.id.profile_number);
        setEditText(getData(getString(R.string.profile_tag_age)),R.id.profile_age);
        setEditText(getData(getString(R.string.profile_tag_gender)),R.id.profile_gender);
        setEditText(getData(getString(R.string.profile_tag_home)),R.id.profile_home);
        setEditText(getData(getString(R.string.profile_tag_office)),R.id.profile_office);
    }

    public void save(View v){
        SharedPreferences.Editor spe = prefs.edit();

        spe.clear();

        spe.putString(getString(R.string.profile_tag_name),getEditText(R.id.profile_name));
        spe.putString(getString(R.string.profile_tag_company),getEditText(R.id.profile_company));
        spe.putString(getString(R.string.profile_tag_number),getEditText(R.id.profile_number));
        spe.putString(getString(R.string.profile_tag_age),getEditText(R.id.profile_age));
        spe.putString(getString(R.string.profile_tag_gender),getEditText(R.id.profile_gender));

        spe.putString(getString(R.string.profile_tag_home), getEditText(R.id.profile_home));
        spe.putString(getString(R.string.profile_tag_office),getEditText(R.id.profile_office));

        spe.commit();
        spe.apply();
        finish();
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
}
