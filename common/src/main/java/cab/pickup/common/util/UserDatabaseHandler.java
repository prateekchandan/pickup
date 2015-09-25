package cab.pickup.common.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.common.api.Journey;
import cab.pickup.common.api.PastJourney;
import cab.pickup.common.api.User;

/**
 * Created by prateek on 7/8/15.
 */
public class UserDatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "pickupDB";

    // Contacts table name
    private static final String TABLE_USER = "users";
    private static final String TABLE_HISTORY = "history";

    // Contacts Table Columns names
    private static class UserTable
    {
        private static final String id = "id";
        private static final String fbid = "fbid";
        private static final String device_id = "device_id";
        private static final String name = "name";
        private static final String email = "email";
        private static final String gender = "gender";
        private static final String company = "company";
        private static final String phone = "phone";
        private static final String age = "age";
        private static final String company_email = "company_email";
        private static final String date_fetched = "date";
    };

    private static class HistoryTable
    {
        private static final String index = "_id";
        private static final String date_time = "date_time";
        private static final String fare = "fare";
        private static final String start="start";
        private static final String start_lat="start_lat";
        private static final String start_lng="start_lng";
        private static final String end="end";
        private static final String end_lat="end_lat";
        private static final String end_lng="end_lng";
        private static final String distance="distance";
    };

    public UserDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + UserTable.id +" INTEGER PRIMARY KEY,"
                + UserTable.fbid + " TEXT,"
                + UserTable.device_id + " TEXT,"
                + UserTable.name + " TEXT,"
                + UserTable.email + " TEXT,"
                + UserTable.gender + " TEXT,"
                + UserTable.company + " TEXT,"
                + UserTable.phone + " TEXT,"
                + UserTable.age + " TEXT,"
                + UserTable.company_email + " TEXT,"
                + UserTable.date_fetched +" INTEGER"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

        String CREATE_HISTORY_TABLE = "CREATE TABLE "+TABLE_HISTORY+"("
                +HistoryTable.index+" INTEGER PRIMARY KEY,"
                +HistoryTable.date_time+" TEXT,"
                +HistoryTable.distance+" REAL,"
                +HistoryTable.fare+" REAL,"
                +HistoryTable.start+" TEXT,"
                +HistoryTable.start_lat+" INTEGER,"
                +HistoryTable.start_lng+" INTEGER,"
                +HistoryTable.end+" TEXT,"
                +HistoryTable.end_lat+" INTEGER,"
                +HistoryTable.end_lng+" INTEGER"
                +")";
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);

        // Create tables again
        onCreate(db);
    }

    public void addUser(User user){
        if(user==null)
            return;
        if(findUser(user.id)!=null){
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserTable.id, user.id);
        values.put(UserTable.fbid, user.fbid);
        values.put(UserTable.device_id, user.device_id);
        values.put(UserTable.name, user.name);
        values.put(UserTable.email, user.email);
        values.put(UserTable.gender, user.gender);
        values.put(UserTable.company, user.company);
        values.put(UserTable.phone, user.phone);
        values.put(UserTable.age, user.age);
        values.put(UserTable.company_email, user.company_email);
        values.put(UserTable.date_fetched,System.currentTimeMillis());
        // Inserting Row
        try {
            db.insert(TABLE_USER, null, values);
        }catch (Exception E){
            E.printStackTrace();
        }
        db.close(); // Closing database connection
    }

    public User findUser(String id){
        if(id==null)
            return null;

        String selectQuery = "SELECT  * FROM " + TABLE_USER + " where " + UserTable.id + " = ?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{id});
        User user = null;
        if(cursor.moveToFirst()){
            user = new User();
            user.id = cursor.getString(0);
            user.fbid = cursor.getString(1);
            user.device_id = cursor.getString(2);
            user.name = cursor.getString(3);
            user.email = cursor.getString(4);
            user.gender = cursor.getString(5);
            user.company = cursor.getString(6);
            user.phone = cursor.getString(7);
            user.age = cursor.getString(8);
            user.company_email = cursor.getString(9);

        }
        cursor.close();
        db.close();
        //TODO : Update the last fetch time for user .. it will be used for removing unused users and save emory space
        return  user;
    }

    public void addHistory(PastJourney journey){
        if(journey==null)
            return;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HistoryTable.date_time, journey.time);
        values.put(HistoryTable.distance, journey.distance);
        values.put(HistoryTable.start, journey.start_text);
        values.put(HistoryTable.start_lat, journey.start_lat);
        values.put(HistoryTable.start_lng, journey.start_lng);
        values.put(HistoryTable.end, journey.end_text);
        values.put(HistoryTable.end_lat, journey.end_lat);
        values.put(HistoryTable.end_lng, journey.end_lng);
        values.put(HistoryTable.fare, journey.fare);
        // Inserting Row
        try {
            db.insert(TABLE_HISTORY, null, values);
        }catch (Exception E){
            E.printStackTrace();
        }
        db.close(); // Closing database connection
    }

    // TODO sort journeys based on a query
    public List<PastJourney> getHistory(){
        List<PastJourney> journeys = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        PastJourney journey = null;
        while(cursor.moveToFirst()){
            journey = new PastJourney();
            journey.time=cursor.getString(1);
            journey.distance=cursor.getDouble(2);
            journey.start_text=cursor.getString(3);
            journey.start_lat=cursor.getInt(4);
            journey.start_lng=cursor.getInt(5);
            journey.end_text=cursor.getString(6);
            journey.end_lat=cursor.getInt(7);
            journey.end_lng=cursor.getInt(8);
            journey.fare=cursor.getDouble(9);

            journeys.add(journey);
        }
        cursor.close();
        db.close();

        return journeys;
    }
}
