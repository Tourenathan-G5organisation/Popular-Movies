package com.toure.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.toure.popularmovies.BuildConfig;
import com.toure.popularmovies.MainActivity;
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
    public static void getTopRatedMovies(Context context, int page) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<MovieApiResponse> call = apiService.getTopRatedMovies(BuildConfig.themoviedb_api_key, page);

        getMoviesOnline(context, call, page);
    }

    /**
     * Get the most popular movies online
     */
    public static void getPopularMovies(Context context, int page) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<MovieApiResponse> call = apiService.getPopularMovies(BuildConfig.themoviedb_api_key, page);

        getMoviesOnline(context, call, page);
    }

    /**
     * Launch the request to fet the movies online
     * @param call
     */
    private static void getMoviesOnline(final Context context, Call<MovieApiResponse> call, final int page) {
        call.enqueue(new Callback<MovieApiResponse>() {
            @Override
            public void onResponse(Call<MovieApiResponse> call, retrofit2.Response<MovieApiResponse> response) {
                Log.d(LOG_TAG, response.body().toString());
                final List<Movie> movies = response.body().getResults();
                Log.d(LOG_TAG, "Number of movies received: " + movies.size());
                if (page == MainActivity.PAGE_START) {
                    savePageInfo(context, response.body().getTotalPages(), response.body().getPage());
                }
                final AppDatabase mDb = AppDatabase.getsInstance(context.getApplicationContext());
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (page == MainActivity.PAGE_START) {
                            mDb.moviesDao().deleteAllMovies();
                        }
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
     * Determine if the sort order is most popular
     *
     * @param context
     * @return boolean value indicating the sort order. 'True' if it 'most popular' and 'false' if ist 'top rated'
     */
    public static boolean isSortMostPopular(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = sharedPref.getString(context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_most_popular_value));
        // Return true if the sort order is "most popular" and false if its NOT
        return sortOrder.equals(context.getString(R.string.pref_sort_order_most_popular_value));
    }

    /**
     * Determine if the sort order is top rated
     *
     * @param context
     * @return
     */
    public static boolean isSortTopRated(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = sharedPref.getString(context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_most_popular_value));
        // Return true if the sort order is "top rated" and false if its NOT
        return sortOrder.equals(context.getString(R.string.pref_sort_order_top_rated_value));
    }

    /**
     * Check if the sort order is favourite
     *
     * @param context
     * @return
     */
    public static boolean isSortFavourite(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = sharedPref.getString(context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_most_popular_value));
        // Return true if the sort order is "favourite" and false if its NOT
        return sortOrder.equals(context.getString(R.string.pref_sort_order_favourite_value));
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

    /**
     * Save the page info number
     *
     * @param context
     * @param totalPages
     */
    private static void savePageInfo(Context context, int totalPages, int currentPage) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putInt(context.getString(R.string.total_pages_key), totalPages)
                .putInt(context.getString(R.string.current_page_key), currentPage)
                .apply();
    }

    /**
     * Check internet connectivity
     *
     * @param context
     * @return
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
