package com.ensarsarajcic.reactivegithubsample.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ensar on 03/10/16.
 */
public class RestClient {
    public static final String TAG = RestClient.class.getSimpleName();

    private static GitHubApi gitHubApi;
    private static Retrofit restAdapter = null;

    public static Retrofit getRestAdapter() {
        if(restAdapter == null) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.github.com/");
            restAdapter = builder.build();
        }
        return  restAdapter;
    }


    public static GitHubApi getGitHubApi() {
        if(gitHubApi == null) {
            gitHubApi = getRestAdapter().create(GitHubApi.class);
        }
        return gitHubApi;
    }


}
