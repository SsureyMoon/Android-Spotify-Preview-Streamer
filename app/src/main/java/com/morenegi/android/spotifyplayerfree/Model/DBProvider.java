package com.morenegi.android.spotifyplayerfree.Model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


/**
 * Created by ssureymoon on 7/16/15.
 */
public class DBProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static DBHelper mOpenHelper;

    static final int ARTIST = 100;
    static final int ARTIST_WITH_KEYWORD = 101;
    static final int TRACK = 200;
    static final int TRACK_WITH_ARTIST_ID = 201;
    static final int TRACK_WITH_ARTIST_ID_AND_TRACK_ID = 202;

    private static final SQLiteQueryBuilder sArtistQueryBuilder;
    static {
        sArtistQueryBuilder = new SQLiteQueryBuilder();
        sArtistQueryBuilder.setTables(DBContract.ArtistTable.TABLE_NAME);
    }

    private static final SQLiteQueryBuilder sTrackQueryBuilder;
    static {
        sTrackQueryBuilder = new SQLiteQueryBuilder();
        sTrackQueryBuilder.setTables(DBContract.TrackTable.TABLE_NAME);
    }

    // search_keyword = ?
    private static final String sKeywordSelectionInArtists =
                    DBContract.ArtistTable.COLUMN_SEARCH_KEYWORD + " = ? ";

    // artist_id = ?
    private static final String sArtistIdSelectionInTracks =
            DBContract.TrackTable.COLUMN_ARTIST_ID + " = ? ";

    // artist_name = ? AND track_name = ?
    private static final String sArtistIdWithTrackIdSelection =
            DBContract.TrackTable.COLUMN_ARTIST_ID + " = ? AND " +
                    DBContract.TrackTable.COLUMN_TRACK_ID + " = ? ";

    private Cursor getArtistBySearchKeyword(Uri uri, String[] projection, String[] selectionArgs, String sortOrder){
        String keyword = DBContract.ArtistTable.getKeywordFromUri(uri);
        return sArtistQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sKeywordSelectionInArtists,
                new String[]{ keyword },
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrackByArtistId(Uri uri, String[] projection, String[] selectionArgs, String sortOrder){
        String keyword = DBContract.TrackTable.getArtistIdFromUri(uri);
        return sTrackQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sArtistIdSelectionInTracks,
                new String[]{ keyword },
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrackByArtistIdAndTrackId(Uri uri, String[] projection, String[] selectionArgs, String sortOrder){
        String artistId = DBContract.TrackTable.getArtistIdFromUri(uri);
        String trackId = DBContract.TrackTable.getTrackIdFromUri(uri);
        return sTrackQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sArtistIdWithTrackIdSelection,
                new String[] {artistId, trackId},
                null,
                null,
                sortOrder
        );
    }

    public static boolean doesKeywordSetExistInArtistDB(Uri uri){
        String keyword = DBContract.ArtistTable.getKeywordFromUri(uri);

        Cursor cursor = sArtistQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                null,
                sKeywordSelectionInArtists,
                new String[]{keyword},
                null,
                null,
                null
        );
        if(cursor.moveToFirst()){
            cursor.close();
            return true;
        }
        else{
            cursor.close();
            return false;
        }
    }

    public static boolean doesKeywordSetExistInTrackDB(Uri uri){
        String keyword = DBContract.TrackTable.getArtistIdFromUri(uri);

        Cursor cursor = sTrackQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                null,
                sArtistIdSelectionInTracks,
                new String[]{keyword},
                null,
                null,
                null
        );
        if(cursor.moveToFirst()){
            cursor.close();
            return true;
        }
        else{
            cursor.close();
            return false;
        }
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
//            case TRACK_WITH_ARTIST_ID_AND_TRACK_NAME:
//                return DBContract.TrackTable.CONTENT_ITEM_TYPE;
            case ARTIST:
                return DBContract.ArtistTable.CONTENT_TYPE;
            case ARTIST_WITH_KEYWORD:
                return DBContract.ArtistTable.CONTENT_TYPE;
            case TRACK:
                return DBContract.TrackTable.CONTENT_TYPE;
            case TRACK_WITH_ARTIST_ID:
                return DBContract.TrackTable.CONTENT_TYPE;
            case TRACK_WITH_ARTIST_ID_AND_TRACK_ID:
                return DBContract.TrackTable.CONTENT_ITEM_TYPE;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case TRACK_WITH_ARTIST_ID_AND_TRACK_ID: {
                retCursor = getTrackByArtistIdAndTrackId(uri, projection, selectionArgs, sortOrder);
                break;
            }
            // "artist/*"
            case ARTIST_WITH_KEYWORD: {
                retCursor = getArtistBySearchKeyword(uri, projection, selectionArgs, sortOrder);
                break;
            }
            case TRACK_WITH_ARTIST_ID: {
                retCursor = getTrackByArtistId(uri, projection, selectionArgs, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ARTIST: {
                long _id = db.insert(DBContract.ArtistTable.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DBContract.ArtistTable.buildArtistUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRACK: {
                long _id = db.insert(DBContract.TrackTable.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DBContract.TrackTable.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = DBContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DBContract.PATH_ARTIST, ARTIST);
        matcher.addURI(authority, DBContract.PATH_ARTIST + "/*", ARTIST_WITH_KEYWORD);
        matcher.addURI(authority, DBContract.PATH_TRACK, TRACK);
        matcher.addURI(authority, DBContract.PATH_TRACK + "/*", TRACK_WITH_ARTIST_ID);
        matcher.addURI(authority, DBContract.PATH_TRACK + "/*/" +
                DBContract.TrackTable.COLUMN_TRACK_ID + "/*", TRACK_WITH_ARTIST_ID_AND_TRACK_ID);

        return matcher;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ARTIST:{
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DBContract.ArtistTable.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case TRACK:{
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DBContract.TrackTable.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
