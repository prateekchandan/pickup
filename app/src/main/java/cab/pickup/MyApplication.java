package cab.pickup;


import android.app.Application;
import android.content.Context;

public class MyApplication extends Application{
    public MyApplication() {
    }

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        // Initialize the Parse SDK.
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
