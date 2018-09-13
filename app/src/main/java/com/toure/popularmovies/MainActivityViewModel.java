package com.toure.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.utils.Utility;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {
    public static final String LOG_TAG = MainActivityViewModel.class.getSimpleName();
    private LiveData<List<Movie>> mMovies;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        AppDatabase mDb = AppDatabase.getsInstance(this.getApplication());
        if (Utility.isSortMostPopular(this.getApplication())) {
            // get the movies sort by the popularity field
            mMovies = mDb.moviesDao().getPopularItems();
        } else if (Utility.isSortTopRated(this.getApplication())) {
            // Get the movies sorted by the "top rated" field
            mMovies = mDb.moviesDao().getTopRatedItems();
        } else {
            mMovies = mDb.moviesDao().getFavouriteItems();
        }
        Log.d(LOG_TAG, "Retrieving data from the database");
    }

    public LiveData<List<Movie>> getmMovies() {
        return mMovies;
    }
}
