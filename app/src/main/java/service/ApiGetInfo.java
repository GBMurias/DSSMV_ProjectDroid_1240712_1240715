package service;

import android.content.Context;

import com.example.projectdroid.R;

public class ApiGetInfo {

    public static String getApiUrl(Context context) {

        return context.getString(R.string.ApiURL);
    }


    public static String getApiKey(Context context) {

        return context.getString(R.string.ApiKEY);
    }




}

