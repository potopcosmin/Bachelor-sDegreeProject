package com.example.myapplication.Retrofit;

import com.example.myapplication.DataModel.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface InterfaceAPI {


    @POST("add")
    Call<String> registerUser(@Body User user) ;
    @POST("login")
    Call<User> loginUser(@Body Map<String,String> cred);
}
