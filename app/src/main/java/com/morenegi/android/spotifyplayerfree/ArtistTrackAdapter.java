package com.morenegi.android.spotifyplayerfree;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.morenegi.android.spotifyplayerfree.Fragments.ArtistsFragment;
import com.morenegi.android.spotifyplayerfree.Fragments.TracksFragment;
import com.squareup.picasso.Picasso;

/**
 * Created by ssureymoon on 7/16/15.
 */
public class ArtistTrackAdapter extends CursorAdapter{

    public static final int VIEW_TYPE_ARTSIST = 0;
    public static final int VIEW_TYPE_TRACK = 1;

    private final Context mContext;
    private final int mViewType;

    public ArtistTrackAdapter(Context context, Cursor c, int flags, int view_type){
        super(context, c, flags);
        mContext = context;
        mViewType = view_type;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        View view = null;
        switch (viewType) {
            case VIEW_TYPE_ARTSIST: {
                layoutId = R.layout.list_item_artists;
                view = LayoutInflater.from(context).inflate(layoutId, parent, false);
                view.setTag(new ViewHolderForArtist(view));
                break;
            }
            case VIEW_TYPE_TRACK: {
                layoutId = R.layout.list_item_tracks;
                view = LayoutInflater.from(context).inflate(layoutId, parent, false);
                view.setTag(new ViewHolderForTrack(view));
                break;
            }
        }

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Read weather icon ID from cursor
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_ARTSIST: {
                ViewHolderForArtist viewHolder = (ViewHolderForArtist) view.getTag();
                String imageUri = cursor.getString(ArtistsFragment.COL_ARTIST_THUMBNAIL);
                if(imageUri != null)
                    Picasso.with(context).load(imageUri).into(viewHolder.iconView);
                else
                    viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);
                String artistName = cursor.getString(ArtistsFragment.COL_ARTIST_NAME);
                viewHolder.textView.setText(artistName);
                break;
            }
            case VIEW_TYPE_TRACK: {
                ViewHolderForTrack viewHolder = (ViewHolderForTrack) view.getTag();
                String imageUri = cursor.getString(TracksFragment.COL_TRACK_THUMBNAIL);
                if(imageUri != null)
                    Picasso.with(context).load(imageUri).into(viewHolder.iconView);
                else
                    viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);
                viewHolder.textAlbumView.setText(cursor.getString(TracksFragment.COL_ALBUM_NAME));
                viewHolder.textTrackView.setText(cursor.getString(TracksFragment.COL_TRACK_NAME));
                break;
            }

        }

    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public static class ViewHolderForArtist {
        public final ImageView iconView;
        public final TextView textView;

        public ViewHolderForArtist(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_artist_icon);
            textView = (TextView) view.findViewById(R.id.list_item_artist_name);
        }
    }

    public static class ViewHolderForTrack {
        public final ImageView iconView;
        public final TextView textAlbumView;
        public final TextView textTrackView;

        public ViewHolderForTrack(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_track_icon);
            textAlbumView = (TextView) view.findViewById(R.id.list_item_album_name);
            textTrackView = (TextView) view.findViewById(R.id.list_item_track_name);
        }
    }
}
