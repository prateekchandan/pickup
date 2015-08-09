package cab.pickup.common;

/**
 * Created by udiboy on 9/8/15.
 */
public class Constants {
    public static final String KEY = "9f83c32cf3c9d529e";
    public static final String BASE_URL = "http://pickup.prateekchandan.me";

    public static String getUrl(String... path){
            return BASE_URL+(path!=null?path[0]:"");
    }
}
