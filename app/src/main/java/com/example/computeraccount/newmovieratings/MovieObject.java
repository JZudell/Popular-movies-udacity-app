package com.example.computeraccount.newmovieratings;

/**
 * Created by Computeraccount on 9/23/15.
 */
public class MovieObject {
    public int movieId;
    public String originalTitle;
    public String posterPath;
    public String overview;
    public double voteAverage;
    public double popularity;
    public String releaseDate;

    public MovieObject(int movieId
            ,String originalTitle
            ,String posterPath
            ,String overview
            ,double voteAverage
            ,double popularity
            ,String releaseDate){
        this.movieId = movieId;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.popularity = popularity;
        this.releaseDate = releaseDate;
    }
    public int getMovieId(){ return movieId;}

    public String getPosterPath(){ return posterPath;}

    public String getOriginalTitle() { return originalTitle;}

    public String getOverview(){ return overview;}

    public String getReleaseDate(){ return releaseDate;}

    public double getVoteAverage(){ return voteAverage;}

    public double getPopularity(){ return popularity;}

}
