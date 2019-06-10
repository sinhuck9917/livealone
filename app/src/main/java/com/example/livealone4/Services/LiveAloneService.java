package com.example.livealone4.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.livealone4.Utils.LiveAloneNotification;

public class LiveAloneService extends Service {

    private static String uid;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef = database.getReference().child("user");
    private int previousNumOfMessages = 0;
    private boolean currentLiveAlone;
    private boolean isAvailable = true;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BackGroundThread extends Thread{

        @Override
        public void run() {
            super.run();

            while (true && isAvailable){
                try {
                    Thread.sleep(1000); //1초마다 진행

                    userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot == null){
                                isAvailable = false;
                                return;
                            }

                            //비활성화일 때만 알림
                            if(currentLiveAlone && dataSnapshot.child("isOnline").getValue(Boolean.class)==null || !dataSnapshot.child("isOnline").getValue(Boolean.class)){

                                //새 메시지 알림
                                final Integer newMessages = dataSnapshot.child("newMessages").getValue(Integer.class);
                                if(newMessages > 0 && previousNumOfMessages!=newMessages){
                                    previousNumOfMessages = newMessages; //계속해서 띄우는 현상을 방지

                                    LiveAloneNotification.notifyNewMessage(LiveAloneService.this, "새로운 메시지가 "+ newMessages + "건 도착했습니다!");
                                }

                            }

                            //애플리케이션 실행 여부에 상관 없이 알림
                            //중단을 요청했을 때 알림
                            if(dataSnapshot.child("waitingForDeletion").getValue(String.class) != null){
                                userRef.child(uid).child("waitingForDeletion").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        LiveAloneNotification.notifyNewMessage(LiveAloneService.this, "상대방이 같이먹기를 중단을 요청했습니다.");

                                    }
                                });
                            }

                            //현재 진행중인 같이먹기가 종료되거나, 새로 생성되었을 때 알림
                            if(currentLiveAlone && dataSnapshot.child("current_livealone").getValue(String.class) == null){
                                currentLiveAlone = false;
                                LiveAloneNotification.notifyNewMessage(LiveAloneService.this, "같이먹기가 종료되었습니다.");
                            } else if(!currentLiveAlone && dataSnapshot.child("current_livealone").getValue(String.class) != null){
                                currentLiveAlone = true;
                                LiveAloneNotification.notifyNewMessage(LiveAloneService.this, "새로운 같이먹기가 생성되었습니다!");
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null)
            uid = intent.getStringExtra("uid");

        if(uid!=null) {

            //초기 user의 값들을 저장시킨다.
            userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("current_livealone").getValue(String.class) == null){
                        currentLiveAlone = false; //같이먹기가 존재하지 않음
                    } else {
                        currentLiveAlone = true; //같이먹기가 존재함
                    }

                    BackGroundThread thread = new BackGroundThread();
                    thread.start();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }

        return super.onStartCommand(intent, flags, startId);
    }

    public static String getUid() {
        return uid;
    }

}