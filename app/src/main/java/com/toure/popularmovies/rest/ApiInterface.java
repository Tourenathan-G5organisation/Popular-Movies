package com.toure.popularmovies.rest;

import com.toure.popularmovies.model.MovieApiResponse;
import com.toure.popularmovies.model.MovieReviewResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("movie/top_rated")
    Call<MovieApiResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/popular")
    Call<MovieApiResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/{id}")
    Call<MovieApiResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<MovieReviewResponse> getMovieReviews(@Path("id") int id, @Query("api_key") String apiKey);

    /*@GET("movie/{id}/videos")
    Call<MovieApiResponse> getMovieTrailers(@Path("id") int id, @Query("api_key") String apiKey);*/
}


