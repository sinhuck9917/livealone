package com.example.livealone4.Firebase;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.livealone4.Activities.MainActivity;
import com.example.livealone4.Utils.MyLinearLayoutManager;
import com.example.livealone4.Utils.ProgressDialogHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.livealone4.Adapters.CandidateAdapter;
import com.example.livealone4.Adapters.LiveAloneAdapter;
import com.example.livealone4.Fragments.LiveAloneCreationFragment;
import com.example.livealone4.Fragments.MessageDialogFragment;
import com.example.livealone4.Models.Chat;
import com.example.livealone4.Models.LiveAlone;
import com.example.livealone4.Models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class FirebaseLiveAlone {

    /*
        FirebaseLiveAlone : 컨트롤러

        H관련 CRUD
        1. writeLivealone() C
        2. destroyLivealone() D
        3. refreshLiveAlone() R
        4. updateLivealone() U

        Candidates 관련
        1. requestLiveAlone() C / D
        2. initTextOfRequestButton() : 요청 상태에 따라 뷰 초기화
        3. refreshCandidates() : R
        4.
     */

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference liveAloneRef = database.getReference().child("livealone");
    private final DatabaseReference userRef = database.getReference().child("user");

    private Context context;
    private RecyclerView liveAloneRecyclerView, candidatesRecyclerView;
    private final static List<LiveAlone> liveAloneList = new ArrayList<>();
    private final static List<User> userList = new ArrayList<>();
    private final static List<User> candidateList = new ArrayList<>();
    private final static List<LiveAlone> filteredLiveAloneList = new ArrayList<>();
    private final static List<User> filteredUserList = new ArrayList<>();

    private static final String CANDIDATES = "candidates";
    private static final String CURRENT_LIVE_ALONE = "current_livealone";
    private static final String UID_OF_ALONETAKER = "uidOfAloneTaker";
    private static final String WAITING_FOR_DELETION = "waitingForDeletion"; //삭제 시도한 uid 저장

    public FirebaseLiveAlone(Context context) {

        this.context = context;

    }

    //같이먹기를 키값으로 탐색
    public LiveAlone searchLiveAlone(String key){
        Iterator<LiveAlone> it = liveAloneList.iterator();

        while (it.hasNext()){
            LiveAlone liveAlone = it.next();
            if(key.equals(liveAlone.getKey())){
                return liveAlone;
            }
        }
        return null;
    }

    public void setLiveAloneRecyclerView(RecyclerView liveAloneRecyclerView) {
        this.liveAloneRecyclerView = liveAloneRecyclerView;
    }

    public RecyclerView getLiveAloneRecyclerView() {
        return liveAloneRecyclerView;
    }

    public RecyclerView getCandidatesRecyclerView() {
        return candidatesRecyclerView;
    }

    public void setCandidatesRecyclerView(RecyclerView candidatesRecyclerView) {
        this.candidatesRecyclerView = candidatesRecyclerView;
    }

    //CREATE HOME CARE
    public void writeLiveAlone(final String uid, final LiveAlone liveAlone, final LiveAloneCreationFragment fragment){
        ProgressDialogHelper.show(context);

        /*
            Process
            0. 프로그레스 다이얼로그 띄움
            1. root/user/uid/current_livealone이 null인지 아닌지 확인 (이미 올린 게시물이 있는지 없는지 확인)
            2. null이면 생성, 이미 존재할 경우 생성하지 않음
            3. (생성하였으면 리프레쉬)
            4. 결과를 다이얼로그로 띄움
            5. 프로그레스 다이얼로그 dismiss
         */

        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(CURRENT_LIVE_ALONE).getValue()==null) {

                    DatabaseReference specificLiveAloneRef = liveAloneRef.push();
                    liveAlone.setKey(specificLiveAloneRef.getKey());
                    specificLiveAloneRef.setValue(liveAlone).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ProgressDialogHelper.dismiss();
                            MessageDialogFragment.setLiveAloneCreationFragment(fragment);
                            MessageDialogFragment.showDialog(MessageDialogFragment.LIVEALONE_CREATION_SUCCESS,context);
//                            refreshLiveAlone(null); //리프레쉬
                            ((MainActivity)context).refresh(true, null); //리프레쉬
                        }
                    });
                    userRef.child(uid).child(CURRENT_LIVE_ALONE).setValue(specificLiveAloneRef.getKey());

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    //DESTROY HOME CARE
    public void destroyLiveAlone(final String key, final String uid, final boolean calledByMain){

        /*
            상황
            1. 상대방과 매칭이 되지 않은 경우
                -> 걍 삭제 (db의 user와 LiveAlone에서 삭제하고 finish and refresh)
            2. 상대방과 매칭이 이미 된 경우
                -> 상대방도 삭제 신청을 한 경우 삭제
                -> 그렇지 않을 경우 삭제 신청 상태로 전환
         */

        ProgressDialogHelper.show(context);
        liveAloneRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LiveAlone liveAlone = dataSnapshot.getValue(LiveAlone.class);
                if(liveAlone==null){
                    Toast.makeText(context, "존재하지 않는 게시물입니다. ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(liveAlone.getUidOfAloneTaker() == null){
                    userRef.child(uid).child(CURRENT_LIVE_ALONE).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            liveAloneRef.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    ProgressDialogHelper.dismiss();
                                    Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show();

                                    if(!calledByMain) {
                                        ((Activity) context).setResult(MainActivity.RESULT_REFRESH);
                                        ((Activity) context).finish();
                                    } else {
                                        ((MainActivity)context).refresh(true, null);
                                    }

                                }
                            });
                        }
                    });
                } else if(liveAlone.getWaitingForDeletion() != null && !liveAlone.getWaitingForDeletion().equals(uid)){
                    //신청은 됐지만 current user가 아닌 경우 (상대방이 요청한 경우)

                    FirebaseMessenger.destroyChat(key); //Chat 제거

                    userRef.child(uid).child(CURRENT_LIVE_ALONE).removeValue();
                    userRef.child(uid).child("newMessages").setValue(0);
                    userRef.child(liveAlone.getWaitingForDeletion()).child(CURRENT_LIVE_ALONE).removeValue();
                    userRef.child(liveAlone.getWaitingForDeletion()).child("newMessages").setValue(0);

                    liveAloneRef.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ProgressDialogHelper.dismiss();
                            Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            refreshLiveAlone(null);
                            ((MainActivity)context).refresh(true, null);
                        }
                    });

                } else {
                    //같이먹기 도중, 상대방에게 삭제 요청을 해야하는 경우
                    if(MainActivity.getUidOfOpponentUser()!=null)
                        userRef.child(MainActivity.getUidOfOpponentUser()).child(WAITING_FOR_DELETION).setValue(uid);
                    else
                        return;

                    userRef.child(uid).child("suspensions").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Integer suspensions = dataSnapshot.getValue(Integer.class);
                            userRef.child(uid).child("suspensions").setValue(suspensions+1); //위반 횟수를 1 increment
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    liveAloneRef.child(key).child(WAITING_FOR_DELETION).setValue(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ProgressDialogHelper.dismiss();
                            MessageDialogFragment.showDialog(MessageDialogFragment.DELETION_WAITING,context);
                        }
                    });



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //READ HOME CARES
    public void refreshLiveAlone(@Nullable final SwipeRefreshLayout swipeRefreshLayout){
        /*
            1. 게시물 목록을 갱신한다.
            2. 갱신된 게시물의 작성자 uid 정보로부터 유저 리스트(writers)를 갱신한다.
            3. 리사이클러뷰에 적용
         */
        if(liveAloneRecyclerView == null)
            return;

        liveAloneList.clear();
        userList.clear();


        liveAloneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    LiveAlone liveAlone = ds.getValue(LiveAlone.class);
                    liveAloneList.add(liveAlone);
                }

                Collections.reverse(liveAloneList);

                //작성된 게시물 목록으로부터 유저 리스트도 갱신
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(swipeRefreshLayout!=null)
                            swipeRefreshLayout.setRefreshing(false);
                        Iterator<LiveAlone> it = liveAloneList.iterator();
                        while (it.hasNext()){
                            LiveAlone liveAlone = it.next();
                            userList.add(dataSnapshot.child(liveAlone.getUid()).getValue(User.class));
                        }

                        LiveAloneAdapter liveAloneAdapter = new LiveAloneAdapter(liveAloneList, userList, context);
                        liveAloneRecyclerView.setLayoutManager(new MyLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                        liveAloneRecyclerView.setAdapter(liveAloneAdapter);
                        if(MainActivity.getProgressBarLayout()!=null && MainActivity.getProgressBarLayout().getVisibility() != View.GONE)
                            MainActivity.getProgressBarLayout().setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void filter(){

        LiveAloneAdapter liveAloneAdapter = new LiveAloneAdapter(filteredLiveAloneList, filteredUserList, context);
        liveAloneRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        liveAloneRecyclerView.setAdapter(liveAloneAdapter);

    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    /* 여기서부터 Candidates 관련 */

    public void pickCandidate(final String key, final String uidOfCandidate){

        /*
            0. 프로그레스바 띄움
            1. 이미 케어하는 사람이 존재할 경우 리턴
            2. LiveAlone/key/uidOfCareTaker 에 uid 갱신
            3. Chat 생성
            4. request code를 포함하여 finish (갱신되게)
         */


        ProgressDialogHelper.show(context, "등록 중입니다...");

        //상대방이 이미 같이먹기를 진행 중인지 확인한다.
        userRef.child(uidOfCandidate).child(CURRENT_LIVE_ALONE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue(String.class) != null){
                    ProgressDialogHelper.dismiss();
                    Toast.makeText(context, "상대방이 이미 같이먹기 서비스를 진행 중입니다.", Toast.LENGTH_SHORT).show();
                    return;

                } else {
                    liveAloneRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ProgressDialogHelper.dismiss(); //임시

                            if(dataSnapshot.child("uidOfAloneTaker").getValue(String.class) != null){
                                Toast.makeText(context, "이미 같이먹기 서비스가 진행 중입니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //게시물의 케어테이커에 케어테이커의 uid 추가
                            //케어테이커의 현재 게시물에 key 추가
                            userRef.child(uidOfCandidate).child(CURRENT_LIVE_ALONE).setValue(key);
                            liveAloneRef.child(key).child(UID_OF_ALONETAKER).setValue(uidOfCandidate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    //메시지 생성
                                    Chat chat = new Chat(key, MainActivity.getUidOfCurrentUser(), uidOfCandidate);
                                    FirebaseMessenger.writeChat(chat);

                                    //생성 후 성공 메시지 띄움
                                    MessageDialogFragment.setContext(context);
                                    MessageDialogFragment.showDialog(MessageDialogFragment.CANDIDATE_PICK_SUCCESS, context);

                                }
                            });
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void refreshCandidates(String key){
        if(candidatesRecyclerView==null)
            return;
        /*
            1. key에 해당하는 게시물의 candidates uid를 불러온다.
            2. 불러오면 List에 넣는다
            3. List의 uid에 해당되는 User를 userList에 추가한다.
         */

        liveAloneRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final List<String> uidOfCandidates = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.child(CANDIDATES).getChildren()){
                    uidOfCandidates.add(ds.getValue(String.class)); //후보자의 키를 넣음
                }

                //유저 데이터스냅샷을 불러온 다음, 리스트에 해당하는 객체만 넣는다.
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        candidateList.clear();

                        Iterator<String> it = uidOfCandidates.iterator(); //유저 리스트로부터

                        while (it.hasNext()){
                            String candidateUid = it.next();
                            candidateList.add(dataSnapshot.child(candidateUid).getValue(User.class));

                        }
                        CandidateAdapter candidateAdapter = new CandidateAdapter(candidateList, context);
                        candidatesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                        candidatesRecyclerView.setAdapter(candidateAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    public void requestLiveAlone(final String key, final String uid, final Button requestButton){
        /*
            requestLiveAlone : 같이먹기 신청 기능
            1. ProgressDialog를 띄움
            2. 신청 내역이 없다면 신청자 목록에 자신의 정보(uid)를 추가한다.
            3. 신청 내역이 있다면 신청자 목록에서 자신의 정보를 제거한다.
            4. 1이나 3의 결과에 따라 request Button의 text를 수정한다, (신청하기 <-> 신청 취소)
            5. 자신에게 신청했을 경우 토스트 띄우고 리턴
         */

        ProgressDialogHelper.show(context, "같이먹기 요청 중입니다.");

        liveAloneRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProgressDialogHelper.dismiss();
                if(dataSnapshot.getValue()==null){
                    MessageDialogFragment.showDialog(MessageDialogFragment.LIVEALONE_NULL, context);
                    return;
                }

                if(dataSnapshot.child("uid").getValue(String.class).equals(uid)){
                    Toast.makeText(context, "자신에게 신청할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //자신이 있는지 탐색하기
                for(DataSnapshot ds : dataSnapshot.child(CANDIDATES).getChildren()) {
                    if(ds.getValue(String.class).equals(uid)) {
                        liveAloneRef.child(key).child(CANDIDATES).child(ds.getKey()).removeValue();
                        requestButton.setText("신청하기");
                        Toast.makeText(context, "신청이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                //신청 기록이 없을 경우 신청
                liveAloneRef.child(key).child(CANDIDATES).push().setValue(uid);
                requestButton.setText("신청취소");
                Toast.makeText(context, "신청이 완료되었습니다!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void initTextOfRequestButton(final String key, final String uid, final Button contactButton){
        /*
            1. 같이먹기 마감 상태에 따라 버튼의 visibility와 title 설정
            2. 신청했으면 "신청하기", 신청하지 않으면 "신청취소"로 텍스트 바꾸기
         */

        liveAloneRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()==null){
                    MessageDialogFragment.showDialog(MessageDialogFragment.LIVEALONE_NULL, context);
                    return;
                }

                //자신이 있는지 탐색하기
                for (DataSnapshot ds : dataSnapshot.child(CANDIDATES).getChildren()) {
                    if (ds.getValue(String.class).equals(uid)) {
                        //신청 기록이 있으면 신청 취소로
                        contactButton.setText("신청취소");
                        return;
                    }
                }
                //신청 기록이 없을 경우 신청
                contactButton.setText("신청하기");
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public User searchUser(String key) {

        Iterator<User> it = userList.iterator();
        while (it.hasNext()){
            User user = it.next();
            if(key.equals(user.getCurrent_livealone())){
                return user;
            }
        }
        return null;
    }

    public static List<LiveAlone> getFilteredLiveAloneList() {
        return filteredLiveAloneList;
    }

    public static List<User> getFilteredUserList() {
        return filteredUserList;
    }

    public static List<LiveAlone> getLiveAloneList() {
        return liveAloneList;
    }

    public static List<User> getUserList() {
        return userList;
    }
}
