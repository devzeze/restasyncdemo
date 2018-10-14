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
import com.devzeze.restasyncdemo.model.Todos;
import com.devzeze.restasyncdemo.model.Users;
import com.devzeze.restasyncdemo.utils.ApiClient;
import com.devzeze.restasyncdemo.utils.ApiPromisesService;
import com.devzeze.restasyncdemo.utils.PromisesUtil;

import org.jdeferred2.DeferredManager;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.DonePipe;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DefaultDeferredManager;
import org.jdeferred2.multiple.AllValues;
import org.jdeferred2.multiple.MasterProgress;
import org.jdeferred2.multiple.MultipleResults2;
import org.jdeferred2.multiple.OneValue;

import java.util.ArrayList;
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

        View view = inflater.inflate(R.layout.fragment_async, container, false);

        Button simpleCall = view.findViewById(R.id.simple_call);
        simpleCall.setOnClickListener((View) -> {
            performSimpleCall();
        });

        Button zipCall = view.findViewById(R.id.zip_call);
        zipCall.setOnClickListener((View v) -> {
            performWhenCall();
        });

        Button flatmapCall = view.findViewById(R.id.map_call);
        flatmapCall.setOnClickListener((View v) -> {
            performMap();
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

    private void performMap() {

        DeferredManager dm = new DefaultDeferredManager();

        long startTime = System.currentTimeMillis();
        PromisesUtil.request(apiPromisesService.fetchAllUsers())
                .then(new DonePipe<List<Users>, AllValues, Throwable, MasterProgress>() {
                    @Override
                    public Promise<AllValues, Throwable, MasterProgress> pipeDone(List<Users> result) {

                        List<Promise> list = new ArrayList<>(result.size());
                        for (Users user : result) {
                            list.add(PromisesUtil.request(apiPromisesService.fetchPostersByUser(user.getId())));
                        }
                        return dm.settle(list);
                    }
                }, null, null)
                .done(new DoneCallback<AllValues>() {
                    @Override
                    public void onDone(AllValues result) {

                        for (OneValue oneValue : result) {
                            List<Posts> posts = (List<Posts>) oneValue.getValue();
                            if (posts != null && posts.size() > 0) {
                                Log.d(TAG, "onNext - userId " + posts.get(0).getUserId() + "; posters " + posts.size() + ";");
                            } else {
                                Log.d(TAG, "onNext - empty posters");
                            }
                        }

                        long stopTime = System.currentTimeMillis();
                        Log.d(TAG, "Complete in " + Long.toString(stopTime - startTime));
                    }
                });
    }
}
