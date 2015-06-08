package cab.pickup.driver;

public class Location {
    public double latitude, longitude;
    public String shortDescription,longDescription;

    public Location(){
        latitude=0;
        longitude=0;
    }

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

    public String latlngString(){
        return latitude+","+longitude;
    }
}
