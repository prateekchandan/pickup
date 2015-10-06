package cab.pickup.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.MyApplication;
import cab.pickup.R;
import cab.pickup.common.api.PastJourney;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.Result;

public class HistoryActivity extends MyActivity implements OnRefreshListener{

    SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRefreshLayout=((SwipeRefreshLayout)findViewById(R.id.history_swipe_refresh));
        mRefreshLayout.setOnRefreshListener(this);

        getData();
    }

    private void getData() {
        HistoryAdapter adapter = new HistoryAdapter(HistoryActivity.this);
        adapter.addAll(MyApplication.getDB().getHistory());

        ((ListView)findViewById(R.id.history_list)).setAdapter(adapter);
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

    @Override
    public void onRefresh() {
        /**
         * Fetch from server and add to database
         */
        new GetTask(this){
            @Override
            public void onPostExecute(Result res) {
                super.onPostExecute(res);

                if(res.statusCode==200){
                    try {
                        JSONArray history = res.data.getJSONArray("past_journeys");
                        for(int i=0; i<history.length(); i++){
                            JSONObject past = history.getJSONObject(i);
                            PastJourney journey = new PastJourney();

                            // Only display completed journeys for now
                            if(past.getString("status").equals("completed")) {
                                journey.start_lat = (float) past.getDouble("start_lat");
                                journey.start_lng = (float) past.getDouble("start_long");
                                journey.end_lat = (float) past.getDouble("end_lat");
                                journey.end_lng = (float) past.getDouble("end_long");

                                journey.start_text = past.getString("start_text");
                                journey.end_text = past.getString("end_text");

                                journey.fare = past.getDouble("fare");
                                journey.time = past.getString("journey_time");
                                journey.distance = past.getDouble("distance");

                                MyApplication.getDB().addHistory(journey);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mRefreshLayout.setRefreshing(false);
            }
        };
    }

    class HistoryAdapter extends ArrayAdapter<PastJourney> {
        public HistoryAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_journey_history, parent, false);

            PastJourney e = getItem(position);
            ((TextView) convertView.findViewById(R.id.item_history_pickup_text)).setText(e.start_text);
            ((TextView) convertView.findViewById(R.id.item_history_drop_text)).setText(e.end_text);
            ((TextView) convertView.findViewById(R.id.item_history_fare)).setText(getString(R.string.item_history_fare,e.fare));
            ((TextView) convertView.findViewById(R.id.item_history_distance)).setText(getString(R.string.item_history_distance, e.distance));
            return convertView;
        }
    }

}

