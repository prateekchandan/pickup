package cab.pickup.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.R;
import cab.pickup.api.User;

public class UserListAdapter extends ArrayAdapter<String> {
    List<String> users = new ArrayList<>();
    Context context;

    public UserListAdapter(Context context, List<String> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);

        this.context=context;
        users = objects;
    }

    public UserListAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);

        this.context=context;
    }

    @Override
    public void add(String u){
        users.add(u);
    }

    @Override
    public void clear(){
        users.clear();
    }

    @Override
    public int getCount(){
        return users.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        UserProfileView user_layout = new UserProfileView(context);
        ListView.LayoutParams lp = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
        user_layout.setLayoutParams(lp);
        user_layout.setUserId(users.get(position));

        return user_layout;
    }
}
