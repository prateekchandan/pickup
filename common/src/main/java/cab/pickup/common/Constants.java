package cab.pickup.common;


import android.util.Log;

/**
 * Created by prateekchandan on 9/8/15.
 */
public class Constants {
    public static final String KEY = "9f83c32cf3c9d529e";
    public static final String DEBUG_KEY = "9f83c32cf3c9d529e";
    public static final String BASE_URL = "http://api.getpickup.in";
    public static final String DEBUG_BASE_URL = "http://apitest.getpickup.in/";
    public static boolean DEBUG = false;
    public static String getUrl(String... path){
        if (DEBUG) {
            return DEBUG_BASE_URL+(path!=null?path[0]:"");
        }
        return BASE_URL+(path!=null?path[0]:"");
    }

    public static String getKey(){
        if(DEBUG)
            return DEBUG_KEY;
        else
            return KEY;
    }
}
