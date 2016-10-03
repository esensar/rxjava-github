package com.ensarsarajcic.reactivegithubsample.models;

/**
 * Created by ensar on 03/10/16.
 */
public class GitHubRepo {
    public static final String TAG = GitHubRepo.class.getSimpleName();

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
