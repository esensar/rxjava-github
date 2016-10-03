package com.ensarsarajcic.reactivegithubsample.network;

import com.ensarsarajcic.reactivegithubsample.models.GitHubRepo;
import com.ensarsarajcic.reactivegithubsample.models.GitHubSearchResponse;
import com.ensarsarajcic.reactivegithubsample.models.GitHubUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ensar on 03/10/16.
 */
public interface GitHubApi {

    @GET("/users")
    Call<List<GitHubUser>> getUsers(@Query("since") Integer since);

    @GET("/search/users")
    Call<GitHubSearchResponse> searchForUsers(@Query("q") String query);

    @GET("/users/{user}/repos")
    Call<List<GitHubRepo>> getUserRepos(@Path("user") String user);
}
