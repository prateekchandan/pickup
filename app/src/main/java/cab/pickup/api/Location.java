package cab.pickup.api;

public class Location {
    public double latitude, longitude;
    public String shortDescription,longDescription;
    public String placeId;
    public boolean locUpdated = false;

    public Location(){
        latitude=0;
        longitude=0;
    }

    public Location(double lat, double lng, String shDes){
        latitude=lat;
        longitude=lng;
        shortDescription=shDes;
        longDescription=shortDescription;

        locUpdated=true;
    }

    public Location(double lat, double lng, String shDes, String lnDes){
        latitude=lat;
        longitude=lng;
        shortDescription=shDes;
        longDescription=lnDes;

        locUpdated=true;
    }

    public Location(String place_id, String shDes){
        placeId=place_id;
        shortDescription=shDes;
        longDescription=shDes;
    }

    public void setLatLong(double lat,double lng){
        latitude = lat;
        longitude = lng;
    }
}
