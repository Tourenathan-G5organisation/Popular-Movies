package com.toure.popularmovies.model;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie")
    LiveData<List<Movie>> getAllItems();

    @Query("SELECT * FROM movie ORDER BY vote_count DESC")
    LiveData<List<Movie>> getTopRatedItems();

    @Query("SELECT * FROM movie ORDER BY popularity DESC")
    LiveData<List<Movie>> getPopularItems();

    @Query("SELECT * FROM movie WHERE id = :id")
    LiveData<Movie> getMovieItemById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Movie> items);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Movie item);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Movie item);

    @Delete
    void delete(Movie item);

    @Query("DELETE  FROM movie")
    void deleteAllMovies();
}
