package cab.pickup.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cab.pickup.MyApplication;
import cab.pickup.R;
import cab.pickup.common.api.PastJourney;

public class HistoryActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    class HistoryAdapter extends ArrayAdapter<PastJourney> {
        public HistoryAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_journey, parent, false);

            PastJourney e = getItem(position);
            ((TextView) convertView.findViewById(R.id.fromText)).setText(e.start_text);
            ((TextView) convertView.findViewById(R.id.toText)).setText(e.end_text);
            ((TextView) convertView.findViewById(R.id.fareText)).setText(String.valueOf(e.fare));
            ((TextView) convertView.findViewById(R.id.distance_text)).setText(String.valueOf(e.distance) + " km");
            ImageView imgC = ((ImageView) convertView.findViewById(R.id.iconCancel));
            ImageView imgD = ((ImageView) convertView.findViewById(R.id.iconDone));
            if (e.status.equals("cancelled")) {
                imgC.setVisibility(View.VISIBLE);
            } else imgD.setVisibility(View.VISIBLE);
            return convertView;
        }
    }

}

