package com.toure.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.Movie;

public class DetailViewModel extends ViewModel {
    private static final String LOG_TAG = DetailViewModel.class.getSimpleName();
    private LiveData<Movie> mMovie;
    private AppDatabase mDb;

    public DetailViewModel(AppDatabase mDb, int itemId) {
        mMovie = mDb.moviesDao().getMovieItemById(itemId);

        Log.d(LOG_TAG, "Retrieving data from the database");
    }

    public LiveData<Movie> getmMovie() {
        return mMovie;
    }
}
