package com.example.chihurmnanyanwanevu.bakingapp.api;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.example.chihurmnanyanwanevu.bakingapp.data.models.Recipe;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Chihurumnanya.
 */

public class RecipeClient {

    public static final String BASE_URL = "https://d17h27t6h515a5.cloudfront.net/";
    public static Retrofit retrofit = null;

    public static Retrofit getClient(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
