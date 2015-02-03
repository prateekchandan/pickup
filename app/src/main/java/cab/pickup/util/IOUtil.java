package cab.pickup.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtil {
    public static String buildStringFromIS(InputStream is) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));

            int c;
            while((c=bf.read()) != -1){
                sb.append((char)c);
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
