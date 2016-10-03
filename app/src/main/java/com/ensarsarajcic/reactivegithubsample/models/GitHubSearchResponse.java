package com.ensarsarajcic.reactivegithubsample.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ensar on 03/10/16.
 */
public class GitHubSearchResponse {

    private List<GitHubUser> items;

    public List<GitHubUser> getItems() {
        return items;
    }

    public void setItems(List<GitHubUser> items) {
        this.items = items;
    }
}
