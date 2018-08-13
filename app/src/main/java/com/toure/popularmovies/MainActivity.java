package com.toure.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.RequestQueue;
import com.toure.popularmovies.adapter.MoviesAdapter;
import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.utils.Utility;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, ItemOnClickHandler {

    public static final String LOG_TAC = MainActivity.class.getSimpleName();

    RecyclerView mMovieRecyclerView;
    SharedPreferences mSharedPref;
    RequestQueue mRequestQueue;
    // LiveData object for the list of movies to be displayed on the main activity
    LiveData<List<Movie>> mMovies;
    private MoviesAdapter mMovieAdapter;
    // Member variable for the database
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.pref_movie, false);

        mDb = AppDatabase.getsInstance(getApplicationContext());

        mMovieRecyclerView = findViewById(R.id.movie_recyclerview);

        // improve performance since change in content do not change the layout size of the RecyclerView
        mMovieRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MoviesAdapter(this, this);
        mMovieRecyclerView.setAdapter(mMovieAdapter);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        if (Utility.isSortMostPopular(this)) {
            // get the movies sort by the popularity field
            mMovies = mDb.moviesDao().getPopularItems();
            Utility.getPopularMovies(this); // Network request to get popular movies
        } else {
            // Get the movies sorted by the "top rated" field
            mMovies = mDb.moviesDao().getTopRatedItems();
            Utility.getTopRatedMovies(this); // network request to get the top rated movies
        }

        mMovies.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                mMovieAdapter.setMovies(movies);
            }
        });
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAC, "key: " + key);
        if (key.equals(getString(R.string.pref_sort_order_key))) {
            if (Utility.isSortMostPopular(this)) {
                mMovies = mDb.moviesDao().getPopularItems();
            } else {
                mMovies = mDb.moviesDao().getTopRatedItems();
            }
            mMovies.observe(this, new Observer<List<Movie>>() {
                @Override
                public void onChanged(@Nullable List<Movie> movies) {
                    mMovieAdapter.setMovies(movies);
                    mMovies.removeObserver(this);
                }
            });
            Log.d(LOG_TAC, "change in");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortOrder = mSharedPref.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_sort_order_most_popular_value));
        Log.d(LOG_TAC, sortOrder);
        setTitle(sortOrder.substring(0, 1).toUpperCase() + sortOrder.substring(1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister the shared preference listener
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Onclick method use to capture the event
     *
     * @param itemId Id of the selected item
     */
    @Override
    public void onClick(int itemId) {
        Log.d(LOG_TAC, "Item clicked: " + itemId);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.ITEM_ID_KEY, itemId);
        startActivity(intent);
    }
}
