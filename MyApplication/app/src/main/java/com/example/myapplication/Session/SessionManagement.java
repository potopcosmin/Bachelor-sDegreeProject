package com.example.myapplication.Session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.DataModel.User;

public class SessionManagement {
    SharedPreferences sharedPreferences;
    String SESSION_Key="session_user";
    SharedPreferences.Editor editor;
    String LAST_UPDATE="lastUpdate";

    public SessionManagement(Context context){
        sharedPreferences=context.getSharedPreferences("session",Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void saveSession(User user){

    }

    public int getSession(){
    return sharedPreferences.getInt(SESSION_Key,-1);
    }

    public void removeSession(){
        editor.putInt(SESSION_Key,-1).apply();
    }
}
