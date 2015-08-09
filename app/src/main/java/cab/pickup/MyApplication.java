package cab.pickup;


import android.app.Application;
import android.content.Context;

import cab.pickup.common.util.UserDatabaseHandler;

public class MyApplication extends Application{
    protected static UserDatabaseHandler db;

    public MyApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MyApplication.db=new UserDatabaseHandler(getApplicationContext());
    }

    public static UserDatabaseHandler getDB(){
        return db;
    }
}
