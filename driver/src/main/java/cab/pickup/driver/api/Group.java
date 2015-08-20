package cab.pickup.driver.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cab.pickup.common.api.Driver;
import cab.pickup.common.api.User;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.util.UserDatabaseHandler;

/**
 * Created by prateek on 15/8/15.
 */
public class Group {
    public ArrayList<Journey> journeys = new ArrayList<>();
    public JSONObject json;
    public String group_id;
    public ArrayList<Order> event_order=new ArrayList<>();

    public Group(){
    }

    public Group(JSONObject rep,OnTaskCompletedListener listener) throws JSONException {
        Log.d("GROUP", rep.toString());

        if(rep.has("group_id"))
            group_id = rep.getString("group_id");


        if(rep.has("orders")){
            JSONArray orders = rep.getJSONArray("orders");
            for (int i = 0; i < orders.length(); i++) {
                event_order.add(new Order(new JSONObject(orders.getString(i))));
            }
        }

        if(rep.has("journeys")) {
            JSONArray journey_array = rep.getJSONArray("journeys");
            for (int i = 0; i < journey_array.length(); i++) {
                journeys.add(new Journey(new JSONObject(journey_array.getString(i))));
            }
            this.json = rep.getJSONObject("json");
        }else if(rep.has("journey_details")){
            JSONArray journey_array = rep.getJSONArray("journey_details");
            for (int i = 0; i < journey_array.length(); i++) {
                journeys.add(new Journey(new JSONObject(journey_array.getString(i)),listener));
            }
            this.json = rep;
            JSONObject path_waypts = new JSONObject(rep.getString("path_waypoints"));
            JSONArray start_pts = path_waypts.getJSONArray("start_order");
            JSONArray end_pts = path_waypts.getJSONArray("end_order");
            for(int i=0; i<start_pts.length(); i++){
                event_order.add(new Order(String.valueOf(start_pts.getInt(i)),0));
            }
            for(int i=0; i<start_pts.length(); i++){
                event_order.add(new Order(String.valueOf(start_pts.getInt(i)),1));
            }
        }
    }

    public Group(JSONObject rep) throws JSONException {
        Log.d("GROUP", rep.toString());
        if(rep.has("group_id"))
            group_id = rep.getString("group_id");


        if(rep.has("orders")){
            JSONArray orders = rep.getJSONArray("orders");
            for (int i = 0; i < orders.length(); i++) {
                event_order.add(new Order(new JSONObject(orders.getString(i))));
            }
        }


        if(rep.has("journeys")) {
            JSONArray journey_array = rep.getJSONArray("journeys");
            for (int i = 0; i < journey_array.length(); i++) {
                journeys.add(new Journey(new JSONObject(journey_array.getString(i))));
            }
            this.json = rep.getJSONObject("json");
        }else if(rep.has("journey_details")){
            JSONArray journey_array = rep.getJSONArray("journey_details");
            for (int i = 0; i < journey_array.length(); i++) {
                journeys.add(new Journey(new JSONObject(journey_array.getString(i))));
            }
            this.json = rep;
            JSONObject path_waypts = new JSONObject(rep.getString("path_waypoints"));
            JSONArray start_pts = path_waypts.getJSONArray("start_order");
            JSONArray end_pts = path_waypts.getJSONArray("end_order");
            for(int i=0; i<start_pts.length(); i++){
                event_order.add(new Order(String.valueOf(start_pts.getInt(i)),0));
            }
            for(int i=0; i<start_pts.length(); i++){
                event_order.add(new Order(String.valueOf(start_pts.getInt(i)),1));
            }
        }
    }


    @Override
    public String toString(){
        JSONObject rep=new JSONObject();
        JSONArray journey_array = new JSONArray();
        JSONArray order_array = new JSONArray();


        try {
            for(Journey j : journeys)
                journey_array.put(new JSONObject(j.toString()));

            for (Order o : event_order){
                order_array.put(new JSONObject(o.toString()));
            }

            rep.put("journeys",journey_array);
            rep.put("orders",order_array);
            rep.put("json",json);
            rep.put("group_id",group_id);
        } catch (JSONException e) {
            e.printStackTrace();

        }

        return rep.toString();
    }

    public class Order{
        public String journey_id;
        public int type; // 0 for pickup , 1 for drop

        public Order(){}

        public Order(JSONObject rep) throws JSONException{
            journey_id = rep.getString("journey_id");
            type = rep.getInt("type");
        }

        public Order(String _journey_id,int _type){
            journey_id = _journey_id;
            type = _type;
        }

        @Override
        public String toString(){
            JSONObject rep=new JSONObject();
            try {
                rep.put("journey_id", journey_id);
                rep.put("type", type);
            }catch (Exception E){
                E.printStackTrace();
            }
            return rep.toString();
        }
    };
}

