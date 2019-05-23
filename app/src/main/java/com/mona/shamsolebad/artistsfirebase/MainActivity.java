package com.mona.shamsolebad.artistsfirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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

public class MainActivity extends AppCompatActivity implements OnViewHolderClickListenerDelegate {
    private RecyclerView mArtistRecyclerView;
    private EditText mNameEditText;
    private Spinner mGenreSpinner;
    private ArtistAdapter mArtistAdapter;
    private ArrayList<Artist> mArtistArrayList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration mlistenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNameEditText = findViewById(R.id.nameEditText);
        mArtistRecyclerView = findViewById(R.id.artistsRecyclerView);
        mGenreSpinner = findViewById(R.id.genreSpinner);

        mArtistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //read in data(attach data change listener)
        mlistenerRegistration = db.collection("artists").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mArtistArrayList = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Artist artist = documentSnapshot.toObject(Artist.class);
                    artist.setId(documentSnapshot.getId());
                    mArtistArrayList.add(artist);
                }
                mArtistAdapter = new ArtistAdapter(getApplicationContext(), mArtistArrayList, MainActivity.this);
                mArtistRecyclerView.setAdapter(mArtistAdapter);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mlistenerRegistration.remove();
    }


    public void addArtist(View view) {

        final String name = mNameEditText.getText().toString().trim();
        String genre = mGenreSpinner.getSelectedItem().toString();
        if (!TextUtils.isEmpty(name)) {
            Artist artist = new Artist(name, genre);

            mArtistArrayList.add(artist);
            db.collection("artists").add(artist)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                              @Override
                                              public void onSuccess(DocumentReference documentReference) {
                                                  // mArtistAdapter.notifyDataSetChanged();
                                                  Snackbar.make(findViewById(R.id.coordinatorLayout), name + " successfully added!", Snackbar.LENGTH_LONG).show();
                                                  mNameEditText.setText("");
                                                  mNameEditText.clearFocus();
                                              }
                                          }

                    ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                }
            });


        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout), "please enter name", Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void onLongClickViewHolder(View view, int position) {
        showAlertDialog(position);
    }

    @Override
    public void onClickViewHolder(View view, int position) {
        Intent trackIntent = new Intent(this, TracksActivity.class);
        Artist artist = mArtistArrayList.get(position);
        trackIntent.putExtra("artistId", artist.getId());
        trackIntent.putExtra("artistName", artist.getName());

        startActivity(trackIntent);
    }

    private void showAlertDialog(int position) {
        final Artist artist = mArtistArrayList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogeView = LayoutInflater.from(this).inflate(R.layout.artist_edit_dialog, null);
        builder.setView(dialogeView);
        final EditText nameET = dialogeView.findViewById(R.id.dialogNameEditText);
        nameET.setText(artist.getName());
        final Spinner spinner = dialogeView.findViewById(R.id.dialogGenreSpinner);
        spinner.setSelection(getIndexforGenre(artist.getGenre()));
        Button updateBtn = dialogeView.findViewById(R.id.dialogUpdateButton);
        Button deleteBtn = dialogeView.findViewById(R.id.dialogDeleteButton);

        builder.setTitle("Update " + artist.getName());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        updateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newName = nameET.getText().toString().trim();
                String newGenre = spinner.getSelectedItem().toString();
                if (TextUtils.isEmpty(newName)) {
                    nameET.setError("Artist name Required");
                    return;
                }
                updateArtist(newName, newGenre, artist.getId());
                alertDialog.dismiss();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteArtist(artist.getId());
                alertDialog.dismiss();
            }
        });


    }

    private void deleteArtist(String id) {
        CollectionReference artistRef = db.collection("artists");
        artistRef.document(id).
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

    private void updateArtist(final String newName, final String newGenre, String id) {

       final DocumentReference artistRef =db.collection("artists").document(id);
       db.runTransaction(new Transaction.Function<Void>() {
           @android.support.annotation.Nullable
           @Override
           public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
               transaction.set(artistRef,new Artist(newName,newGenre));
               return null;
           }
       });

    }

    private int getIndexforGenre(String genre) {
        switch (genre) {
            case "Hip-Hop":
                return 0;
            case "R&B":
                return 1;
            case "Pop":
                return 2;
            case "Rock":
                return 3;
            default:
                return 0;
        }
    }



}
