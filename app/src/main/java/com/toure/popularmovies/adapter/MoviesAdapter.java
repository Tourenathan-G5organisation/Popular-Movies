package com.toure.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.toure.popularmovies.R;
import com.toure.popularmovies.lib.GlideApp;
import com.toure.popularmovies.model.Movie;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    static final String LOG_TAC = MoviesAdapter.class.getSimpleName();

    private List<Movie> mMovieItems;
    private Context mContext;

    public MoviesAdapter(Context context) {
        mContext = context;
    }

    /**
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    /**
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = mMovieItems.get(position);
        GlideApp.with(mContext)
                .load(getThumbnailUrl(movie.getPosterPath()))
                .centerInside()
                .placeholder(R.drawable.placeholder_image)
                .into(holder.mItemImageView);
        holder.mItemImageView.setContentDescription(movie.getTitle());

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return (mMovieItems == null) ? 0 : mMovieItems.size();
    }

    /**
     * Set the movies to be displayed
     *
     * @param moviesItems Movies list to be displayed
     */
    public void setMovies(List<Movie> moviesItems) {
        mMovieItems = moviesItems;
        notifyDataSetChanged();
    }

    /**
     * Add movies to the already existing list
     *
     * @param moviesItems Movies list to be added to the already existing list
     */
    void addMovies(List<Movie> moviesItems) {
        if (mMovieItems != null) {
            mMovieItems.addAll(moviesItems);
        } else {
            mMovieItems = moviesItems;
        }
        notifyDataSetChanged();
    }

    private String getThumbnailUrl(String imageRelativeLink) {
        /*final String IMAGE_SIZE = "w185";
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(IMAGE_SIZE)
                .appendPath(imageRelativeLink)
                .build();*/
        return "http://image.tmdb.org/t/p/w185/" + imageRelativeLink;
    }


    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mItemImageView;
        public ViewHolder(View v) {
            super(v);
            mItemImageView = v.findViewById(R.id.movie_list_item_imageview);
        }
    }

}
