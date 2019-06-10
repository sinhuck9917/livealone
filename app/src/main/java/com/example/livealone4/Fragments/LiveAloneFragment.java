package com.example.livealone4.Fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.example.livealone4.Activities.CandidateListActivity;
import com.example.livealone4.Activities.MainActivity;
import com.example.livealone4.Activities.MessageActivity;
import com.example.livealone4.Activities.RatingActivity;
import com.example.livealone4.Activities.UserProfileActivity;
import com.example.livealone4.Firebase.FirebaseProfile;
import com.example.livealone4.Models.LiveAlone;
import com.example.livealone4.Models.User;
import com.example.livealone4.R;
import com.example.livealone4.Utils.ProgressDialogHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveAloneFragment extends Fragment {

    private static LinearLayout hiddenLayout, noneAloneLayout, oppUserLayout; //진행중인 같이먹기의 존재 여부에 따라 레이아웃을 띄운다.
    private static ImageView profileImageView;
    private static TextView titleText, dateText, periodText, aloneTypeText, locationText, commentText
            , nameText, starText;
    private static Button messageButton, estimationButton, cancelButton, contactButton;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static Context context;
    private static boolean isOngoing;

    private static boolean mutex = false; //액티비티가 중첩돼서 실행되지 않게 해줌

    public LiveAloneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_alone, container, false);
        context = getActivity();
        initView(view);

        return view;
    }

    public static void setViews(boolean onGoing) {
        isOngoing = onGoing;

        //뷰 초기화
        oppUserLayout.setVisibility(View.GONE);
        contactButton.setVisibility(View.GONE);
        messageButton.setVisibility(View.GONE);

        final LiveAlone liveAlone = MainActivity.getLiveAloneOfCurrentUser(); //메인에서 서버로부터 받은 게시물 리스트에서 해당 key에 맞는 게시물을 탐색.

        if(isOngoing) {
            oppUserLayout.setVisibility(View.VISIBLE);
            messageButton.setVisibility(View.VISIBLE);

            final User user = MainActivity.getOpponentUser(); //작성자 정보를 불러온다.
            //사진을 띄움
            MainActivity.getFirebasePicture().downloadImage(user.getUid(), profileImageView);
            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("uid", user.getUid());
                    intent.putExtra("name", user.getName());
                    intent.putExtra("star",  String.format("%.2f", user.getStar()));
                    intent.putExtra("loc", user.getLocation());
                    intent.putExtra("count", user.getLivealoneCount());
                    context.startActivity(intent);

                }
            });

            //유저 정보를 띄움
            starText.setText("★ " + String.format("%.2f", user.getStar()) + " (" + user.getLivealoneCount() + ")");
            nameText.setText(user.getName());

        } else {

            contactButton.setVisibility(View.VISIBLE);
            contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CandidateListActivity.class);
                    intent.putExtra("key", liveAlone.getKey());
                    ((MainActivity)context).startActivityForResult(intent, MainActivity.REQUEST_IN_LIVE_ALONE_ACTIVITY);
                }
            });

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
        titleText.setText(liveAlone.getTitle());





    }

    private void initView(View view) {

        hiddenLayout = view.findViewById(R.id.hidden_view_in_message_fragment);
        noneAloneLayout = view.findViewById(R.id.none_alone_view_in_message_fragment);
        oppUserLayout = view.findViewById(R.id.opponent_user_layout);
        profileImageView = view.findViewById(R.id.profile_image_view_in_fragment_live_alone);
        profileImageView.setBackground(new ShapeDrawable(new OvalShape()));
        profileImageView.setClipToOutline(true);
        profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        titleText = view.findViewById(R.id.title_text_view_in_fragment_live_alone);
        dateText = view.findViewById(R.id.upload_date_text_view_in_fragment_live_alone);
        periodText = view.findViewById(R.id.live_alone_period_text_view_in_fragment_live_alone);
        aloneTypeText = view.findViewById(R.id.live_alone_type_text_view_in_fragment_live_alone);
        locationText = view.findViewById(R.id.live_alone_location_text_view_in_fragment_live_alone);
        commentText = view.findViewById(R.id.comment_text_view_in_fragment_live_alone);
        nameText = view.findViewById(R.id.name_text_view_in_fragment_live_alone);
        starText = view.findViewById(R.id.star_text_view_in_fragment_live_alone);

        cancelButton = view.findViewById(R.id.cancel_button_in_fragment_live_alone);
        messageButton = view.findViewById(R.id.message_button_in_fragment_live_alone);
        estimationButton = view.findViewById(R.id.estimation_button_in_fragment_live_alone);
        contactButton = view.findViewById(R.id.contact_button_in_fragment_live_alone);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_in_live_alone_fragment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((MainActivity)getActivity()).refresh(false, swipeRefreshLayout);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mutex)
                    return;
                mutex = true;

                ProgressDialogHelper.show(LiveAloneFragment.this.getActivity());
                FirebaseProfile.getUserRef().child(MainActivity.getUidOfCurrentUser()).child("current_livealone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mutex = false;
                        ProgressDialogHelper.dismiss();
                        if (dataSnapshot.getValue(String.class) == null) {
                            Toast.makeText(LiveAloneFragment.this.getContext(), "삭제된 게시물입니다.", Toast.LENGTH_SHORT).show();
                            ((MainActivity) LiveAloneFragment.this.getActivity()).refresh(true, null);
                        } else {
                            if(isOngoing) {
                                MessageDialogFragment.setContext(LiveAloneFragment.this.getActivity());
                                MessageDialogFragment.setKeyAndUid(MainActivity.getLiveAloneOfCurrentUser().getKey(), MainActivity.getUidOfCurrentUser());
                                MessageDialogFragment.showDialog(MessageDialogFragment.LIVEALONE_CANCELLATION, LiveAloneFragment.this.getActivity());
                            } else {
                                MessageDialogFragment.setContext(LiveAloneFragment.this.getActivity());
                                MessageDialogFragment.setKeyAndUid(MainActivity.getLiveAloneOfCurrentUser().getKey(), MainActivity.getUidOfCurrentUser());
                                MessageDialogFragment.showDialog(MessageDialogFragment.LIVEALONE_DELETION_IN_MAIN, LiveAloneFragment.this.getActivity());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mutex = false;
                    }
                });




            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mutex)
                    return;
                mutex = true;

                ProgressDialogHelper.show(LiveAloneFragment.this.getActivity());
                FirebaseProfile.getUserRef().child(MainActivity.getUidOfCurrentUser()).child("current_livealone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mutex = false;
                        ProgressDialogHelper.dismiss();
                        if (dataSnapshot.getValue(String.class) == null) {
                            Toast.makeText(LiveAloneFragment.this.getContext(), "삭제된 게시물입니다.", Toast.LENGTH_SHORT).show();
                            ((MainActivity) LiveAloneFragment.this.getActivity()).refresh(true, null);
                        } else {
                            Intent intent = new Intent(LiveAloneFragment.this.getActivity(), MessageActivity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mutex = false;
                    }
                });

            }
        });

        estimationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialogHelper.show(LiveAloneFragment.this.getActivity());

                FirebaseProfile.getUserRef().child(MainActivity.getUidOfCurrentUser()).child("current_livealone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ProgressDialogHelper.dismiss();
                        if(dataSnapshot.getValue(String.class) == null){
                            Toast.makeText(LiveAloneFragment.this.getContext(), "삭제된 게시물입니다.", Toast.LENGTH_SHORT).show();
                            ((MainActivity)LiveAloneFragment.this.getActivity()).refresh(true, null);
                        } else {
                            Intent intent = new Intent(LiveAloneFragment.this.getActivity(), RatingActivity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        });

    }

    public static Button getMessageButton() {
        return messageButton;
    }

    public static LinearLayout getHiddenLayout() {
        return hiddenLayout;
    }

    public static LinearLayout getNoneAloneLayout() {
        return noneAloneLayout;
    }
}
