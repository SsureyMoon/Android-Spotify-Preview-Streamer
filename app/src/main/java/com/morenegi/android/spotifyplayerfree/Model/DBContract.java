package com.morenegi.android.spotifyplayerfree.Model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ssureymoon on 7/15/15.
 */
public class DBContract {

    public static final String CONTENT_AUTHORITY = "com.morenegi.android.spotifyplayerfree";

    public static final Uri BASE_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ARTIST = "artist";
    public static final String PATH_TRACK = "track";


    public static final class ArtistTable implements BaseColumns{

        // content uri for the content provider
        public static final Uri CONTENT_URI =
                BASE_URI.buildUpon().appendPath(PATH_ARTIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;

        public static final String TABLE_NAME = "artist";

        public static final String COLUMN_ARTIST_ID = "artist_id";

        public static final String COLUMN_ARTIST_NAME = "artist_name";

        public static final String COLUMN_ARTIST_THUMBNAIL = "artist_thumbnail";

        public static final String COLUMN_SEARCH_KEYWORD = "search_keyword";

        public static final String COLUMN_LAST_UPDATE = "last_update";

        // content://com.morenegi.android.spotifyplayerfree/artist/1
        public static Uri buildArtistUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // content://com.morenegi.android.spotifyplayerfree/artist/coldplay
        public static Uri buildArtistWithKeyword(String keyword) {
            return CONTENT_URI.buildUpon().appendPath(keyword).build();
        }

        public static long getArtistIdFromUri(Uri uri){
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static String getKeywordFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }


    }

    public static final class TrackTable implements BaseColumns{

        // content uri for the content provider
        public static final Uri CONTENT_URI =
                BASE_URI.buildUpon().appendPath(PATH_TRACK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;

        public static final String TABLE_NAME = "track";

        public static final String COLUMN_ALBUM_NAME  = "album_name";

        public static final String COLUMN_TRACK_NAME  = "track_name";

        public static final String COLUMN_TRACK_ID  = "track_id";

        public static final String COLUMN_TRACK_THUMBNAIL = "track_thumbnail";

        public static final String COLUMN_ARTIST_NAME = "artist_name";

        public static final String COLUMN_ARTIST_ID = "artist_id";

        public static final String COLUMN_TRACK_PREVIEW_LINK = "preview_link";

        public static final String COLUMN_LAST_UPDATE = "last_update";

        // content://com.morenegi.android.spotifyplayerfree/track/1
        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // content://com.morenegi.android.spotifyplayerfree/track/1sfddsafsda
        public static Uri buildTrackWithArtistId(String artist){
            return CONTENT_URI.buildUpon().appendPath(artist).build();
        }

        // content://com.morenegi.android.spotifyplayerfree/track/1sfddsafsda/mysong
        public static Uri buildTrackWithArtistIdAndTrackId(String artist, String track){
            return CONTENT_URI.buildUpon().appendPath(artist)
                    .appendPath(COLUMN_TRACK_ID)
                    .appendPath(track).build();
        }

        public static String getTrackIdFromUri(Uri uri){
            return uri.getPathSegments().get(3);
        }

        public static String getArtistIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }
}
