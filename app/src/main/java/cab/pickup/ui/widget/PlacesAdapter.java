package cab.pickup.ui.widget;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.R;
import cab.pickup.common.api.Location;

public class PlacesAdapter  extends ArrayAdapter<Location> {
    List<Location> addrs = new ArrayList<>();
    Context context;

    public PlacesAdapter(Context context, List<Location> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);

        this.context=context;
        addrs = objects;
    }

    public PlacesAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);

        this.context=context;
    }

    @Override
    public void add(Location a){
        addrs.add(a);
    }

    @Override
    public void clear(){
        addrs.clear();
    }

    @Override
    public int getCount(){
        return addrs.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout rowView = (RelativeLayout) inflater.inflate(R.layout.item_location_search_result, parent, false);

        ((TextView)rowView.findViewById(R.id.item_location_search_main_text)).setText(addrs.get(position).shortDescription);
        ((TextView)rowView.findViewById(R.id.item_location_search_sub_text)).setText(addrs.get(position).longDescription);
        rowView.setTag(addrs.get(position));
        return rowView;
    }
}
