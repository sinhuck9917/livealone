package com.example.livealone4.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.livealone4.MachineLearning.DataGenerator;
import com.example.livealone4.Models.User;
import com.example.livealone4.R;

public class DataGeneratorActivity extends AppCompatActivity {

    /*

        Supervised Learning 을 위한 데이터셋을 만드는 액티비티

        features :
        1. star (총 평점)
        2. livealone count (진행 횟수)
        3. suspensions (중단 횟수)
        5. type ( 0일 경우 일반, 1일 경우 비정상)

    */
    EditText star, liveCount, suspensions, type;
    Button submitBtn;
    DataGenerator dataGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_generator);

        final User user = new User();
        dataGenerator = new DataGenerator();

        star = findViewById(R.id.star_edt);
        liveCount = findViewById(R.id.count_edt);
        suspensions = findViewById(R.id.sus_edt);
        type = findViewById(R.id.type_edt);
        submitBtn = findViewById(R.id.submt_btn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    user.setStar(Double.parseDouble(star.getText().toString()));
                    user.setLivealoneCount(Integer.valueOf(liveCount.getText().toString()));
                    user.setSuspensions(Integer.valueOf(suspensions.getText().toString()));

                    if(Integer.valueOf(type.getText().toString()) == 0){
                        //normal user [1 0]
                        user.setType0(1);
                        user.setType1(0);
                    } else {
                        //abnormal user [0 1]
                        user.setType0(0);
                        user.setType1(1);
                    }

                    dataGenerator.generateTrainingData(user);
                    Toast.makeText(DataGeneratorActivity.this, "완료", Toast.LENGTH_SHORT).show();

                } catch (Exception e){

                    Toast.makeText(DataGeneratorActivity.this, "타입 불일치. 인풋을 확인하세요.", Toast.LENGTH_SHORT).show();

                }
            }
        });

        findViewById(R.id.clr_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataGenerator.clearUserDataset();
            }
        });

        findViewById(R.id.existing_usr_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataGenerator.generateTraningDataFromUsers();
            }
        });
    }
}
