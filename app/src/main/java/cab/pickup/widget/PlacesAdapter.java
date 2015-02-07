package cab.pickup.widget;


import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.R;

public class PlacesAdapter  extends ArrayAdapter<Address> implements Filterable {
    List<Address> addrs = new ArrayList<>();
    Context context;

    public PlacesAdapter(Context context, int resource, List<Address> objects) {
        super(context, resource, objects);

        this.context=context;
        addrs = objects;
    }

    public PlacesAdapter(Context context, int resource) {
        super(context, resource);

        this.context=context;
    }

    @Override
    public void add(Address a){
        addrs.add(a);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView rowView = (TextView) inflater.inflate(R.layout.location_list_item, parent, false);

        if(rowView!=null) rowView.setText(
                stringFromAddress(addrs.get(position)));
        return rowView;
    }

    @Override
    public Filter getFilter(){
        return new PlacesFilter();
    }

    private String stringFromAddress(Address a){
        return a.getFeatureName()+", "+a.getLocality();
    }

    class PlacesFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            results.values = addrs;
            results.count = addrs.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

        }
    }
}
