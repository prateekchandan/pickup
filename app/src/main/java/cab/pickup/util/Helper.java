package cab.pickup.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import cab.pickup.ui.activity.MyActivity;

/**
 * Created by prateek on 3/8/15.
 */
public class Helper {
    public static void setFBImage(final String fbid,final MyActivity context,final ImageView v){
        new AsyncTask<String,Void,Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... strings) {
                File img =  new File(context.getFilesDir()+"/profile.jpg");
                Bitmap bmp= null;
                if(img.exists()){
                    try {
                        bmp = BitmapFactory.decodeFile(img.getAbsolutePath());
                    }
                    catch (Exception E){
                        E.printStackTrace();
                    }
                }else{
                    try {
                        URL img_url = new URL("https://graph.facebook.com/" + String.valueOf(fbid) + "/picture?type=large&redirect=true&width=400&height=400");
                        bmp = BitmapFactory.decodeStream(img_url.openConnection().getInputStream());
                        FileOutputStream fOut = new FileOutputStream(img);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                        fOut.flush();
                        fOut.close();
                    }
                    catch (Exception E){
                        E.printStackTrace();
                    }
                }
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap img){
                if(img!=null){
                    v.setImageBitmap(img);
                }
            }
        }.execute();
    }
}
