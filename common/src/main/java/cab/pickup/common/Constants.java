package cab.pickup.common;

/**
 * Created by udiboy on 9/8/15.
 */
public class Constants {
    public static final String KEY = "";
    public static final String BASE_URL = "";

    public static String getUrl(String... path){
            return BASE_URL+(path!=null?path[0]:"");
    }
}
