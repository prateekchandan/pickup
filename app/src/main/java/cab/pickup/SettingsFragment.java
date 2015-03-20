package cab.pickup;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
    private static final String ARG_FRAGMENT_ID = "fragment_id";
    private static final String TAG = "SettingsFragment";

    private int fragment_id;


    public static SettingsFragment newInstance(int frag_id) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAGMENT_ID, frag_id);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragment_id = getArguments().getInt(ARG_FRAGMENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(fragment_id, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle state){
        super.onActivityCreated(state);
        ((SettingsActivity)getActivity()).onSectionAttached(fragment_id);
    }
    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: "+fragment_id);

        ((SettingsActivity) activity).onSectionAttached(fragment_id);
    }*/
}
