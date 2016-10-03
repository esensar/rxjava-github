package com.ensarsarajcic.reactivegithubsample.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.EditText;

import com.ensarsarajcic.reactivegithubsample.R;
import com.ensarsarajcic.reactivegithubsample.models.GitHubSearchResponse;
import com.ensarsarajcic.reactivegithubsample.models.GitHubUser;
import com.ensarsarajcic.reactivegithubsample.network.RestClient;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    EditText etSearch;
    RecyclerView rvUsers;

    CompositeSubscription compositeSubscription;

    Observable<CharSequence> textChangeStream;
    Observable<List<GitHubUser>> gitHubSearchResponseStream;
    Observable<List<GitHubUser>> gitHubUsersResponseStream;
    Observable<List<GitHubUser>> gitHubAllResponsesStream;

    GitHubUsersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSearch = (EditText) findViewById(R.id.etSearch);
        rvUsers = (RecyclerView) findViewById(R.id.rvUsers);

        adapter = new GitHubUsersAdapter(new ArrayList<GitHubUser>());
        rvUsers.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvUsers.setLayoutManager(layoutManager);
        rvUsers.setItemAnimator(new DefaultItemAnimator());

        compositeSubscription = new CompositeSubscription();
        textChangeStream = RxTextView.textChanges(etSearch).
                debounce(1, TimeUnit.SECONDS).subscribeOn(AndroidSchedulers.mainThread());

        gitHubSearchResponseStream = textChangeStream.filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        return !TextUtils.isEmpty(charSequence);
                    }
                })
                .map(new Func1<CharSequence, GitHubSearchResponse>() {
                    @Override
                    public GitHubSearchResponse call(CharSequence charSequence) {
                        try {
                            return RestClient.getGitHubApi().searchForUsers(charSequence.toString()).execute().body();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                            return null;
                        }
                    }
                })
                .filter(new Func1<GitHubSearchResponse, Boolean>() {
                    @Override
                    public Boolean call(GitHubSearchResponse gitHubSearchResponse) {
                        return gitHubSearchResponse != null;
                    }
                })
                .map(new Func1<GitHubSearchResponse, List<GitHubUser>>() {
                    @Override
                    public List<GitHubUser> call(GitHubSearchResponse gitHubSearchResponse) {
                        return gitHubSearchResponse.getItems();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        gitHubUsersResponseStream = textChangeStream.
                filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        return TextUtils.isEmpty(charSequence);
                    }
                })
                .map(new Func1<CharSequence, List<GitHubUser>>() {
                    @Override
                    public List<GitHubUser> call(CharSequence charSequence) {
                        try {
                            return RestClient.getGitHubApi().getUsers(new Random().nextInt(1000)).execute().body();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                            return new ArrayList<GitHubUser>();
                        }
                    }
                })
                .subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread());

        gitHubAllResponsesStream = Observable.merge(gitHubSearchResponseStream, gitHubUsersResponseStream);

        compositeSubscription.add(gitHubAllResponsesStream.subscribe(new Subscriber<List<GitHubUser>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(List<GitHubUser> gitHubUsers) {
                adapter.setItems(gitHubUsers);
                adapter.notifyDataSetChanged();
            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.clearSubscriptions();
        if(!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }
}
