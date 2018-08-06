package com.toure.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.RequestQueue;
import com.toure.popularmovies.adapter.MoviesAdapter;
import com.toure.popularmovies.utils.Utility;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAC = MainActivity.class.getSimpleName();

    RecyclerView mMovieRecyclerView;
    private RecyclerView.Adapter mMovieAdapter;
    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMovieRecyclerView = findViewById(R.id.movie_recyclerview);

        // improve performance since change in content do not change the layout size of the RecyclerView
        mMovieRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MoviesAdapter();
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.movie_item_margin);
        mMovieRecyclerView.setAdapter(mMovieAdapter);

        Utility.getTopRatedMovies(this);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
