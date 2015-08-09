package cab.pickup.ui.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<String> {

    Context context;

    public UserListAdapter(Context context, List<String> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);

        this.context=context;

    }

    public UserListAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);

        this.context=context;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        UserProfileView user_layout = new UserProfileView(context);
        ListView.LayoutParams lp = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
        user_layout.setLayoutParams(lp);
        user_layout.setUserId(getItem(position));

        return user_layout;
    }
}
