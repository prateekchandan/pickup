package cab.pickup.util;


import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapUtil {
    private static final String TAG = "MapUtil";

    public static List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }

    public static String stringFromAddress(Address address) {
        if(address == null) return "";

        String addr = address.getAddressLine(0);
        if(address.getAddressLine(1)!=null) addr+=", "+address.getAddressLine(1);
        if(address.getAddressLine(2)!=null) addr+=", "+address.getAddressLine(2);

        return addr;
    }

    public static Address addressFrom(double lat, double lng, String text){
        Address a=new Address(Locale.CHINA);

        a.setAddressLine(0,text);

        a.setLatitude(lat); a.setLongitude(lng);

        return a;
    }
}
