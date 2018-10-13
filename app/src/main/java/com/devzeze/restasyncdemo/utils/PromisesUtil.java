package com.devzeze.restasyncdemo.utils;

import org.jdeferred2.Deferred;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromisesUtil {

    public static <T> Promise<T, Throwable, T> request(Call<T> call) {
        final Deferred<T, Throwable, T> deferred = new DeferredObject<>();
        call.enqueue(new Callback<T>() {

            @Override
            public void onResponse(Call<T> call, Response<T> response) {

                if (response.isSuccessful()) {
                    deferred.resolve(response.body());
                } else {
                    deferred.reject(new Exception("Exception " + response.body().toString()));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                deferred.reject(t);
            }
        });

        return deferred.promise();
    }
}
