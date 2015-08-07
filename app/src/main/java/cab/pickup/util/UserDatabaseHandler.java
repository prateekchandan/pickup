package cab.pickup.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cab.pickup.api.User;

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

    // Contacts Table Columns names
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

    public UserDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + id +" INTEGER PRIMARY KEY,"
                + fbid + " TEXT,"
                + device_id + " TEXT,"
                + name + " TEXT,"
                + email + " TEXT,"
                + gender + " TEXT,"
                + company + " TEXT,"
                + phone + " TEXT,"
                + age + " TEXT,"
                + company_email + " TEXT,"
                + date_fetched +" INTEGER"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

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
        values.put(id, user.id);
        values.put(fbid, user.fbid);
        values.put(device_id, user.device_id);
        values.put(name, user.name);
        values.put(email, user.email);
        values.put(gender, user.gender);
        values.put(company, user.company);
        values.put(phone, user.phone);
        values.put(age, user.age);
        values.put(company_email, user.company_email);
        values.put(date_fetched,System.currentTimeMillis());
        // Inserting Row
        try {
            db.insert(TABLE_USER, null, values);
        }catch (Exception E){
            E.printStackTrace();
        }
        db.close(); // Closing database connection
    }

    public User findUser(String id){
        String selectQuery = "SELECT  * FROM " + TABLE_USER + " where " + this.id + " = ?";
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
        return  user;
    }
}
