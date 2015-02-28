package cab.pickup.widget;


import android.content.Context;
import android.location.Address;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import cab.pickup.util.MapUtil;

public class LocationSearchBar extends TextView implements View.OnClickListener{

    private Address address;

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
        LocationSearchDialog dialog = new LocationSearchDialog(getContext(), getId(), (Address)getTag(), true);

        dialog.show();
    }

    public void setAddress(Address address) {
        this.address = address;

        setText(MapUtil.stringFromAddress(address));
    }

    public Address getAddress() {
        return address;
    }
}
