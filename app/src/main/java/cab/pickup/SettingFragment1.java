package cab.pickup;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    OnButtonPressedListener mCallback;

    // Container Activity must implement this interface
    public interface OnButtonPressedListener {
        public void changeFrame();
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment1 newInstance(String param1, String param2) {
        SettingFragment1 fragment = new SettingFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }
    public void onNextPressed(View v)
    {
        mCallback.changeFrame();
    }
    public SettingFragment1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        /*setEditText(getData(getString(R.string.profile_tag_name)),R.id.profile_name);
        setEditText(getData(getString(R.string.profile_tag_email)),R.id.profile_email);
        setEditText(getData(getString(R.string.profile_tag_gender)),R.id.profile_gender);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_setting_basic, container, false);
        setEditText(getData(getString(R.string.profile_tag_name)),R.id.profile_name,v);
        setEditText(getData(getString(R.string.profile_tag_email)),R.id.profile_email,v);
        setEditText(getData(getString(R.string.profile_tag_gender)),R.id.profile_gender,v);
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    public String getData(String key){
        return getArguments().getString(key);
    }
    public void setEditText(String text, int id,View v){
        ((TextView)v.findViewById(id)).setText(text);
    }
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        try {
            ((SettingsActivity)activity).onSectionAttached(0);

            mCallback = (OnButtonPressedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
