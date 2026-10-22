package service;

import android.content.Context;
import com.example.projectdroid.R;

import java.net.MalformedURLException;
import java.net.URL;

public class ApiGetInfo {

    public static URL getApiUrl(Context cxt) {
        try {
            String urlString = cxt.getString(R.string.ApiURL);
            return new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getApiKey(Context cxt) {

        return cxt.getString(R.string.ApiKEY);
    }
}

