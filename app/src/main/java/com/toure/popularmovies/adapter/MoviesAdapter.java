package com.toure.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.toure.popularmovies.ItemOnClickHandler;
import com.toure.popularmovies.R;
import com.toure.popularmovies.lib.GlideApp;
import com.toure.popularmovies.model.Movie;
import com.toure.popularmovies.utils.Utility;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    static final String LOG_TAC = MoviesAdapter.class.getSimpleName();

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private boolean isLoadingAdded = false; // Determine if the loading bar is displayed or not

    private List<Movie> mMovieItems;
    private Context mContext;
    private final ItemOnClickHandler mClickHandler;

    public MoviesAdapter(Context context, ItemOnClickHandler itemOnClickHandler) {
        mContext = context;
        mClickHandler = itemOnClickHandler;
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
        int layoutIdForListItem = 0;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        switch (viewType) {
            case ITEM:
                layoutIdForListItem = R.layout.movie_list_item;
                break;
            case LOADING:
                layoutIdForListItem = R.layout.movie_progress_item;
                break;
        }
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
        switch (getItemViewType(position)) {
            case ITEM:
                GlideApp.with(mContext)
                        .load(Utility.getPosterUrl(movie.getPosterPath()))
                        .centerInside()
                        .placeholder(R.drawable.placeholder_image)
                        .into(holder.mItemImageView);
                holder.mItemImageView.setContentDescription(movie.getTitle());
                break;
        }


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

    @Override
    public int getItemViewType(int position) {
        // Display the loading if the item is the last item or the item detils or otherwise
        return (position == mMovieItems.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
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


    public void addMovie(Movie movie) {
        mMovieItems.add(movie);
        notifyItemInserted(mMovieItems.size() - 1);
    }

    public void remove(Movie movie) {
        int position = mMovieItems.indexOf(movie);
        if (position > -1) {
            mMovieItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        addMovie(new Movie());
    }

    // Removes the loading progress bar
    public void removeLoadingFooter() {
        isLoadingAdded = false;

        if (mMovieItems != null && mMovieItems.size() > 0) {
            int position = mMovieItems.size() - 1;
            Movie item = getItem(position);

            if (item != null) {
                mMovieItems.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * Get item at a specified position from the adapter data set
     *
     * @param position
     * @return
     */
    public Movie getItem(int position) {
        return mMovieItems.get(position);
    }

    // Provide a reference to the views for each data item
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mItemImageView;

        public ViewHolder(View v) {
            super(v);
            mItemImageView = v.findViewById(R.id.movie_list_item_imageview);
            if (mItemImageView != null) {
                v.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mClickHandler.onClick(mMovieItems.get(position).getId());
        }
    }

}
