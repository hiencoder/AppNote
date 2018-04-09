package com.example.gmo.notesapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by GMO on 4/9/2018.
 */

public class PrefUtils {
    public PrefUtils(){}

    private static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("APP_PREF",Context.MODE_PRIVATE);
    }

    public static void saveApiKey(Context context, String apiKey){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("API_KEY",apiKey);
        editor.commit();
    }

    public static String getApiKey(Context context){
        return getSharedPreferences(context).getString("API_KEY","");
    }

}
