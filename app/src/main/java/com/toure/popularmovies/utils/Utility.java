package com.toure.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
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

    private static final String LOG_TAG = Utility.class.getSimpleName();

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
    private static void getMoviesOnline(final Context context, Call<MovieApiResponse> call) {
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

    /**
     * Determine if the sort order is most popular or top rated
     *
     * @param context
     * @return boolean value indicating the sort order. 'True' if it 'most popular' and 'false' if ist 'top rated'
     */
    public static boolean isSortMostPopular(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = sharedPref.getString(context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_most_popular_value));
        // Return true if the sort order is "most popular" and false if its "top rated"
        return sortOrder.equals(context.getString(R.string.pref_sort_order_most_popular_value));
    }

    /**
     * Get the poster full link
     *
     * @param imageRelativeLink Relative link to the poster
     * @return
     */
    public static String getPosterUrl(String imageRelativeLink) {
        return "http://image.tmdb.org/t/p/w185/" + imageRelativeLink;
    }

    /**
     * Get the backdrop full link
     *
     * @param imageRelativeLink Relative link to the backdrop
     * @return
     */
    public static String getBackdropUrl(String imageRelativeLink) {
        return "http://image.tmdb.org/t/p/w500/" + imageRelativeLink;
    }
}
