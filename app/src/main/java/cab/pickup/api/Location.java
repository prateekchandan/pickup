package cab.pickup.api;

/**
 * Created by udiboy1209 on 30/4/15.
 */
public class Location {
    public double latitude, longitude;
    public String shortDescription,longDescription;

    public Location(double lat, double lng, String shDes){
        latitude=lat;
        longitude=lng;
        shortDescription=shDes;
        longDescription=shortDescription;
    }

    public Location(double lat, double lng, String shDes, String lnDes){
        latitude=lat;
        longitude=lng;
        shortDescription=shDes;
        longDescription=lnDes;
    }
}
