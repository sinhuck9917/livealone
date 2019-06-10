package com.example.livealone4.MachineLearning;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.livealone4.Models.User;

import java.util.ArrayList;
import java.util.List;
/*

    dataset을 만들기 위한 클래스
    parseInto() : csv형식으로 변환하기 위해 디비 내용을 적절하게 파싱
    generateTrainingData(User user) : Supervised Learning 을 위한 사전 데이터셋 생성
    generateTraningDataFromUsers() : 실제 유저 데이터로부터 파싱 형식에 맞는 데이터셋 추출
    clearDataset() : 유저 데이터셋을 지움 (이미 평가가 완료된 유저들은 제거한다.)

 */
public class DataGenerator {
    //Supervised Learning 을 위한 사전 데이터셋 생성
    //DataGeneratorActivity에서 받은 User를 디비에 삽입

    FirebaseDatabase database;

    public DataGenerator(){
        database = FirebaseDatabase.getInstance();
    }

    public void generateTrainingData(final User user){
        //관리자 전용 기능이기 때문에 지역변수로 처리
        final DatabaseReference ref = database.getReference().child("train_data");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<User> userList = (List<User>) dataSnapshot.getValue(Object.class);

                if(userList == null){
                    userList = new ArrayList<>();
                }
                user.setUid(userList.size()+"");
                userList.add(user);
                ref.setValue(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //실제 유저 데이터로부터 데이터셋을 생성
    //같이먹기 카운트가 1회 이상인 유저 정보만 데이터셋에 포함시킨다.
    @Deprecated //파이썬 코드로 대체됨
    public void generateTraningDataFromUsers(){
        DatabaseReference userRef = database.getReference().child("user");
        final DatabaseReference userDatasetRef = database.getReference().child("train_data_users");
        final List<User> userList = new ArrayList<>();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //유저 데이터를 불러와서 같이먹기 횟수가 1회 이상인 유저를 리스트에 넣음
                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    User user = ds.getValue(User.class);

                    if(user.getLivealoneCount() > 0) {

                        User userData = new User();
                        userData.setUid(user.getUid());
                        userData.setType0(user.getType0());
                        userData.setType1(user.getType1());
                        userData.setStar(user.getStar());
                        userData.setLivealoneCount(user.getLivealoneCount());
                        userData.setSuspensions(user.getSuspensions());

                        userList.add(userData);

                    }
                }

                userDatasetRef.setValue(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void clearUserDataset(){
        database.getReference().child("train_data_users").removeValue();
    }

    public void clearSupervisedDataset(){
        database.getReference().child("train_data").removeValue();
    }
}
