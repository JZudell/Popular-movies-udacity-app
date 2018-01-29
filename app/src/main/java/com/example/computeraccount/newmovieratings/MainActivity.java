package com.example.computeraccount.newmovieratings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static boolean mTwoPane;
    static int faveMovieId;
    static String faveMovieString;
    static Boolean movieInFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DummyFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

    }

    public static boolean isTwoPane() {
        return mTwoPane;

    }

    public void replaceFragment(Bundle bundle) {
        DetailActivity.PlaceholderFragment fragment = new DetailActivity.PlaceholderFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void returnFaveMovieId(int faveId) {
        faveMovieId = faveId;
    }

    public static void returnFaveMovieStr(String faveMovieStr) {
        faveMovieString = faveMovieStr;
    }

    public static void returnIfMovieInFavorites(Boolean ismovieInFavorites) {
        movieInFavorites = ismovieInFavorites;
    }

    public void addToFavorites2(View view) {


        int movieId = faveMovieId;
        SharedPreferences settings = getSharedPreferences("FavoritesIdKey", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (movieInFavorites) {
            editor.remove(faveMovieString);
            Toast toast = Toast.makeText(getApplicationContext(), "Removed from Favorites",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            movieInFavorites = false;
        } else {
            editor.putString(faveMovieString, String.valueOf(movieId));
            Toast toast = Toast.makeText(getApplicationContext(), "Added to Favorites",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            movieInFavorites = true;

        }
        editor.commit();

    }

}

