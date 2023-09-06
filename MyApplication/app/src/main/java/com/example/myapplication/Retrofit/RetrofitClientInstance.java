package com.example.myapplication.Retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClientInstance {
    private static final String API_BASE_URL="http://10.0.2.2:8000/users/";
    private static Retrofit retrofit;
    private static Gson gson;

    public static Retrofit getRetrofitInstance(){

        if(retrofit==null){
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BODY);

            // Add Interceptor to HttpClient
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            gson=new GsonBuilder().setLenient().create();

            retrofit=new Retrofit.Builder().baseUrl(API_BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson)).client(client).build();
        }
        return retrofit;
    }
}
