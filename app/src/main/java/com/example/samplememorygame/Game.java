package com.example.samplememorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Game extends AppCompatActivity implements View.OnClickListener {

    ArrayList<Integer> shuffled;
    private int[] gameBtnsID = new int[12];
    private ImageButton[] gameBtns = new ImageButton[12];
    private TextView result;
    private TextView infoText;
    private Chronometer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        result = findViewById(R.id.result);
        result.setText(getString(R.string.result,0));

        infoText = findViewById(R.id.infoText);
        timer = findViewById(R.id.timer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        for(int i =0; i<12; i++){
            gameBtnsID[i] = getResources().getIdentifier("gameBtn"+ (i+1),"id",this.getPackageName());
            gameBtns[i] = findViewById(gameBtnsID[i]);
            gameBtns[i].setOnClickListener(this);
        }

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        ArrayList<Integer> selectedIndices = (ArrayList<Integer>) getIntent().getSerializableExtra("idx");
        shuffled = shuffleList(selectedIndices);

        for(int i=0; i<12; i++){
            ImageButton btn = findViewById(gameBtnsID[i]);
            int index = shuffled.get(i).intValue();
            File fileSource =new File(dir,"img"+index);
            Bitmap pic = BitmapFactory.decodeFile(fileSource.getAbsolutePath());
            btn.setImageBitmap(pic);
        }
    }

    @Override
    public void onClick(View view){
        int id = view.getId();
    }


    protected ArrayList<Integer> shuffleList(ArrayList<Integer> arr){
        ArrayList<Integer> shuffled = new ArrayList<>();

        for(int i=0; i<6; i++){
            shuffled.add(arr.get(i));
            shuffled.add(arr.get(i));
        }
        Collections.shuffle(shuffled);
        return shuffled;
    }
}