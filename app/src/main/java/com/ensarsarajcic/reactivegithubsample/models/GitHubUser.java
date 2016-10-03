package com.ensarsarajcic.reactivegithubsample.models;

/**
 * Created by ensar on 03/10/16.
 */
public class GitHubUser {
    public static final String TAG = GitHubUser.class.getSimpleName();

    private String login;
    private String html_url;
    private String avatar_url;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }
}
