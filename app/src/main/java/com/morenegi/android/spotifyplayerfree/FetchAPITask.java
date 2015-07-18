package com.morenegi.android.spotifyplayerfree;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.morenegi.android.spotifyplayerfree.Model.DBContract.ArtistTable;
import com.morenegi.android.spotifyplayerfree.Model.DBContract.TrackTable;
import com.morenegi.android.spotifyplayerfree.Model.DBProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * Created by ssureymoon on 7/15/15.
 */
public class FetchAPITask extends AsyncTask<String, Void, Void>{

    private String LOG_TAG = FetchAPITask.class.getSimpleName();
    private ArrayAdapter<String> mForecastAdapter;
    private final Context mContext;

    ArrayList<String> artistsArray;

    public static final String FETCH_ARTISTS = "ARTISTS";
    public static final String FETCH_TRACKS = "TRACKS";

    public FetchAPITask(Context context){
        mContext = context;
    }


    @Override
    protected Void doInBackground(String... params) {

        //api.setAccessToken("myAccessToken");

        try {
            switch(params[0]){
                case (FETCH_ARTISTS):{
                    fetchArtists(params[1]);
                }
                case (FETCH_TRACKS):{
                    fetchTracks(params[1]);
                }
            }

        } catch(Exception e){
            Log.e(LOG_TAG, e.getStackTrace().toString());
        }

        return null;
    }
    private void fetchArtists(String keyword){
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        ArtistsPager results = spotify.searchArtists(keyword);
        List<Artist> artists = results.artists.items;

        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(artists.size());

        for (Artist elem : artists) {

            String artistName = elem.name;
            String imageUri = null;
            if (elem.images.size() > 0) {
                imageUri = ((Image) elem.images.get(0)).url;
            }
            ContentValues artistValues = new ContentValues();
            artistValues.put(ArtistTable.COLUMN_ARTIST_ID, elem.id);
            artistValues.put(ArtistTable.COLUMN_ARTIST_NAME, elem.name);
            artistValues.put(ArtistTable.COLUMN_ARTIST_THUMBNAIL, imageUri);
            artistValues.put(ArtistTable.COLUMN_LAST_UPDATE, System.currentTimeMillis()*1000);
            artistValues.put(ArtistTable.COLUMN_SEARCH_KEYWORD, keyword);

            cVVector.add(artistValues);
        }

        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(ArtistTable.CONTENT_URI,
                    cvArray);
            // delete old data so we don't build up an endless history
//                context.getContentResolver().delete(DBContract.ArtistTable.CONTENT_URI,
//                        DBContract.ArtistTable.COLUMN_DATE + " <= ?",
//                        new String[] {System.currentMillis() - 36000});
        }


    }

    private void fetchTracks(String keyword){
        if(DBProvider.doesKeywordSetExistInArtistDB(TrackTable.buildTrackWithArtistId(keyword)))
            return;
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        Map<String, String> queryOptions = new HashMap<String, String>();
        queryOptions.put("country", "US");
        Tracks result = spotify.getArtistTopTrack(keyword, (Map) queryOptions);
        List<Track> tracks = result.tracks;

        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(tracks.size());

        for (Track elem : tracks) {


            String albumName = elem.album.name;
            String imageUri = null;
            String trackName = elem.name;
            String artistId = keyword;
            String trackId = elem.id;
            String artistName = elem.artists.get(0).name;
            String previewLink = elem.preview_url;
            long lastUpdate = System.currentTimeMillis()*1000;

            if (elem.album.images.size() > 0) {
                imageUri = ((Image) elem.album.images.get(0)).url;
            }
            ContentValues trackValues = new ContentValues();
            trackValues.put(TrackTable.COLUMN_ALBUM_NAME, albumName);
            trackValues.put(TrackTable.COLUMN_ARTIST_ID, artistId);
            trackValues.put(TrackTable.COLUMN_ARTIST_NAME, artistName);
            trackValues.put(TrackTable.COLUMN_TRACK_ID, trackId);
            trackValues.put(TrackTable.COLUMN_TRACK_NAME, trackName);
            trackValues.put(TrackTable.COLUMN_TRACK_PREVIEW_LINK, previewLink);
            trackValues.put(TrackTable.COLUMN_TRACK_THUMBNAIL, imageUri);
            trackValues.put(TrackTable.COLUMN_LAST_UPDATE, lastUpdate);

            cVVector.add(trackValues);
        }

        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(TrackTable.CONTENT_URI,
                    cvArray);
            // delete old data so we don't build up an endless history
//                context.getContentResolver().delete(DBContract.TracksTable.CONTENT_URI,
//                        DBContract.TracksTable.LAST_UPDATE + " <= ?",
//                        new String[] {System.currentMillis() - 36000});
        }


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
