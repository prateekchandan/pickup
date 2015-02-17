package cab.pickup.widget;


import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.util.MapUtil;

public class PlacesAdapter  extends ArrayAdapter<Address>{
    List<Address> addrs = new ArrayList<>();
    Context context;

    public PlacesAdapter(Context context, List<Address> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);

        this.context=context;
        addrs = objects;
    }

    public PlacesAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);

        this.context=context;
    }

    @Override
    public void add(Address a){
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
        TextView rowView = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        rowView.setText(MapUtil.stringFromAddress(addrs.get(position)));
        rowView.setTag(addrs.get(position));

        rowView.setMinLines(0);
        rowView.setMaxLines(2);
        return rowView;
    }
}
