package com.example.livealone4.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.livealone4.Firebase.FirebaseLiveAlone;
import com.example.livealone4.Firebase.FirebasePicture;
import com.example.livealone4.Fragments.MessageDialogFragment;
import com.example.livealone4.Models.LiveAlone;
import com.example.livealone4.Models.User;
import com.example.livealone4.R;
import com.example.livealone4.Firebase.FirebaseLiveAlone;

import java.text.SimpleDateFormat;
import java.util.Calendar;


//게시글을 자세히 보여주는 액티비티
public class LiveAloneActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView titleText, dateText, periodText, aloneTypeText, locationText, commentText
            , nameText, starText;
    private LiveAlone liveAlone;
    private Button contactButton, editButton, deleteButton;
    private FirebaseLiveAlone firebaseLiveAlone;
    private FirebasePicture firebasePicture;
    private String key;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_alone);

        key = getIntent().getStringExtra("key");
        initView();
        initViewFromLiveAloneInstance();
        getDataAndUpdateUI(); //서버로부터 데이터를 받아서 뷰 처리.

    }

    private void getDataAndUpdateUI() {

        //자신이 작성한 글일 경우 수정 삭제 버튼 띄움.
        if(MainActivity.getUidOfCurrentUser().equals(liveAlone.getUid())){
            //editButton.setVisibility(View.VISIBLE); //TODO 메시지 구현 후 구현 예정.
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MessageDialogFragment.setContext(LiveAloneActivity.this);
                    MessageDialogFragment.setKeyAndUid(key, MainActivity.getUidOfCurrentUser());
                    MessageDialogFragment.showDialog(MessageDialogFragment.LIVEALONE_DELETION, LiveAloneActivity.this);
                }
            });

            contactButton.setText("신청자 보기");
            contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LiveAloneActivity.this, CandidateListActivity.class);
                    intent.putExtra("key", key);
                    startActivityForResult(intent, MainActivity.REQUEST_IN_LIVE_ALONE_ACTIVITY);
                }
            });
        } else {
            //자신이 작성하지 않았으면, 신청 상태에 따라 "신청취소" 또는 "신청하기"로 텍스트 변경
            firebaseLiveAlone.initTextOfRequestButton(key, MainActivity.getUidOfCurrentUser(), contactButton);
            contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firebaseLiveAlone.requestLiveAlone(key, MainActivity.getUidOfCurrentUser(), contactButton);
                }
            });

        }



    }

    private void initViewFromLiveAloneInstance() {
        firebaseLiveAlone = new FirebaseLiveAlone(this);
        firebasePicture = new FirebasePicture(this);
        liveAlone = firebaseLiveAlone.searchLiveAlone(key); //메인에서 서버로부터 받은 게시물 리스트에서 해당 key에 맞는 게시물을 탐색.
        user = firebaseLiveAlone.searchUser(key); //작성자 정보를 불러온다.

        if(liveAlone == null || user == null){
            Toast.makeText(this, "존재하지 않는 게시물입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LiveAloneActivity.this, UserProfileActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("name", user.getName());
                intent.putExtra("star",  String.format("%.2f", user.getStar()));
                intent.putExtra("loc", user.getLocation());
                intent.putExtra("count", user.getLivealoneCount());
                startActivity(intent);

            }
        });

        //사진을 띄움
        firebasePicture.downloadImage(liveAlone.getUid(), profileImageView);

        //마감 상태에 따라 버튼과 타이틀의 상태를 바꿈
        if(liveAlone.getUidOfAloneTaker() !=null){
            //마감된 경우 타이틀 변경
            titleText.setTextColor(getResources().getColor(R.color.colorAccent));
            titleText.setText("( 마감된 같이먹기입니다. )");

        } else {
            //마감되지 않은 경우 버튼 신청 버튼 띄움
            contactButton.setVisibility(View.VISIBLE);
            titleText.setText(liveAlone.getTitle());

        }

        //시간 관련 텍스트뷰 (Period, Date)
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat fmt2 = new SimpleDateFormat("MM/dd");
        SimpleDateFormat fmt3 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(liveAlone.getStartPeriod());
        periodText.setText(fmt.format(cal.getTime()));
        cal.setTimeInMillis(liveAlone.getEndPeriod());
        periodText.append(" - "+ fmt2.format(cal.getTime()));
        cal.setTimeInMillis((long)liveAlone.getTimestamp());
        dateText.setText(fmt3.format(cal.getTime()));

        //나머지
        aloneTypeText.setText(liveAlone.getAloneType());
        locationText.setText(liveAlone.getLocation());
        commentText.setText(liveAlone.getComment());
        //유저 정보를 띄움
        starText.setText("★ " + String.format("%.2f",user.getStar()) + " (" + user.getLivealoneCount() + ")");
        nameText.setText(user.getName());


    }


    private void initView() {

        profileImageView = findViewById(R.id.profile_image_view_in_activity_live_alone);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(Color.TRANSPARENT);
        profileImageView.setBackground(shapeDrawable);
        profileImageView.setClipToOutline(true);
        profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        titleText = findViewById(R.id.title_text_view_in_activity_live_alone);
        dateText = findViewById(R.id.upload_date_text_view_in_activity_live_alone);
        periodText = findViewById(R.id.live_alone_period_text_view_in_activity_live_alone);
        aloneTypeText = findViewById(R.id.live_alone_alone_type_text_view_in_activity_live_alone);
        locationText = findViewById(R.id.live_alone_location_text_view_in_activity_live_alone);
        commentText = findViewById(R.id.comment_text_view_in_activity_live_alone);
        nameText = findViewById(R.id.name_text_view_in_activity_live_alone);
        starText = findViewById(R.id.star_text_view_in_activity_live_alone);

        contactButton = findViewById(R.id.contact_button_in_activity_live_alone);
        editButton = findViewById(R.id.edit_button_in_activity_live_alone);
        deleteButton = findViewById(R.id.delete_button_in_activity_live_alone);

        findViewById(R.id.back_button_in_activity_live_alone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == MainActivity.RESULT_REFRESH_IN_LIVE_ALONE_ACTIVITY){
            titleText.setTextColor(getResources().getColor(R.color.colorAccent));
            titleText.setText("( 마감된 매칭입니다. )");
            setResult(MainActivity.RESULT_REFRESH);
            contactButton.setVisibility(View.GONE);
        }

    }

    public FirebaseLiveAlone getFirebaseLiveAlone() {
        return firebaseLiveAlone;
    }
}
