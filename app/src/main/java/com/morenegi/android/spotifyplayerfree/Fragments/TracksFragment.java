package com.morenegi.android.spotifyplayerfree.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.morenegi.android.spotifyplayerfree.Activities.PlayerActivity;
import com.morenegi.android.spotifyplayerfree.ArtistTrackAdapter;
import com.morenegi.android.spotifyplayerfree.FetchAPITask;
import com.morenegi.android.spotifyplayerfree.Model.DBContract;
import com.morenegi.android.spotifyplayerfree.R;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.widget.Toast;


/**
 * A placeholder fragment containing a simple view.
 */
public class TracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = TracksFragment.class.getSimpleName();
    public static final String TRACK_URI = "TRACK_URI";
    private Uri mUri;

    public TracksFragment() {
    }

    CursorAdapter mTrackAdapter = null;
    private static final int TRACK_LOADER = 0;
    private static final String DEFAULT_KEYWORD = "_______";
    private String mKeyword = DEFAULT_KEYWORD;

    private static final String[] TRACK_COLUMNS = new String[]{

            DBContract.TrackTable._ID,
            DBContract.TrackTable.COLUMN_ALBUM_NAME,
            DBContract.TrackTable.COLUMN_TRACK_ID,
            DBContract.TrackTable.COLUMN_TRACK_NAME,
            DBContract.TrackTable.COLUMN_ARTIST_ID,
            DBContract.TrackTable.COLUMN_ARTIST_NAME,
            DBContract.TrackTable.COLUMN_TRACK_THUMBNAIL,
            DBContract.TrackTable.COLUMN_TRACK_PREVIEW_LINK,
            DBContract.TrackTable.COLUMN_LAST_UPDATE
    };

    public static final int COL_ID = 0;
    public static final int COL_ALBUM_NAME = 1;
    public static final int COL_TRACK_ID = 2;
    public static final int COL_TRACK_NAME = 3;
    public static final int COL_ARTIST_ID = 4;
    public static final int COL_ARTIST_NAME = 5;
    public static final int COL_TRACK_THUMBNAIL = 6;
    public static final int COL_TRACK_PREVIEW_LINK = 7;
    public static final int COL_LAST_UPDATE = 8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRACK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);

        mTrackAdapter = new ArtistTrackAdapter(getActivity(), null, 0, ArtistTrackAdapter.VIEW_TYPE_TRACK);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_tracks);
        listView.setAdapter(mTrackAdapter);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(TracksFragment.TRACK_URI);
        }
        (new FetchAPITask(getActivity()))
                .execute(FetchAPITask.FETCH_TRACKS, DBContract.TrackTable.getArtistIdFromUri(mUri));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = ((Cursor) adapterView.getItemAtPosition(position));
                if (cursor != null) {
                    Uri playerUri = DBContract.TrackTable.buildTrackWithArtistIdAndTrackId(
                            cursor.getString(TracksFragment.COL_ARTIST_ID), cursor.getString(TracksFragment.COL_TRACK_ID));

                    String toastMessage = "Opening player for \n" + cursor.getString(TracksFragment.COL_TRACK_NAME);
                    Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
                    Intent intent = (new Intent(getActivity(), PlayerActivity.class))
                            .setData(playerUri);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = DBContract.TrackTable.COLUMN_LAST_UPDATE + " ASC";

        return new CursorLoader(getActivity(),
                mUri,
                TRACK_COLUMNS,
                null,
                null, sortOrder);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTrackAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mTrackAdapter.swapCursor(cursor);
    }
}
