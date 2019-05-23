package com.mona.shamsolebad.artistsfirebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private ArrayList<Track> mTrackArrayList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private OnLongClickTrackListenerDelegate mDelegate;

    public TrackAdapter(Context context, ArrayList<Track> mTrackArrayList, OnLongClickTrackListenerDelegate delegate) {
        this.mTrackArrayList = mTrackArrayList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDelegate = delegate;

    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mLayoutInflater.inflate(R.layout.tracklist_item, viewGroup, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        trackViewHolder.bind(mTrackArrayList.get(i));

    }

    @Override
    public int getItemCount() {
        return mTrackArrayList.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        private TextView mTrackTextView;
        private TextView mRatingTextView;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            mTrackTextView = itemView.findViewById(R.id.trackTitleTextView);
            mRatingTextView = itemView.findViewById(R.id.ratingTextView);
            itemView.setOnLongClickListener(this);
        }

        public void bind(Track track) {
            mTrackTextView.setText(track.getTitle());
            mRatingTextView.setText(String.valueOf(track.getRating()));
        }


        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            Toast.makeText(mContext, "" + mTrackArrayList.get(pos).getTitle(), Toast.LENGTH_LONG).show();
            mDelegate.onLongClickViewHolder(v, pos);
            return false;
        }
    }
}

interface OnLongClickTrackListenerDelegate{
    void onLongClickViewHolder(View view, int position);
}

