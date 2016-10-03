package com.ensarsarajcic.reactivegithubsample.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ensarsarajcic.reactivegithubsample.R;
import com.ensarsarajcic.reactivegithubsample.models.GitHubRepo;
import com.ensarsarajcic.reactivegithubsample.models.GitHubUser;
import com.ensarsarajcic.reactivegithubsample.network.RestClient;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by ensar on 03/10/16.
 */
public class GitHubUsersAdapter extends RecyclerView.Adapter<GitHubUsersAdapter.GitHubUserViewHolder> {
    public static final String TAG = GitHubUsersAdapter.class.getSimpleName();

    private List<GitHubUser> users;
    private CompositeSubscription compositeSubscription;

    public GitHubUsersAdapter(List<GitHubUser> users) {
        this.users = users;
        compositeSubscription = new CompositeSubscription();
    }

    public void setItems(List<GitHubUser> users) {
        this.users = users;
    }

    @Override
    public GitHubUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new GitHubUserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final GitHubUserViewHolder holder, int position) {
        GitHubUser gitHubUser = users.get(position);
        holder.tvUserName.setText(gitHubUser.getLogin());
        holder.tvUserUrl.setText(gitHubUser.getHtml_url());

        Observable<Bitmap> fetchImageObservable = Observable.just(gitHubUser).startWith(new GitHubUser())
                .map(new Func1<GitHubUser, Bitmap>() {
                    @Override
                    public Bitmap call(GitHubUser gitHubUser) {
                        if(gitHubUser.getAvatar_url() == null) {
                            return BitmapFactory.decodeResource(holder.itemView.getContext().getResources(), R.mipmap.ic_launcher);
                        }

                        try {
                            URL url = new URL(gitHubUser.getAvatar_url());
                            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .filter(new Func1<Bitmap, Boolean>() {
                    @Override
                    public Boolean call(Bitmap bitmap) {
                        return bitmap != null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<List<GitHubRepo>> fetchUserReposObservable = Observable.just(gitHubUser)
                .map(new Func1<GitHubUser, List<GitHubRepo>>() {
                    @Override
                    public List<GitHubRepo> call(GitHubUser gitHubUser) {
                        try {
                            return RestClient.getGitHubApi().getUserRepos(gitHubUser.getLogin()).execute().body();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "call: ", e);
                            return null;
                        }
                    }
                })
                .filter(new Func1<List<GitHubRepo>, Boolean>() {
                    @Override
                    public Boolean call(List<GitHubRepo> gitHubRepos) {
                        return gitHubRepos != null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

//        Observable<Void> randomReposObservable = RxView.clicks(holder.tvRepos).subscribeOn(AndroidSchedulers.mainThread());
//
//        compositeSubscription.add(randomReposObservable.subscribe(new Subscriber<Void>() {
//            @Override
//            public void onCompleted() {
//                Log.d(TAG, "onCompleted: ");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.d(TAG, "onError: ");
//            }
//
//            @Override
//            public void onNext(Void aVoid) {
//                Log.d(TAG, "onNext: ");
//            }
//        }));
//
//        Observable<List<GitHubRepo>> gitHubReposObservable = Observable.combineLatest(randomReposObservable, fetchUserReposObservable, new Func2<Void, List<GitHubRepo>, List<GitHubRepo>>() {
//            @Override
//            public List<GitHubRepo> call(Void aVoid, List<GitHubRepo> gitHubRepos) {
//                return gitHubRepos;
//            }
//        }).subscribeOn(AndroidSchedulers.mainThread());

        Observable<List<String>> repoNamesObservable = fetchUserReposObservable.map(new Func1<List<GitHubRepo>, List<String>>() {
            @Override
            public List<String> call(List<GitHubRepo> gitHubRepos) {
                List<String> names = new ArrayList<String>();
                for (int i = 0; i < 3; i++) {
                    if(gitHubRepos.isEmpty()) break;
                    int position = new Random().nextInt(gitHubRepos.size());
                    names.add(gitHubRepos.get(position).getName());
                    gitHubRepos.remove(position);
                }
                return names;
            }
        });



        compositeSubscription.add(fetchImageObservable.subscribe(new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {
            }
            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Bitmap bitmap) {
                holder.ivUser.setImageBitmap(bitmap);
            }
        }));

        final ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(holder.itemView.getContext(), R.layout.repo);
        holder.lvRepos.setAdapter(stringArrayAdapter);

        compositeSubscription.add(repoNamesObservable.subscribe(new Subscriber<List<String>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<String> strings) {
                stringArrayAdapter.clear();
                stringArrayAdapter.addAll(strings);
                stringArrayAdapter.notifyDataSetChanged();
            }
        }));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class GitHubUserViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivUser;
        private TextView tvUserName;
        private TextView tvUserUrl;
        private ListView lvRepos;

        public GitHubUserViewHolder(View itemView) {
            super(itemView);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvUserUrl = (TextView) itemView.findViewById(R.id.tvUserUrl);
            lvRepos = (ListView) itemView.findViewById(R.id.lvRepos);
        }
    }

    public void clearSubscriptions() {
        if(!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }
}
