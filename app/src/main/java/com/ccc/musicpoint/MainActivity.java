package com.ccc.musicpoint;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ccc.musicpoint.view.MusicPoint;

public class MainActivity extends AppCompatActivity {

    private MusicPoint musicPoint;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicPoint = findViewById(R.id.id_music);
        btn = findViewById(R.id.id_start);

        musicPoint.setImageResource(R.drawable.mv1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicPoint.isStart()){
                    btn.setText("pause");
                    musicPoint.pause();
                }else{
                    btn.setText("play");
                    musicPoint.start();
                }
            }
        });
    }
}
