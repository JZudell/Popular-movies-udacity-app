package com.example.computeraccount.newmovieratings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    static ArrayList<String> trailerReviewList;
    static int favoriteMovieId;
    static String favoriteMovieTitle;
    static Boolean movieIsInFavorites = false;

    public void addToFavorites2(View view) {

        int movieId = favoriteMovieId;
        SharedPreferences settings = getSharedPreferences("FavoritesIdKey", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (movieIsInFavorites) {
            editor.remove(favoriteMovieTitle);
            Toast toast = Toast.makeText(getApplicationContext(), "Removed from Favorites",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            movieIsInFavorites = false;
        } else {
            editor.putString(favoriteMovieTitle, String.valueOf(movieId));
            Toast toast = Toast.makeText(getApplicationContext(), "Added to Favorites",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            movieIsInFavorites = true;

        }
        editor.commit();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new PlaceholderFragment())
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_detail, menu);

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

    @SuppressLint("ValidFragment")
    public static class PlaceholderFragment extends Fragment {

        ArrayAdapter<String> mTrailerAdapter;


        final String trailerUItext = "WATCH TRAILER";
        final String reviewUItext = "READ REVIEW";
        final String baseTrailerUrl = "http://www.youtube.com/watch?v=";
        Bundle extras;

        public PlaceholderFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();

            if (MainActivity.mTwoPane) {
                extras = getArguments();
            } else {

                extras = intent.getExtras();

            }

            final ArrayList<String> trailerList = extras.getStringArrayList("EXTRA_TRAILERS");
            final ArrayList<String> reviewList = extras.getStringArrayList("EXTRA_REVIEWS");
            trailerReviewList = new ArrayList<>();

            for (int x = 0; x < trailerList.size(); x++) {
                String g = trailerUItext + " " + (x + 1);
                trailerReviewList.add(g);
            }
            for (int x = 0; x < reviewList.size(); x++) {
                String k = reviewUItext + " " + (x + 1);
                trailerReviewList.add(k);

            }

            mTrailerAdapter =
                    new ArrayAdapter<>(
                            getActivity(),
                            R.layout.trailer_textview,
                            trailerReviewList);

            int id = extras.getInt("EXTRA_ID");
            favoriteMovieId = id;
            String title = extras.getString("EXTRA_TITLE");
            favoriteMovieTitle = title;
            String posterUrl = extras.getString("EXTRA_POSTER_URL");
            String overview = extras.getString("EXTRA_OVERVIEW");
            String releaseDate = extras.getString("EXTRA_RELEASE_DATE");
            double voteAverage = extras.getDouble("EXTRA_VOTE_AVERAGE");

            String x = Double.toString(voteAverage) + "/10";

            ((TextView) rootView.findViewById(R.id.TitleTextView)).setText(title);
            ((TextView) rootView.findViewById(R.id.releasedatetext)).setText(releaseDate);
            ((TextView) rootView.findViewById(R.id.overviewtext)).setText(overview);
            ((TextView) rootView.findViewById(R.id.voteratingview)).setText(x);

            Picasso.with(getActivity()).load(posterUrl).into(((ImageView) rootView.findViewById(R.id.posterThumbnail)));

            ListView listView = (ListView) rootView.findViewById(R.id.trailerlistViewId);
            listView.setAdapter(mTrailerAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String url = null;
                    if (position <= trailerList.size() - 1) {
                        url = baseTrailerUrl + trailerList.get(position);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else {
                        int g = position - trailerList.size();
                        url = reviewList.get(g);
                        WebView webView = (WebView) rootView.findViewById(R.id.web_view);
                        webView.loadUrl(url);
                    }
                }
            });
            SharedPreferences settings = getActivity().getSharedPreferences("FavoritesIdKey", MODE_PRIVATE);
            if (MainActivity.isTwoPane()) {
                MainActivity.returnFaveMovieId(favoriteMovieId);
                MainActivity.returnFaveMovieStr(favoriteMovieTitle);
                MainActivity.returnIfMovieInFavorites(settings.contains(favoriteMovieTitle));
            }

            if (settings.contains(favoriteMovieTitle)) {
                movieIsInFavorites = true;
            }

            return rootView;
        }

    }

}