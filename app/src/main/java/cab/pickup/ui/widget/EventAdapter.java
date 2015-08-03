package cab.pickup.ui.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cab.pickup.R;
import cab.pickup.api.Event;
import cab.pickup.api.User;

public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        EventView eView = new EventView(super.getContext());
        ListView.LayoutParams lp = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
        eView.setLayoutParams(lp);

        Event e = getItem(position);
        eView.setEvent(e);

        return eView;
    }
}
