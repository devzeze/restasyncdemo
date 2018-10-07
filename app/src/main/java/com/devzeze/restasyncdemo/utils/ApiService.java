package com.devzeze.restasyncdemo.utils;

import com.devzeze.restasyncdemo.model.Posts;
import com.devzeze.restasyncdemo.model.Users;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("users")
    Single<List<Users>> fetchAllUsers();

    @GET("todos")
    Single<List<Users>> fetchAllTodos();


    @GET("posts")
    Single<List<Posts>> fetchPostersByUser(@Query("userId") long userId);
}
