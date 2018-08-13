package com.toure.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.toure.popularmovies.lib.GlideApp;
import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.utils.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String LOG_TAC = DetailActivity.class.getSimpleName();

    public static final String ITEM_ID_KEY = "item_id_key";
    private final int DEFAULT_ITEM_ID = -1;

    // App Database reference
    AppDatabase mDb;
    LiveData<Movie> mMovie;
    int mItemId; // Id of the item selected;

    @BindView(R.id.poster)
    ImageView poster;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.vote_average)
    RatingBar vote_average;
    @BindView(R.id.description_textView)
    TextView description;
    @BindView(R.id.released_date_value)
    TextView releaseDate;
    @BindView(R.id.lanaguage_value)
    TextView language;
    @BindView(R.id.movie_title)
    TextView movieTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDb = AppDatabase.getsInstance(getApplicationContext());
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(ITEM_ID_KEY)) {
            mItemId = getIntent().getIntExtra(ITEM_ID_KEY, DEFAULT_ITEM_ID);
        } else {
            mItemId = savedInstanceState.getInt(ITEM_ID_KEY, DEFAULT_ITEM_ID);
            Log.d(LOG_TAC, "Saved data");
        }

        if (mItemId == DEFAULT_ITEM_ID) {
            finish();
        } else {
            mMovie = mDb.moviesDao().getMovieItemById(mItemId);
            mMovie.observe(this, new Observer<Movie>() {
                @Override
                public void onChanged(@Nullable Movie movie) {
                    populateUI(movie);
                }
            });
        }
    }

    void populateUI(Movie movie) {
        GlideApp.with(this)
                .load(Utility.getBackdropUrl(movie.getBackdropPath()))
                .centerInside()
                .placeholder(R.drawable.placeholder_image)
                .into(backdrop);

        GlideApp.with(this)
                .load(Utility.getPosterUrl(movie.getPosterPath()))
                .centerInside()
                .placeholder(R.drawable.placeholder_image)
                .into(poster);
        setTitle(movie.getTitle());
        movieTitle.setText(movie.getTitle());
        backdrop.setContentDescription(movie.getTitle());
        poster.setContentDescription(movie.getTitle());

        description.setText(movie.getOverview());
        vote_average.setRating((float) movie.getVoteAverage());
        releaseDate.setText(movie.getReleaseDate());
        language.setText(movie.getOriginalLanguage());

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ITEM_ID_KEY, mItemId);
    }
}
