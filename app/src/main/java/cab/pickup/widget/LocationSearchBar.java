package cab.pickup.widget;


import android.content.Context;
import android.location.Address;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class LocationSearchBar extends TextView implements View.OnClickListener{
    private static final String TAG = "LocationSearchBar";

    public LocationSearchBar(Context context) {
        super(context);

        init();
    }

    public LocationSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        LocationSearchDialog dialog = new LocationSearchDialog(getContext(), getId(), (Address)getTag());

        dialog.show();
    }
}
