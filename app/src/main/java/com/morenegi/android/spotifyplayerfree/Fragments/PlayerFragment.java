package com.morenegi.android.spotifyplayerfree.Fragments;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.morenegi.android.spotifyplayerfree.Model.DBContract;
import com.morenegi.android.spotifyplayerfree.R;
import com.squareup.picasso.Picasso;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    MediaPlayer mMediaPlayer = null;
    private Handler mHandler = new Handler();

    public static final String PLAYER_URI = "PLAYER_URI";
    private static final int PLAYER_LOADER = 0;

    private static final String STATE_PLAY = "PLAY";
    private static final String STATE_PAUSE = "PAUSE";
    private double mTimeElapsed = 0;
    private double mDuration = 0;

    private Uri mUri;

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

    private ImageView mTrackThumbNail;
    private TextView mArtistName;
    private TextView mAlbumName;
    private TextView mTrackName;
    private TextView mMinDuration;
    private TextView mMaxDuration;
    private ImageButton mPlayButton;
    private ImageButton mBackButton;
    private ImageButton mForwardButton;
    private SeekBar mSeekBar;
    private Thread mTrhead;
    private String mMediaSource;
    private boolean mIsPlaying = false;
    private boolean mHasLegacy = false;

    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mHasLegacy = savedInstanceState.getBoolean("has_legacy", false);
            mTimeElapsed = savedInstanceState.getDouble("current_position", 0);
            mIsPlaying = savedInstanceState.getBoolean("is_playing", false);
            mMediaSource = savedInstanceState.getString("media_source");

            prepareMediaPlayer(mIsPlaying, mMediaSource);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(PlayerFragment.PLAYER_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mArtistName = (TextView) rootView.findViewById(R.id.player_artist_name);
        mAlbumName = (TextView) rootView.findViewById(R.id.player_album_name);
        mTrackName = (TextView) rootView.findViewById(R.id.player_track_name);
        mTrackThumbNail = (ImageView) rootView.findViewById(R.id.player_track_thumbnail);
        mMinDuration = (TextView) rootView.findViewById(R.id.min_duration);
        mMaxDuration = (TextView) rootView.findViewById(R.id.max_duration);

        mPlayButton = (ImageButton) rootView.findViewById(R.id.media_play);
        mBackButton = (ImageButton) rootView.findViewById(R.id.media_previous);
        mForwardButton = (ImageButton) rootView.findViewById(R.id.media_next);

        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seek_bar);

        if(mHasLegacy){
            if(mIsPlaying)
                mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            else
                mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mDuration = mMediaPlayer.getDuration();
            int sec = (int) mDuration/1000;
            int milisec = (int) mDuration%1000;
            mMaxDuration.setText(getActivity().getString(R.string.duration, sec, milisec));
            mSeekBar.setClickable(true);
            mSeekBar.setProgress((int) mTimeElapsed);
            mHandler.postDelayed(updateSeekBarTime, 100);
        }
        else {
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
        }

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        ((ImageButton) v).setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        mMediaPlayer.start();
                        mTimeElapsed = mMediaPlayer.getCurrentPosition();
                        mHandler.postDelayed(updateSeekBarTime, 100);
                        ((ImageButton) v).setImageResource(android.R.drawable.ic_media_pause);
                    }
                }catch(Exception e){
                    Log.e(LOG_TAG, e.getStackTrace().toString());
                }

            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mMediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return rootView;
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            try {
                mTimeElapsed = mMediaPlayer.getCurrentPosition();
                //set seekbar progress
                if(mTimeElapsed >= mMediaPlayer.getDuration()){
                    initializePlayer();
                    return;
                }
                mSeekBar.setProgress((int) mTimeElapsed);
                //repeat yourself that again in 100 miliseconds
                mHandler.postDelayed(this, 100);
            }
            catch(Exception e){
                Log.e(LOG_TAG, e.getStackTrace().toString());
            }

        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PLAYER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    TRACK_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(!cursor.moveToFirst())
            return;
        String imageUri = cursor.getString(COL_TRACK_THUMBNAIL);
        if(imageUri != null)
            Picasso.with(getActivity()).load(imageUri).into(mTrackThumbNail);
        else
            mTrackThumbNail.setImageResource(R.mipmap.ic_launcher);
        mArtistName.setText(cursor.getString(COL_ARTIST_NAME));
        mAlbumName.setText(cursor.getString(COL_ALBUM_NAME));
            mTrackName.setText(cursor.getString(COL_TRACK_NAME));
        mMinDuration.setText(getActivity().getString(R.string.duration, 0, 0));


        mMediaSource = cursor.getString(COL_TRACK_PREVIEW_LINK);
        if(!mHasLegacy){
            prepareMediaPlayer(false, mMediaSource);
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mDuration = mMediaPlayer.getDuration();
            int sec = (int) mDuration/1000;
            int milisec = (int) mDuration%1000;
            mMaxDuration.setText(getActivity().getString(R.string.duration, sec, milisec));
            mSeekBar.setClickable(true);
        }




    }

    @Override
    public void onStop() {
        super.onStop();
        if(mMediaPlayer != null){
            releaseMediaPlayer();
        }

    }

    void prepareMediaPlayer(boolean isPlaying, String mediaSource){

        try {

            if(mMediaPlayer == null){
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        initializePlayer();
                    }
                });
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            if(mediaSource != null){
                mMediaPlayer.setDataSource(mediaSource);
                mMediaPlayer.prepare();
                mMediaPlayer.seekTo((int) mTimeElapsed);
            }

            if(isPlaying){
                mMediaPlayer.start();
            }
        }
        catch (Exception e){
            mMediaPlayer = null;
            Log.e(LOG_TAG, e.getStackTrace().toString());
        }
    }

    void releaseMediaPlayer(){

        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMediaPlayer != null) {
            outState.putBoolean("has_legacy", true);
            outState.putBoolean("is_playing", mMediaPlayer.isPlaying());
            outState.putDouble("current_position", mMediaPlayer.getCurrentPosition());
            outState.putString("media_source", mMediaSource);
        }

    }

    void initializePlayer(){
        mTimeElapsed = 0;
        mSeekBar.setProgress((int) mTimeElapsed);
        mPlayButton.setImageResource(android.R.drawable.ic_media_play);
    }

}
