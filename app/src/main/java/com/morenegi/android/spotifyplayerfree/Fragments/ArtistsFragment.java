package com.morenegi.android.spotifyplayerfree.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.morenegi.android.spotifyplayerfree.Activities.TracksActivity;
import com.morenegi.android.spotifyplayerfree.ArtistTrackAdapter;
import com.morenegi.android.spotifyplayerfree.FetchAPITask;
import com.morenegi.android.spotifyplayerfree.Model.DBContract;
import com.morenegi.android.spotifyplayerfree.Model.DBProvider;
import com.morenegi.android.spotifyplayerfree.R;
import com.morenegi.android.spotifyplayerfree.Utility;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public ArtistsFragment() {
    }

    CursorAdapter mArtistAdapter = null;
    private static final int ARTIST_LOADER = 0;
    private static final String DEFAULT_KEYWORD = "_______";
    private String mKeyword = DEFAULT_KEYWORD;

    private static final String[] ARTIST_COLUMNS = new String[]{

            DBContract.ArtistTable._ID,
            DBContract.ArtistTable.COLUMN_ARTIST_ID,
            DBContract.ArtistTable.COLUMN_ARTIST_NAME,
            DBContract.ArtistTable.COLUMN_SEARCH_KEYWORD,
            DBContract.ArtistTable.COLUMN_ARTIST_THUMBNAIL,
            DBContract.ArtistTable.COLUMN_LAST_UPDATE
    };

    public static final int COL_ID = 0;
    public static final int COL_ARTIST_ID = 1;
    public static final int COL_ARTIST_NAME = 2;
    public static final int COL_SEARCH_KEYWORD = 3;
    public static final int COL_ARTIST_THUMBNAIL = 4;
    public static final int COL_LAST_UPDATE = 5;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ARTIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mArtistAdapter = new ArtistTrackAdapter(getActivity(), null, 0, ArtistTrackAdapter.VIEW_TYPE_ARTSIST);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_artists);
        listView.setAdapter(mArtistAdapter);

        final EditText searchArtist = (EditText) rootView.findViewById(R.id.search_artist);

        searchArtist.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String toastMessage = "Getting Top 10 tracks of \n" + v.getText().toString();
                    Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

                    Utility.hideKeyboard(getActivity(), v);
                    mKeyword = v.getText().toString();
                    if (mKeyword.isEmpty())
                        mKeyword = DEFAULT_KEYWORD;
                    onKeywordEntered(mKeyword);

                    return false;
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = ((Cursor) adapterView.getItemAtPosition(position));
                if (cursor != null) {
                    Uri trackUri = DBContract.TrackTable.buildTrackWithArtistId(
                            cursor.getString(ArtistsFragment.COL_ARTIST_ID));

                    Toast.makeText(getActivity(), trackUri.toString(),
                            Toast.LENGTH_SHORT).show();

                    Toast.makeText(getActivity(), trackUri.toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = (new Intent(getActivity(), TracksActivity.class))
                            .setData(trackUri);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = DBContract.ArtistTable.COLUMN_LAST_UPDATE + " ASC";
        Uri artistWithKeywordUri;

        artistWithKeywordUri= DBContract.ArtistTable.buildArtistWithKeyword(mKeyword);

        return new CursorLoader(getActivity(),
                artistWithKeywordUri,
                ARTIST_COLUMNS,
                null,
                null, sortOrder);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mArtistAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mArtistAdapter.swapCursor(cursor);
    }

    public void onKeywordEntered(String keyword){
        Uri artistWithKeywordUri= DBContract.ArtistTable.buildArtistWithKeyword(mKeyword);
        if(!DBProvider.doesKeywordSetExistInTrackDB(artistWithKeywordUri))
            (new FetchAPITask(getActivity()))
                    .execute(FetchAPITask.FETCH_ARTISTS, keyword);
        getLoaderManager().restartLoader(ARTIST_LOADER, null, this);
    }
}
