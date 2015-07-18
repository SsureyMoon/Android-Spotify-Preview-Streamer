package com.morenegi.android.spotifyplayerfree.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.morenegi.android.spotifyplayerfree.Model.DBContract.ArtistTable;
import static com.morenegi.android.spotifyplayerfree.Model.DBContract.TrackTable;

/**
 * Created by ssureymoon on 7/15/15.
 */
public class DBHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "spotifyfree.db";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ARTIST_TABLE =
                "CREATE TABLE " + ArtistTable.TABLE_NAME + " (" +
                        ArtistTable._ID + " INTEGER PRIMARY KEY, " +
                        ArtistTable.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                        ArtistTable.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                        ArtistTable.COLUMN_ARTIST_THUMBNAIL + " , " +
                        ArtistTable.COLUMN_SEARCH_KEYWORD + " TEXT NOT NULL, " +
                        ArtistTable.COLUMN_LAST_UPDATE + " INTEGER NOT NULL, " +
                        " UNIQUE (" + ArtistTable.COLUMN_ARTIST_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_ARTIST_TABLE);

        final String SQL_CREATE_TRACK_TABLE =
                "CREATE TABLE " + TrackTable.TABLE_NAME + " (" +
                        TrackTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                        TrackTable.COLUMN_ALBUM_NAME + " TEXT NOT NULL, " +
                        TrackTable.COLUMN_TRACK_ID + " TEXT NOT NULL, " +
                        TrackTable.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                        TrackTable.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                        TrackTable.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                        TrackTable.COLUMN_TRACK_PREVIEW_LINK + " TEXT NOT NULL, " +
                        TrackTable.COLUMN_TRACK_THUMBNAIL + " TEXT NOT NULL, " +
                        TrackTable.COLUMN_LAST_UPDATE + " INTEGER NOT NULL," +
                        " UNIQUE (" + TrackTable.COLUMN_TRACK_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_TRACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + ArtistTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrackTable.TABLE_NAME);
        onCreate(db);
    }
}
