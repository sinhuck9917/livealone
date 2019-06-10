package com.example.livealone4.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.livealone4.Firebase.FirebaseProfile;
import com.example.livealone4.Models.Estimation;
import com.example.livealone4.R;

public class RatingActivity extends AppCompatActivity {

    private Button backButton, submitButton;
    private RatingBar ratingRating;
    private EditText commentEdit;

    private FirebaseProfile firebaseProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        initView();
        initInstances();

        Toast.makeText(this, "현재 같이먹기 기간에 관계 없이 여러 번 평가할 수 있습니다.", Toast.LENGTH_SHORT).show();
    }

    private void initInstances() {
        firebaseProfile = new FirebaseProfile(RatingActivity.this);

        if (MainActivity.getLiveAloneOfCurrentUser() == null || MainActivity.getUidOfOpponentUser() == null) {
            Toast.makeText(this, "비정상적인 접근입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    private void initView() {
        backButton = findViewById(R.id.back_button_in_activity_rating);
        submitButton = findViewById(R.id.submit_button_in_rating);
        ratingRating = findViewById(R.id.rating_rating_bar);
        commentEdit = findViewById(R.id.comment_edit_text_in_rating);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Estimation estimation = new Estimation(
                        MainActivity.getLiveAloneOfCurrentUser().getKey(),
                        commentEdit.getText().toString(),
                        (double)ratingRating.getRating(),
                        MainActivity.getCurrentUser().getUid(),
                        MainActivity.getCurrentUser().getName());

                firebaseProfile.evaluate(MainActivity.getUidOfOpponentUser(), estimation);

            }
        });
    }
}
