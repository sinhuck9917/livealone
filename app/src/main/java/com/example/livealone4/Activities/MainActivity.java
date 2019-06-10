package com.example.livealone4.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.livealone4.Adapters.ViewPagerAdapter;
import com.example.livealone4.Firebase.FirebaseLiveAlone;
import com.example.livealone4.Firebase.FirebaseAccount;
import com.example.livealone4.Firebase.FirebasePicture;
import com.example.livealone4.Firebase.FirebaseProfile;
import com.example.livealone4.Fragments.FilterFragment;
import com.example.livealone4.Fragments.LiveAloneCreationFragment;
import com.example.livealone4.Fragments.MessageDialogFragment;
import com.example.livealone4.Models.LiveAlone;
import com.example.livealone4.Models.User;
import com.example.livealone4.R;
import com.example.livealone4.Services.LiveAloneService;
import com.example.livealone4.Utils.BackButtonHandler;
import com.example.livealone4.Utils.PageChangeListener;

public class MainActivity extends AppCompatActivity {

    //result & request codes
    public static final int RESULT_REFRESH = 1000;
    public static final int REQUEST_LIVE_ALONE_ACTIVITY = 1001;
    public static final int RESULT_REFRESH_IN_LIVE_ALONE_ACTIVITY = 1002;
    public static final int REQUEST_IN_LIVE_ALONE_ACTIVITY = 1003;
    public static final int REQUEST_GALLERY = 1004;
    private static LiveAlone liveAloneOfCurrentUser;

    private Button hiringButton, messageButton, myPageButton, addOrCheckLiveAloneButton, filterButton ,logOutButton;
    private ViewPager mainViewPager;
    private BackButtonHandler backButtonHandler;
    private static ImageView profileImageView;
    private TextView profileNameText, titleText;
    private static LinearLayout progressBarLayout;
    private RelativeLayout actionBarLayout;
    private Button testButton;

    private static FirebaseAccount firebaseAccount;
    private static FirebaseLiveAlone firebaseLiveAlone;
    private static FirebaseProfile firebaseProfile;
    private static FirebasePicture firebasePicture;

    private static User currentUser, opponentUser;
    private static LiveAlone LiveAloneOfCurrentUser;
    private static String uidOfCurrentUser, uidOfOpponentUser; //uidOfOpponentUser : 같이먹기가 진행중일 때, 상대방의 uid

    private static Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_main);

        initInstances(); //인스턴스 생성 및 초기화
        initButtons(); //삭제, 필터 버튼 초기화
        initAuth(); //파이어베이스 관련 객체 초기화
        initView(); //뷰 초기화
        getDataFromFirebase(); //파이어베이스로부터 유저 정보를 받고 ui를 업데이트한다.
        initService(); //서비스 초기화



    }

    private void initService() {
        serviceIntent = new Intent(this, LiveAloneService.class);
        serviceIntent.putExtra("uid", uidOfCurrentUser);
        startService(serviceIntent);
    }

    private void getDataFromFirebase() {
        firebaseProfile.getCurrentUserAndLivealoneInMainActivity(uidOfCurrentUser, profileNameText);
        firebasePicture.downloadImage(uidOfCurrentUser, profileImageView);
    }

    private void initAuth() {
        firebaseAccount = new FirebaseAccount(this);
        firebaseLiveAlone = new FirebaseLiveAlone(this);
        firebaseProfile = new FirebaseProfile(this);
        firebasePicture = new FirebasePicture(this);
    }

    private void initInstances() {
        uidOfCurrentUser = getIntent().getStringExtra("uid");
        backButtonHandler = new BackButtonHandler(this);
    }

    private void initButtons() {
        //카드 추가, 또는 체크 버튼
        addOrCheckLiveAloneButton = findViewById(R.id.live_alone_add_or_check_button);
        addOrCheckLiveAloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LiveAloneCreationFragment liveAloneCreationFragment = new LiveAloneCreationFragment();
                liveAloneCreationFragment.setCancelable(false);
                liveAloneCreationFragment.show(getFragmentManager(), "");
            }
        });

        //필터링 버튼
        filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FilterFragment filterFragment = new FilterFragment();
                filterFragment.setCancelable(false);
                filterFragment.show(getFragmentManager(), "");
            }
        });

        //로그아웃 버튼
        logOutButton = findViewById(R.id.logout_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageDialogFragment.setContext(MainActivity.this);
                MessageDialogFragment.showDialog(MessageDialogFragment.LOG_OUT, MainActivity.this);
            }
        });
    }

    private void initView() {
        //타이틀
        titleText = findViewById(R.id.title_text_view_in_main);
        actionBarLayout = findViewById(R.id.action_bar_in_main);

        //버튼
        hiringButton = findViewById(R.id.hiring_fragment_button);
        hiringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mainViewPager.setCurrentItem(0);
            }
        });
        messageButton = findViewById(R.id.message_fragment_button);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mainViewPager.setCurrentItem(1);
            }
        });
        myPageButton = findViewById(R.id.my_page_fragment_button);
        myPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mainViewPager.setCurrentItem(2) ;
            }
        });

        //뷰페이저
        mainViewPager = findViewById(R.id.main_view_pager);
        mainViewPager.setOffscreenPageLimit(3);
        mainViewPager.setAdapter(new ViewPagerAdapter(this, getSupportFragmentManager()));
        PageChangeListener pageChangeListener = new PageChangeListener(hiringButton, messageButton,
                myPageButton, addOrCheckLiveAloneButton, filterButton ,logOutButton, titleText, actionBarLayout);
        pageChangeListener.onPageSelected(0);
        mainViewPager.setOnPageChangeListener(pageChangeListener);
        mainViewPager.setCurrentItem(0);

        //프로필 관련
        profileImageView = findViewById(R.id.profile_image_view_in_main_activity);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(Color.TRANSPARENT);
        profileImageView.setBackground(shapeDrawable);
        profileImageView.setClipToOutline(true);
        profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        profileNameText = findViewById(R.id.name_text_view_in_main_activity);

        //프로그래스바 레이아웃
        progressBarLayout = findViewById(R.id.progress_bar_layout);
    }

    public void refresh(boolean setProgressBarLayoutVisible, @Nullable SwipeRefreshLayout swipeRefreshLayout){
        if(firebaseLiveAlone != null && firebaseLiveAlone.getLiveAloneRecyclerView()!=null){
            if(setProgressBarLayoutVisible)
                progressBarLayout.setVisibility(View.VISIBLE);
            firebaseProfile.getCurrentUserAndLivealoneInMainActivity(uidOfCurrentUser, profileNameText);
            firebaseLiveAlone.refreshLiveAlone(swipeRefreshLayout);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_REFRESH || resultCode == RESULT_REFRESH_IN_LIVE_ALONE_ACTIVITY){ //리프레쉬가 필요한 경우
            refresh(true, null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseAccount.mAuth.addAuthStateListener(firebaseAccount.mAuthListener);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAccount.mAuthListener != null) {
            firebaseAccount.mAuth.removeAuthStateListener(firebaseAccount.mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        backButtonHandler.onBackPressed(); //두 번 눌렀을 때 종료되도록
    }


    //getters & setters
    public FirebaseAccount getFirebaseAccount() {
        return firebaseAccount;
    }

    public FirebaseLiveAlone getFirebaseLiveAlone() {
        return firebaseLiveAlone;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        MainActivity.currentUser = currentUser;
    }

    public static String getUidOfCurrentUser() {
        return uidOfCurrentUser;
    }

    public Button getAddOrCheckLiveAloneButton() {
        return addOrCheckLiveAloneButton;
    }

    public Button getFilterButton() {
        return filterButton;
    }

    public static LiveAlone getLiveAloneOfCurrentUser() {
        return liveAloneOfCurrentUser;
    }

    public static void setLiveAloneOfCurrentUser(LiveAlone liveAloneOfCurrentUser) {
        MainActivity.liveAloneOfCurrentUser = liveAloneOfCurrentUser;
    }

    public static String getUidOfOpponentUser() {
        return uidOfOpponentUser;
    }

    public static void setUidOfOpponentUser(String uidOfOpponentUser) {
        MainActivity.uidOfOpponentUser = uidOfOpponentUser;
    }

    public static FirebasePicture getFirebasePicture() {
        return firebasePicture;
    }

    public static FirebaseProfile getFirebaseProfile() {
        return firebaseProfile;
    }

    public static User getOpponentUser() {
        return opponentUser;
    }

    public static void setOpponentUser(User opponentUser) {
        MainActivity.opponentUser = opponentUser;
    }

    public static LinearLayout getProgressBarLayout() {
        return progressBarLayout;
    }

    public static ImageView getProfileImageView() {
        return profileImageView;
    }
}

