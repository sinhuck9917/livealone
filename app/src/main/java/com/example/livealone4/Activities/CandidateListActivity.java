package com.example.livealone4.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.example.livealone4.Firebase.FirebaseLiveAlone;
import com.example.livealone4.Firebase.FirebasePicture;
import com.example.livealone4.R;

public class CandidateListActivity extends AppCompatActivity {

    private Button backButton;
    private RecyclerView recyclerView;
    private FirebaseLiveAlone firebaseLiveAlone;
    private static FirebasePicture firebasePicture;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_list);
        key = getIntent().getStringExtra("key");
        initView();
        initFirebase();
    }

    private void initFirebase() {
        firebasePicture = new FirebasePicture(this);
        firebaseLiveAlone = new FirebaseLiveAlone(this);
        firebaseLiveAlone.setCandidatesRecyclerView(recyclerView);
        firebaseLiveAlone.refreshCandidates(key);
    }

    private void initView() {
        backButton = findViewById(R.id.back_button_in_activity_candidate_list);
        recyclerView = findViewById(R.id.candidate_recycler_view);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    public FirebaseLiveAlone getFirebaseLiveAlone() {
        return firebaseLiveAlone;
    }

    public String getKey() {
        return key;
    }

    public static FirebasePicture getFirebasePicture() {
        return firebasePicture;
    }
}
