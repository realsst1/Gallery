package com.example.gallery.Retrofit;

import com.example.gallery.Models.PhotoResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Flickr {

     String API_KEY = "6f102c62f41998d151e5a1b48713cf13";
    //String API_KEY="550cb6dbd15c7fc3ee0bfb6a769b26cb";
     String EXTRA_SMALL_URL = "url_s";

    @GET("rest/")
    Observable<PhotoResult> getRecentResult(@Query("method") String method,
                                            @Query("api_key") String API_KEY,
                                            @Query("extras") String EXTRA_SMALL_URL,
                                            @Query("format") String format,
                                            @Query("nojsoncallback") String nojsoncallback);

    @GET("rest/")
    Observable<PhotoResult> getSearchResult(@Query("method") String method,
                                            @Query("api_key") String API_KEY,
                                            @Query("extras") String EXTRA_SMALL_URL,
                                            @Query("format") String format,
                                            @Query("nojsoncallback") String nojsoncallback,
                                            @Query("text") String text);
}
