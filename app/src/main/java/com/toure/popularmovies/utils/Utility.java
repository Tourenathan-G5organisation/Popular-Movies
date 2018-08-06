package com.toure.popularmovies.utils;

import android.content.Context;
import android.util.Log;

import com.toure.popularmovies.R;
import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.AppExecutors;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.model.MovieApiResponse;
import com.toure.popularmovies.rest.ApiClient;
import com.toure.popularmovies.rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class Utility {

    public static final String LOG_TAG = Utility.class.getSimpleName();

    /**
     * Get the top rated movies online
     */
    public static void getTopRatedMovies(Context context) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<MovieApiResponse> call = apiService.getTopRatedMovies(context.getString(R.string.themoviedb_api_key));

        getMoviesOnline(context, call);
    }

    /**
     * Get the most popular movies online
     */
    public static void getPopularMovies(Context context) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<MovieApiResponse> call = apiService.getPopularMovies(context.getString(R.string.themoviedb_api_key));

        getMoviesOnline(context, call);
    }

    /**
     * Launch the request to fet the movies online
     * @param call
     */
    static void getMoviesOnline(final Context context, Call<MovieApiResponse> call) {
        call.enqueue(new Callback<MovieApiResponse>() {
            @Override
            public void onResponse(Call<MovieApiResponse> call, retrofit2.Response<MovieApiResponse> response) {
                Log.d(LOG_TAG, response.body().toString());
                final List<Movie> movies = response.body().getResults();
                Log.d(LOG_TAG, "Number of movies received: " + movies.size());
                final AppDatabase mDb = AppDatabase.getsInstance(context.getApplicationContext());
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.moviesDao().insertAll(movies);
                    }
                });
            }

            @Override
            public void onFailure(Call<MovieApiResponse> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }
}
