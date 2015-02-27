package cab.pickup.util;


import android.location.Address;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapUtil {
    private static final String TAG = "MapUtil";

    public static JSONObject getResult(String json){
        try {
            JSONObject result =new JSONObject(json);
            return (JSONObject)result.get("path");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    public static LatLngBounds getLatLngBounds(JSONObject result){
        try {
            JSONObject bounds = result.getJSONObject("bounds");

            JSONObject ne = bounds.getJSONObject("northeast");
            JSONObject sw = bounds.getJSONObject("southwest");

            return new LatLngBounds(new LatLng(sw.getDouble("lat"), sw.getDouble("lng")),
                    new LatLng(ne.getDouble("lat"), ne.getDouble("lng")));
        } catch (JSONException e){
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    public static ArrayList<LatLng> getPath(JSONObject result) {

        ArrayList<LatLng> lines = new ArrayList<LatLng>();

        try {
            //JSONArray routes = result.getJSONArray("routes");

            JSONArray steps = result.getJSONArray("legs")
                    .getJSONObject(0).getJSONArray("steps");

            for (int i = 0; i < steps.length(); i++) {
                String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");

                for (LatLng p : decodePolyline(polyline)) {
                    lines.add(p);
                }
            }
        } catch (JSONException e){
            Log.e(TAG, e.getMessage());
        }

        return lines;
    }



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
}
