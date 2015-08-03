package cab.pickup.ui.activity;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import cab.pickup.R;
import cab.pickup.api.Event;
import cab.pickup.api.PastJourney;
import cab.pickup.server.GetTask;
import cab.pickup.server.Result;
import cab.pickup.ui.widget.EventView;

public class HistoryActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch (Exception E){
            E.printStackTrace();
        }
        setContentView(R.layout.activity_history);
        getData();
    }

    private void getData(){
        new GetTask(this,"Fetching data from server"){
            @Override
            public void onPostExecute(Result res){
                super.onPostExecute(res);
                if(res.statusCode==200){
                    try {
                        JSONArray journeys=res.data.getJSONArray("history");
                        ListView list = (ListView)findViewById(R.id.journey_view);
                        HistoryAdapter adapter = new HistoryAdapter(HistoryActivity.this);
                        list.setAdapter(adapter);
                        for (int i = 0;i<journeys.length();i++){
                            adapter.add(new PastJourney(journeys.getJSONObject(i)));
                        }
                    }
                    catch (Exception E){
                        E.printStackTrace();
                    }

                }
            }
        }.execute(getUrl("/get_history/"+String.valueOf(me.id)+"?key="+getKey()));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

class HistoryAdapter extends ArrayAdapter<PastJourney> {
    public HistoryAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        if(convertView==null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_journey, parent, false);

        PastJourney e = getItem(position);
        ((TextView)convertView.findViewById(R.id.status_text)).setText(e.status+" : "+e.start_text+ " to "+e.end_text);

        return convertView;
    }
}

