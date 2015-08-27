package cab.pickup.driver;

/**
 * Created by prateek on 15/8/15.
 */
import android.app.Application;
import android.content.Context;

import cab.pickup.common.api.Driver;
import cab.pickup.common.util.UserDatabaseHandler;

public class MyApplication extends Application {
    protected static UserDatabaseHandler db;

    public static Driver driver;

    public static Context context;

    public MyApplication() {
        context = this;
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
