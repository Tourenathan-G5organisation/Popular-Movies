package com.toure.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toure.popularmovies.lib.GlideApp;
import com.toure.popularmovies.model.AppDatabase;
import com.toure.popularmovies.model.AppExecutors;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.model.MovieReview;
import com.toure.popularmovies.model.MovieReviewResponse;
import com.toure.popularmovies.model.MovieTrailer;
import com.toure.popularmovies.model.MovieTrailerResponse;
import com.toure.popularmovies.rest.ApiClient;
import com.toure.popularmovies.rest.ApiInterface;
import com.toure.popularmovies.utils.Utility;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    public static final String LOG_TAC = DetailActivity.class.getSimpleName();

    public static final String ITEM_ID_KEY = "item_id_key";
    private final int DEFAULT_ITEM_ID = -1;

    // App Database reference
    AppDatabase mDb;
    @BindView(R.id.vote_average)
    TextView vote_average;

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
    @BindView(R.id.reviewsLayout)
    LinearLayout mReviewsLinearLayout;
    @BindView(R.id.trailersLinearLayout)
    LinearLayout mTrailerLinearLayout;

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
            DetailViewModelFactory factory = new DetailViewModelFactory(mDb, mItemId);
            DetailViewModel viewModel =
                    ViewModelProviders.of(this, factory).get(DetailViewModel.class);
            viewModel.getmMovie().observe(this, new Observer<Movie>() {
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

        getMoviesReview();
        getTrailers();
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

    /**
     * Make a network request to gete the Movie review details
     */
    void getMoviesReview() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        if (Utility.isOnline(this)) {
            Call<MovieReviewResponse> call = apiService.getMovieReviews(mItemId, BuildConfig.themoviedb_api_key);
            call.enqueue(new Callback<MovieReviewResponse>() {
                @Override
                public void onResponse(Call<MovieReviewResponse> call, Response<MovieReviewResponse> response) {
                    Log.d(LOG_TAC, response.body().toString());
                    final List<MovieReview> moviesReviews = response.body().getResults();
                    Log.d(LOG_TAC, "Number of  reviewed: " + moviesReviews.size());
                    if (moviesReviews.size() > 0) {
                        mReviewsLinearLayout.setVisibility(View.VISIBLE);
                        for (int i = 0; i < moviesReviews.size(); i++) {
                            MovieReview review = moviesReviews.get(i);
                            mReviewsLinearLayout.addView(getNewReview(review.getAuthor(), review.getContent()), LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieReviewResponse> call, Throwable t) {
                    Log.e(LOG_TAC, t.getMessage());
                }
            });
        }
    }

    /**
     * Get the view which will display the review
     *
     * @param authorName review author name
     * @param comments   Review text
     * @return The view inflated
     */
    View getNewReview(String authorName, String comments) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.movie_review_item, mReviewsLinearLayout, false);
        TextView nameTextView = view.findViewById(R.id.reviewerName);
        TextView commentTextView = view.findViewById(R.id.reviewText);
        nameTextView.setText(authorName);
        commentTextView.setText(comments);
        return view;
    }

    void getTrailers() {
        if (Utility.isOnline(this)) {
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            Call<MovieTrailerResponse> call = apiService.getMovieTrailers(mItemId, BuildConfig.themoviedb_api_key);
            call.enqueue(new Callback<MovieTrailerResponse>() {
                @Override
                public void onResponse(Call<MovieTrailerResponse> call, Response<MovieTrailerResponse> response) {
                    Log.d(LOG_TAC, "Number of trailers:" + response.body().getResults().size());
                    List<MovieTrailer> trailers = response.body().getResults();
                    if (trailers.size() > 0) {
                        mTrailerLinearLayout.setVisibility(View.VISIBLE);
                        for (int i = 0; i < trailers.size(); i++) {
                            mTrailerLinearLayout.addView(getNewTrailerView(trailers.get(i).getKey(), trailers.get(i).getName()), LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieTrailerResponse> call, Throwable t) {
                    Log.e(LOG_TAC, t.getMessage());
                }
            });
        }
    }

    View getNewTrailerView(final String videoId, String name) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.movie_trailer_item, mTrailerLinearLayout, false);
        ImageView thumbnail = view.findViewById(R.id.trailerThumbnail);
        thumbnail.setContentDescription(name);
        GlideApp.with(this)
                .load("http://img.youtube.com/vi/" + videoId + "/0.jpg")
                .centerInside()
                .placeholder(R.drawable.placeholder_image)
                .into(thumbnail);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                try {
                    startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    startActivity(webIntent);
                }
            }
        });
        return view;
    }
}
