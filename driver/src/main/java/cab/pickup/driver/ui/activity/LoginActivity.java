package cab.pickup.driver.ui.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import cab.pickup.driver.R;
import cab.pickup.driver.server.GetTask;
import cab.pickup.driver.server.Result;


public class LoginActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            getSupportActionBar().hide();
        }
        catch (NullPointerException E){
            E.printStackTrace();
        }
        setContentView(R.layout.activity_login);

        new GetTask(this,"yolotest"){
            @Override
            public void onPostExecute(Result res){
                super.onPostExecute(res);
                if(res.statusCode==200) {
                    Log.d("yolo", res.data.toString());
                }
            }
        }.execute(getUrl("/user/1?key="+getKey()));
    }

}
