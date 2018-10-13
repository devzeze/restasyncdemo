package com.devzeze.restasyncdemo.utils;

import com.devzeze.restasyncdemo.model.Posts;
import com.devzeze.restasyncdemo.model.Todos;
import com.devzeze.restasyncdemo.model.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiPromisesService {

    @GET("users")
    Call<List<Users>> fetchAllUsers();

    @GET("todos")
    Call<List<Todos>> fetchAllTodos();


    @GET("posts")
    Call<List<Posts>> fetchPostersByUser(@Query("userId") long userId);
}
