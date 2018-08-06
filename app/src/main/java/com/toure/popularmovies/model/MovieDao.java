package com.toure.popularmovies.model;

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
    List<Movie> getAllItems();

    @Query("SELECT * FROM movie ORDER BY vote_count DESC")
    List<Movie> getTopRatedItems();

    @Query("SELECT * FROM movie ORDER BY popularity DESC")
    List<Movie> getPopularItems();

    @Query("SELECT * FROM movie WHERE id = :id")
    Movie getMoveiItemById( int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Movie> items);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Movie item);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Movie item);

    @Delete
    void delete(Movie item);
}
