package com.devzeze.restasyncdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.devzeze.restasyncdemo.model.Posts;
import com.devzeze.restasyncdemo.model.Users;
import com.devzeze.restasyncdemo.utils.ApiClient;
import com.devzeze.restasyncdemo.utils.ApiService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class RxJavaFragment extends Fragment {

    private static final String TAG = "RxJavaFragment";

    private ApiService apiService;
    private CompositeDisposable disposable = new CompositeDisposable();

    public RxJavaFragment() {
    }

    public static RxJavaFragment newInstance() {

        RxJavaFragment fragment = new RxJavaFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiService = ApiClient.getClient(getContext().getApplicationContext()).create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rx_java, container, false);

        Button simpleCall = view.findViewById(R.id.simple_call);
        simpleCall.setOnClickListener((View) -> {
            performSimpleCall();
        });

        Button zipCall = view.findViewById(R.id.zip_call);
        zipCall.setOnClickListener((View v) -> {
            performZipCall();
        });

        Button flatmapCall = view.findViewById(R.id.flatmap_call);
        flatmapCall.setOnClickListener((View v) -> {
            performFlatMapCall();
        });

        return view;
    }

    private void performSimpleCall() {

        disposable.add(apiService.fetchAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<Users>>() {

                    @Override
                    public void onSuccess(List<Users> users) {
                        if (users != null) {
                            Log.d(TAG, "onSuccess - Users " + users.size() + ";");
                        } else {
                            Log.d(TAG, "onSuccess - Users");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError", e);
                    }
                }));
    }

    private void performZipCall() {

        disposable.add(Single.zip(apiService.fetchAllUsers()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                apiService.fetchAllUsers()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                new BiFunction<List<Users>, List<Users>, String>() {
                    @Override
                    public String apply(List<Users> t1, List<Users> t2) throws Exception {

                        Log.d(TAG, "Zip list1 size: " + (t1 != null ? t1.size() : 0));
                        Log.d(TAG, "Zip list2 size: " + (t2 != null ? t2.size() : 0));
                        if (t1 != null && t2 != null) {
                            t1.addAll(t2);
                            return Integer.toString(t1.size());
                        } else {
                            return "0";
                        }
                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "Consumer total list size " + s);
                    }
                })
        );
    }

    private void performFlatMapCall() {

        disposable.add(apiService.fetchAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .flatMap(new Function<List<Users>, ObservableSource<Users>>() {
                    @Override
                    public ObservableSource<Users> apply(List<Users> users) throws Exception {
                        return Observable.fromIterable(users);
                    }
                })
                .flatMap(new Function<Users, ObservableSource<List<Posts>>>() {
                    @Override
                    public ObservableSource<List<Posts>> apply(Users user) throws Exception {
                        return apiService.fetchPostersByUser(user.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .toObservable();
                    }
                })
                .subscribeWith(new DisposableObserver<List<Posts>>() {
                    @Override
                    public void onNext(List<Posts> posters) {
                        if (posters != null && posters.size() > 0) {
                            Log.d(TAG, "onNext - userId " + posters.get(0).getUserId() + "; posters " + posters.size() + ";");
                        } else {
                            Log.d(TAG, "onNext - empty posters");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError", e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "Complete");
                    }
                })
        );

    }

    @Override
    public void onStop() {
        super.onStop();

        disposable.dispose();
    }
}
