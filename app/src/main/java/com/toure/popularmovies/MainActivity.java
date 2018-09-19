package com.toure.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.toure.popularmovies.adapter.MoviesAdapter;
import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.utils.Utility;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, ItemOnClickHandler {

    public static final String LOG_TAC = MainActivity.class.getSimpleName();

    RecyclerView mMovieRecyclerView;
    SharedPreferences mSharedPref;
    // Index from which pagination should start (1 is the first here)
    public static final int PAGE_START = 1;
    RequestQueue mRequestQueue;
    // LiveData object for the list of movies to be displayed on the main activity
    LiveData<List<Movie>> mMovies;
    private MoviesAdapter mMovieAdapter;
    // Member variable for the database
    private AppDatabase mDb;
    ProgressBar progressBar;
    // Indicates if footer ProgressBar is shown (i.e. next page is loading)
    private boolean isLoading = false;
    // If current page is the last page (Pagination will stop after this page load)
    private boolean isLastPage = false;
    // total no. of pages to load.
    private int TOTAL_PAGES;
    // indicates the current page which Pagination is fetching.
    private int currentPage = PAGE_START;

    MainActivityViewModel viewModel;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.pref_movie, false);

        mDb = AppDatabase.getsInstance(getApplicationContext());

        mMovieRecyclerView = findViewById(R.id.movie_recyclerview);
        progressBar = findViewById(R.id.initial_progressbar);

        // improve performance since change in content do not change the layout size of the RecyclerView
        mMovieRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MoviesAdapter(this, this);
        mMovieRecyclerView.setAdapter(mMovieAdapter);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        TOTAL_PAGES = mSharedPref.getInt(getString(R.string.total_pages_key), currentPage);
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt(getString(R.string.current_page_key), PAGE_START);
        }
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        //mMovies = viewModel.getmMovies();
        if (Utility.isSortMostPopular(this)) {
            // get the movies sort by the popularity field
            mMovies = mDb.moviesDao().getPopularItems();
            Utility.getPopularMovies(this, PAGE_START); // Network request to get popular movies
        } else if (Utility.isSortTopRated(this)) {
            // Get the movies sorted by the "top rated" field
            mMovies = mDb.moviesDao().getTopRatedItems();
            Utility.getTopRatedMovies(this, PAGE_START); // network request to get the top rated movies
        } else {
            mMovies = mDb.moviesDao().getFavouriteItems();
        }

        Log.d(LOG_TAC, "Getting data from viewModel");
        mMovies.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                if (!movies.isEmpty())
                    progressBar.setVisibility(View.GONE);

                mMovieAdapter.removeLoadingFooter();
                isLoading = false;

                mMovieAdapter.setMovies(movies);
            }
        });
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        mMovieRecyclerView.addOnScrollListener(new PaginationScrollListener((GridLayoutManager) mMovieRecyclerView.getLayoutManager()) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1; //Increment page index to load the next one
                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        // Initialise  our admob
        MobileAds.initialize(this, BuildConfig.admob_app_ID);
        mInterstitialAd = new InterstitialAd(this);
        //mInterstitialAd.setAdUnitId(BuildConfig.admob_full_screen_ads_unit);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
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
            if (Utility.isSortMostPopular(this.getApplication())) {
                // get the movies sort by the popularity field
                mMovies = mDb.moviesDao().getPopularItems();
            } else if (Utility.isSortTopRated(this.getApplication())) {
                // Get the movies sorted by the "top rated" field
                mMovies = mDb.moviesDao().getTopRatedItems();
            } else {
                mMovies = mDb.moviesDao().getFavouriteItems();
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

        if (key.equals(getString(R.string.total_pages_key))) {
            TOTAL_PAGES = sharedPreferences.getInt(key, currentPage);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortOrder = mSharedPref.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_sort_order_most_popular_value));
        Log.d(LOG_TAC, sortOrder);
        setTitle(sortOrder.substring(0, 1).toUpperCase() + sortOrder.substring(1));
        Log.d(LOG_TAC, "total:" + TOTAL_PAGES);
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
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d(LOG_TAC, "The interstitial wasn't loaded yet.");
        }
        startActivity(intent);
    }

    void loadNextPage() {
        Log.d("scrolling", "current: " + currentPage + " totalpage: " + TOTAL_PAGES);
        if (currentPage <= TOTAL_PAGES) {
            mMovieAdapter.addLoadingFooter();
            if (Utility.isSortMostPopular(this)) {
                Utility.getPopularMovies(this, currentPage); // Network request to get popular movies
            } else {
                Utility.getTopRatedMovies(this, currentPage); // Network request to get top rated movies
            }
        } else isLastPage = true;

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(getString(R.string.current_page_key), currentPage);
    }
}
