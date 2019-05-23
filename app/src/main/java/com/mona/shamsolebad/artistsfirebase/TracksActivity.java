package com.mona.shamsolebad.artistsfirebase;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class TracksActivity extends AppCompatActivity implements OnLongClickTrackListenerDelegate {
    private RecyclerView mTrackRecyclerView;
    private EditText mTrackTitleEditText;
    private TextView mArtistNameTextView;
    private SeekBar mRatingSeekBar;
    private TrackAdapter mTrackAdapter;
    private ArrayList<Track> mTrackList;
    private String artistId;
    private EditText mUpdateTrackTitleText;
    private SeekBar mUpdateRatingSeekBar;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration mlistenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        artistId = getIntent().getExtras().getString("artistId");

        String artistName = getIntent().getExtras().getString("artistName");

        mTrackTitleEditText = findViewById(R.id.trackTitleText);
        mArtistNameTextView = findViewById(R.id.artistNameTextView);
        mTrackRecyclerView = findViewById(R.id.tracksRecyclerView);
        mRatingSeekBar = findViewById(R.id.ratingSeekBar);


        mTrackRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        mArtistNameTextView.setText(artistName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //read in data(attach data change listener)
        mlistenerRegistration = db.collection("tracks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mTrackList = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Track track = documentSnapshot.toObject(Track.class);
                    track.setId(documentSnapshot.getId());

                    if (track.getArtistId().equals(artistId))
                        mTrackList.add(track);
                }
                mTrackAdapter = new TrackAdapter(getApplicationContext(), mTrackList,TracksActivity.this);
                mTrackRecyclerView.setAdapter(mTrackAdapter);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mlistenerRegistration.remove();
    }

    public void AddTrack(View view) {
        final String title = mTrackTitleEditText.getText().toString().trim();
        int rating = mRatingSeekBar.getProgress();

        if (!TextUtils.isEmpty(title)) {
            Track track = new Track(artistId, title, rating);

            mTrackList.add(track);
            db.collection("tracks").add(track)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                              @Override
                                              public void onSuccess(DocumentReference documentReference) {
                                                  // mArtistAdapter.notifyDataSetChanged();
                                                  Snackbar.make(findViewById(R.id.coordinatorLayout), title + " successfully added!", Snackbar.LENGTH_LONG).show();
                                                  mTrackTitleEditText.setText("");
                                                  mRatingSeekBar.setProgress(0);
                                                  mTrackTitleEditText.clearFocus();
                                              }
                                          }

                    ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                }
            });


        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout), "please enter title", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLongClickViewHolder(View view, int position) {
        showTrackDialog(position);
    }


    private void showTrackDialog(int position) {
        final Track track = mTrackList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View trackView = LayoutInflater.from(this).inflate(R.layout.trackedit_dialog, null);
        builder.setView(trackView);
        final EditText trackNameET = trackView.findViewById(R.id.updateTrackTitleText);
        trackNameET.setText(track.getTitle());
        final SeekBar seekbar = trackView.findViewById(R.id.updateRatingSeekBar);
        seekbar.setProgress(track.getRating());
        Button updateBtn = trackView.findViewById(R.id.updateTrackButton);
        Button deleteBtn = trackView.findViewById(R.id.deleteTrackButton);

        builder.setTitle("Update " + track.getTitle());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        updateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newTrackName = trackNameET.getText().toString().trim();
                int sb = seekbar.getProgress();
                if (TextUtils.isEmpty(newTrackName)) {
                    trackNameET.setError("Track name Required");
                    return;
                }
                updateTrack(newTrackName, sb, track.getId());
                alertDialog.dismiss();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTrack(track.getId());
                alertDialog.dismiss();
            }
        });


    }

    private void deleteTrack(String id) {
        CollectionReference trackRef = db.collection("tracks");
        trackRef.document(id).
                delete().
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void updateTrack(final String newTrackName, final int newRating,final String id) {

        final DocumentReference trackRef =db.collection("tracks").document(id);
        db.runTransaction(new Transaction.Function<Void>() {
            @android.support.annotation.Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.set(trackRef,new Track(artistId,newTrackName,newRating));
                return null;
            }
        });

    }



}
