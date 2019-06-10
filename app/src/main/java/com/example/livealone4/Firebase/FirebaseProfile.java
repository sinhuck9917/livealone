package com.example.livealone4.Firebase;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.livealone4.Activities.MainActivity;
import com.example.livealone4.Activities.UserProfileActivity;
import com.example.livealone4.Adapters.EstimationAdapter;
import com.example.livealone4.Fragments.LiveAloneFragment;
import com.example.livealone4.Fragments.MessageDialogFragment;
import com.example.livealone4.Fragments.MyPageFragment;
import com.example.livealone4.Models.Estimation;
import com.example.livealone4.Models.LiveAlone;
import com.example.livealone4.Models.User;
import com.example.livealone4.Utils.MyLinearLayoutManager;
import com.example.livealone4.Utils.ProgressDialogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 유저 프로필 관련 (점수 매기기 등)

public class FirebaseProfile {

    private final static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final static DatabaseReference userRef = database.getReference().child("user");
    private final static DatabaseReference liveAloneRef = database.getReference().child("livealone");

    private Context context;


    public FirebaseProfile(Context context) {
        this.context = context;
    }



    //서버에서 필요한 데이터를 가져옴 (현재 진행중인 같이먹기 , 상대방 정보(uid) 등
    public void getCurrentUserAndLivealoneInMainActivity(final String uidOfCurrentUser, final TextView nameText){

        userRef.child(uidOfCurrentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                MainActivity.setCurrentUser(user);
                MyPageFragment.setView();
                if(((MainActivity)context).getProfileImageView()!=null){
                    ((MainActivity)context).getProfileImageView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra("uid", uidOfCurrentUser);
                            intent.putExtra("name", user.getName());
                            intent.putExtra("star",  String.format("%.2f", user.getStar()));
                            intent.putExtra("loc", user.getLocation());
                            intent.putExtra("count", user.getLivealoneCount());
                            context.startActivity(intent);
                        }
                    });
                }

                nameText.setText(user.getName());


                if(user.getCurrent_livealone() != null){
                    liveAloneRef.child(user.getCurrent_livealone()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            LiveAlone liveAlone = dataSnapshot.getValue(LiveAlone.class);
                            MainActivity.setLiveAloneOfCurrentUser(liveAlone);

                            if(liveAlone.getUidOfAloneTaker()!=null){
                                //케어가 진행 중일 때
                                if(liveAlone.getUidOfAloneTaker().equals(uidOfCurrentUser)){
                                    MainActivity.setUidOfOpponentUser(liveAlone.getUid()); //내가(current user가) 케어테이커일 경우 상대방은 uid
                                } else {
                                    MainActivity.setUidOfOpponentUser(liveAlone.getUidOfAloneTaker()); //내가 작성자인 경우
                                }
                                userRef.child(MainActivity.getUidOfOpponentUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User opponentUser = dataSnapshot.getValue(User.class);
                                        MainActivity.setOpponentUser(opponentUser);

                                        LiveAloneFragment.setViews(true);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                //진행중인 같이먹기는 없지만, 같이먹기를 등록했을 경우
                                LiveAloneFragment.setViews(false);
                            }

                            if(LiveAloneFragment.getHiddenLayout() != null) {
                                LiveAloneFragment.getHiddenLayout().setVisibility(View.VISIBLE); //데이터를 받아왔을 때 화면을 띄움

                            }
                            if(LiveAloneFragment.getNoneAloneLayout() != null){
                                LiveAloneFragment.getNoneAloneLayout().setVisibility(View.GONE);

                            }

                            if(liveAlone.getWaitingForDeletion() != null && !liveAlone.getWaitingForDeletion().equals(uidOfCurrentUser)){
                                //상대방이 삭제 요청을 보낸 경우
                                MessageDialogFragment.setContext(context);
                                MessageDialogFragment.setKeyAndUid(liveAlone.getKey(), uidOfCurrentUser);
                                MessageDialogFragment.showDialog(MessageDialogFragment.DELETION_CHECK, context);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } else {
                    if(LiveAloneFragment.getNoneAloneLayout() != null){
                        LiveAloneFragment.getNoneAloneLayout().setVisibility(View.VISIBLE);
                    }
                    if(LiveAloneFragment.getHiddenLayout() != null) {
                        LiveAloneFragment.getHiddenLayout().setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //해당 같이먹기에 대한 평가를 내림
    public void evaluate(String uidOfOpponentUser, final Estimation estimation){
        if(uidOfOpponentUser == null || estimation == null || MainActivity.getCurrentUser() == null)
            return;

        ProgressDialogHelper.show(context);

        /*
            1. user의 "LiveAloneRecords"의 values 수를 구함 (평균 평점을 내리기 위함)
            2. user의 "LiveAloneRecords"에 LiveAlone의 key로 estimation을 push한다.
            3. (OnCompleteListner) user의 평균 평점을 갱신한다.
         */
        final DatabaseReference opponentRef = userRef.child(uidOfOpponentUser);

        opponentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProgressDialogHelper.dismiss();
                int count = dataSnapshot.child("livealoneCount").getValue(Integer.class);
                double averageScore = dataSnapshot.child("star").getValue(Double.class); //평균 평점을 구한 뒤
                averageScore = (averageScore*count + (estimation.getRating())/1)/(count+1);

                opponentRef.child("livealoneCount").setValue(count+1);
                opponentRef.child("star").setValue(averageScore);
                opponentRef.child("livealoneRecords").push().setValue(estimation);

                MessageDialogFragment.setContext(context);
                MessageDialogFragment.setEstimation(estimation);
                MessageDialogFragment.showDialog(MessageDialogFragment.ESTIMATION_SUCCESS, context);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ProgressDialogHelper.dismiss();
            }
        });

    }

    public void readEstimations(String uid, final RecyclerView recyclerView){
        //해당 유저의 평가 기록을 가져옴
        if(recyclerView ==null || uid == null)
            return;

        userRef.child(uid).child("livealoneRecords").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List list = new ArrayList();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    list.add(ds.getValue(Estimation.class));
                }

                if(list.size()==0)
                    return;

                Collections.reverse(list); //평가를 최신순으로 보여준다.

                EstimationAdapter estimationAdapter = new EstimationAdapter(context, list);
                recyclerView.setLayoutManager(new MyLinearLayoutManager(context));
                recyclerView.setAdapter(estimationAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static DatabaseReference getUserRef() {
        return userRef;
    }
}
