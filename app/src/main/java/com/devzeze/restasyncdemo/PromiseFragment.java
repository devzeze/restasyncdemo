package com.devzeze.restasyncdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.devzeze.restasyncdemo.model.Todos;
import com.devzeze.restasyncdemo.model.Users;
import com.devzeze.restasyncdemo.utils.ApiClient;
import com.devzeze.restasyncdemo.utils.ApiPromisesService;
import com.devzeze.restasyncdemo.utils.PromisesUtil;

import org.jdeferred2.DeferredManager;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DefaultDeferredManager;
import org.jdeferred2.multiple.MultipleResults2;

import java.util.List;


public class PromiseFragment extends Fragment {

    private static final String TAG = "PromiseFragment";

    private ApiPromisesService apiPromisesService;

    public PromiseFragment() {
    }

    public static PromiseFragment newInstance() {

        PromiseFragment fragment = new PromiseFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiPromisesService = ApiClient.getClientPromise(getContext().getApplicationContext()).create(ApiPromisesService.class);
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
            performWhenCall();
        });

        Button flatmapCall = view.findViewById(R.id.flatmap_call);
        flatmapCall.setOnClickListener((View v) -> {
            performFlatMapCall();
        });

        return view;
    }

    private void performSimpleCall() {

        Promise<List<Todos>, Throwable, List<Todos>> promise = PromisesUtil.request(apiPromisesService.fetchAllTodos());

        promise.then(new DoneCallback<List<Todos>>() {
            @Override
            public void onDone(List<Todos> result) {
                if (result != null) {
                    Log.d(TAG, "onSuccess - Todos " + result.size() + ";");
                } else {
                    Log.d(TAG, "onSuccess - Todos");
                }
            }
        });
    }

    private void performWhenCall() {

        DeferredManager dm = new DefaultDeferredManager();

        Promise<List<Users>, Throwable, List<Users>> fetchAllUsers = PromisesUtil.request(apiPromisesService.fetchAllUsers());
        Promise<List<Todos>, Throwable, List<Todos>> fetchAllTodos = PromisesUtil.request(apiPromisesService.fetchAllTodos());

        dm.when(fetchAllUsers, fetchAllTodos).then(new DoneCallback<MultipleResults2<List<Users>, List<Todos>>>() {
            @Override
            public void onDone(MultipleResults2<List<Users>, List<Todos>> result) {
                List<Users> listUsers = result.getFirst().getResult();
                List<Todos> listTodos = result.getSecond().getResult();

                Log.d(TAG, "When list1 size: " + (listUsers != null ? listUsers.size() : 0));
                Log.d(TAG, "When list2 size: " + (listTodos != null ? listTodos.size() : 0));
            }
        });
    }

    private void performFlatMapCall() {

        /*
        disposable.add(apiRxJavaService.fetchAllUsers()
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
                        return apiRxJavaService.fetchPostersByUser(user.getId())
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
        */
    }
}
