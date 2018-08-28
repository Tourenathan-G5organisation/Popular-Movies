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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.toure.popularmovies.lib.GlideApp;
import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.AppExecutors;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.utils.Utility;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String LOG_TAC = DetailActivity.class.getSimpleName();

    public static final String ITEM_ID_KEY = "item_id_key";
    private final int DEFAULT_ITEM_ID = -1;

    // App Database reference
    AppDatabase mDb;
    @BindView(R.id.vote_average)
    TextView vote_average;
    private LiveData<Movie> mMovie;

    @BindView(R.id.poster)
    ImageView poster;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    private int mItemId; // Id of the item selected;
    @BindView(R.id.description_textView)
    TextView description;
    @BindView(R.id.released_date_value)
    TextView releaseDate;
    @BindView(R.id.lanaguage_value)
    TextView language;
    @BindView(R.id.movie_title)
    TextView movieTitle;
    @BindView(R.id.favourite)
    ImageView favouriteImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        final Movie[] mmovie = new Movie[1];
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

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
                    mmovie[0] = movie;
                }
            });
        }

        favouriteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mmovie[0].setFavourite(!(mmovie[0].isFavourite()));
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.moviesDao().update(mmovie[0]);
                    }
                });
            }
        });
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
        vote_average.setText(String.format(Locale.ENGLISH, "%.1f/10", movie.getVoteAverage()));
        releaseDate.setText(movie.getReleaseDate());
        language.setText(movie.getOriginalLanguage());

        if (movie.isFavourite())
            favouriteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_600_24dp));
        else
            favouriteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_grey_400_24dp));

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ITEM_ID_KEY, mItemId);
    }
}
