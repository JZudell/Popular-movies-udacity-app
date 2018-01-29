package com.example.computeraccount.newmovieratings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Computeraccount on 9/20/15.
 */
public class MovieFragment extends Fragment {

    private ImageAdapter mMovieAdapter;
    private ArrayList<String> trailerKeys;
    private ArrayList<String> reviewUrls;
    ArrayList<MovieObject> faveMovies = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            try {
                updateMovies();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        mMovieAdapter = new ImageAdapter(getActivity());
        trailerKeys = new ArrayList<>();
        reviewUrls = new ArrayList<>();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieObject movie = (MovieObject) mMovieAdapter.getItem(position);

                try {
                    updateTrailers(String.valueOf(movie.getMovieId()));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    updateReviews(String.valueOf(movie.getMovieId()));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Bundle extras = new Bundle();

                extras.putStringArrayList("EXTRA_REVIEWS", reviewUrls);
                extras.putStringArrayList("EXTRA_TRAILERS", trailerKeys);
                extras.putInt("EXTRA_ID", movie.getMovieId());
                extras.putString("EXTRA_TITLE", movie.getOriginalTitle());
                extras.putString("EXTRA_POSTER_URL", movie.getPosterPath());
                extras.putString("EXTRA_OVERVIEW", movie.getOverview());
                extras.putString("EXTRA_RELEASE_DATE", movie.getReleaseDate());
                extras.putDouble("EXTRA_POPULARITY", movie.getPopularity());
                extras.putDouble("EXTRA_VOTE_AVERAGE", movie.getVoteAverage());
                if (MainActivity.isTwoPane()) {
                    ((MainActivity) getActivity()).replaceFragment(extras);
                } else {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    private void updateMovies() throws ExecutionException, InterruptedException {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortType = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        if (sortType.matches("favorites")) {
            faveMovies.clear();

            SharedPreferences faves = getActivity().getSharedPreferences("FavoritesIdKey", Context.MODE_PRIVATE);
            Map<String, ?> userData2 = faves.getAll();
            for (Map.Entry<String, ?> entry : userData2.entrySet()) {
                String movieId = (String) entry.getValue();
                FetchFavoriteTask favoriteTask = new FetchFavoriteTask();

                favoriteTask.execute(movieId).get();
            }

            Collection x = faveMovies;
            mMovieAdapter.addAll(x);

        } else {

            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute(sortType);
        }

    }

    private void updateTrailers(String movieId) throws ExecutionException, InterruptedException {
        FetchTrailerTask trailerTask = new FetchTrailerTask();

        trailerTask.execute(movieId).get();

    }

    private void updateReviews(String movieId) throws ExecutionException, InterruptedException {
        FetchReviewTask reviewTask = new FetchReviewTask();
        reviewTask.execute(movieId).get();
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            updateMovies();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Collection<MovieObject>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private Collection<MovieObject> getMovieDataFromJson(String movieDataReturned)
                throws JSONException {

            ArrayList<MovieObject> movieObjectArray = new ArrayList<>();

            final String OWM_LIST = "results";
            final String OWN_MOVIEID = "id";
            final String OWM_TITLE = "original_title";
            final String OWM_POSTERPATH = "poster_path";
            final String OWM_OVERVIEW = "overview";
            final String OWM_VOTEAVERAGE = "vote_average";
            final String OWM_POPULARITY = "popularity";
            final String OWM_RELEASEDATE = "release_date";

            JSONObject movieDataJson = new JSONObject(movieDataReturned);
            JSONArray movieArray = movieDataJson.getJSONArray(OWM_LIST);

            for (int i = 0; i < movieArray.length(); i++) {

                int movieId;
                String title;
                String posterPath;
                String overview;
                double voteAverage;
                double popularity;
                String releaseDate;

                JSONObject movie = movieArray.getJSONObject(i);

                movieId = movie.getInt(OWN_MOVIEID);
                title = movie.getString(OWM_TITLE);
                String x = movie.getString(OWM_POSTERPATH);
                if (x == "null") {
                    posterPath = "https://cdn.amctheatres.com/Media/Default/Images/noposter.jpg";
                } else {
                    posterPath = pathToUrl(x);
                }
                overview = movie.getString(OWM_OVERVIEW);
                voteAverage = movie.getDouble(OWM_VOTEAVERAGE);
                popularity = movie.getDouble(OWM_POPULARITY);
                releaseDate = movie.getString(OWM_RELEASEDATE);
                movieObjectArray.add(new MovieObject(movieId, title, posterPath, overview, voteAverage, popularity, releaseDate));

            }

            return movieObjectArray;
        }

        private String pathToUrl(String posterPath) {

            String baseUrlString = "http://image.tmdb.org/t/p/";
            String pictureSizeUrlCode = "w342";
            String urlString = baseUrlString + pictureSizeUrlCode + posterPath;

            return urlString;
        }

        @Override
        protected Collection<MovieObject> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieDataReturned = null;

            try {
                String startUrl = "http://api.themoviedb.org/3/discover/movie?sort_by=";
                String sortTypeUrl = params[0];
                String endUrl = "&page=1&";
                String apiKeyPrefix = "api_key=";

                URL url = new URL(startUrl + sortTypeUrl + endUrl + apiKeyPrefix + BuildConfig.OPEN_MOVIE_DATABASE_API_KEY);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                    return null;
                }
                movieDataReturned = buffer.toString();

            } catch (IOException e) {
                Log.e("MovieFragment", "Error ", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                Collection<MovieObject> x = getMovieDataFromJson((movieDataReturned));
                return x;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Collection<MovieObject> movieObjects) {
            if (movieObjects != null) {

                mMovieAdapter.addAll(movieObjects);

            }

        }

    }

    public class FetchTrailerTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        private ArrayList<String> getTrailerDataFromJSON(String trailerDataReturned) throws JSONException {
            JSONObject trailerDataJson = new JSONObject(trailerDataReturned);
            JSONArray trailerArray = trailerDataJson.getJSONArray("results");
            ArrayList<String> trailers = new ArrayList<>();

            for (int x = 0; x < trailerArray.length(); x++) {
                JSONObject trailer = trailerArray.getJSONObject(x);
                trailers.add(trailer.getString("key"));

            }
            return trailers;
        }

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailerJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String baseUrl = "http://api.themoviedb.org/3/movie/";
                String movieKey = params[0];
                String apiKeyPrefix = "/videos?api_key=";
                URL url = new URL(baseUrl.concat(movieKey).concat(apiKeyPrefix).concat(BuildConfig.OPEN_MOVIE_DATABASE_API_KEY));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                ArrayList<String> g = getTrailerDataFromJSON(trailerJsonStr);
                trailerKeys.clear();
                for (int e = 0; e < g.size(); e++) {
                    trailerKeys.add(g.get(e));
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
    }

    public class FetchReviewTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

        private ArrayList<String> getReviewDataFromJSON(String reviewDataReturned) throws JSONException {
            JSONObject reviewDataJson = new JSONObject(reviewDataReturned);
            JSONArray reviewArray = reviewDataJson.getJSONArray("results");
            ArrayList<String> reviews = new ArrayList<>();

            for (int x = 0; x < reviewArray.length(); x++) {
                JSONObject trailer = reviewArray.getJSONObject(x);
                reviews.add(trailer.getString("url"));

            }
            return reviews;
        }

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String reviewJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String baseUrl = "http://api.themoviedb.org/3/movie/";
                String movieKey = params[0];
                String apiPrefix = "/reviews?api_key=";
                URL url = new URL(baseUrl.concat(movieKey).concat(apiPrefix).concat(BuildConfig.OPEN_MOVIE_DATABASE_API_KEY));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                reviewJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                ArrayList<String> g = getReviewDataFromJSON(reviewJsonStr);
                reviewUrls.clear();
                for (int e = 0; e < g.size(); e++) {
                    reviewUrls.add(g.get(e));
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
    }

    public class FetchFavoriteTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchFavoriteTask.class.getSimpleName();

        private MovieObject getFavoriteMovieFromJsonData(String data) throws JSONException {

            int movieId;
            String title;
            String posterPath;
            String posterPathFinal;
            String overview;
            double voteAverage;
            double popularity;
            String releaseDate;

            JSONObject DataJson = new JSONObject(data);

            movieId = DataJson.getInt("id");
            title = DataJson.getString("original_title");
            posterPath = DataJson.getString("poster_path");
            overview = DataJson.getString("overview");
            voteAverage = DataJson.getDouble("vote_average");
            popularity = DataJson.getDouble("popularity");
            releaseDate = DataJson.getString("release_date");

            if (posterPath == "null") {
                posterPathFinal = "https://cdn.amctheatres.com/Media/Default/Images/noposter.jpg";
            } else {
                posterPathFinal = pathToUrl(posterPath);
            }

            return new MovieObject(movieId, title, posterPathFinal, overview, voteAverage, popularity, releaseDate);

        }

        private String pathToUrl(String posterPath) {

            String baseUrlString = "http://image.tmdb.org/t/p/";
            String pictureSizeUrlCode = "w342";
            String urlString = baseUrlString + pictureSizeUrlCode + posterPath;

            return urlString;
        }

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String favoriteJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String baseUrl = "http://api.themoviedb.org/3/movie/";
                String movieIdForUrl = params[0];
                String apiKey = "?api_key=";
                URL url = new URL(baseUrl.concat(movieIdForUrl).concat(apiKey).concat(BuildConfig.OPEN_MOVIE_DATABASE_API_KEY));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                favoriteJsonStr = buffer.toString();
                faveMovies.add(getFavoriteMovieFromJsonData(favoriteJsonStr));

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }


    }

}
