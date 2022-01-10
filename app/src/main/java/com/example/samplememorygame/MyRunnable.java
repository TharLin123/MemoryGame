package com.example.samplememorygame;

import android.graphics.Bitmap;
import android.widget.ImageButton;

public class MyRunnable implements Runnable{
    protected int k;
    ImageButton[] imgBtns;
    Bitmap savedImage;

    public MyRunnable(int k, ImageButton[] imgBtns, Bitmap savedImage){
        this.k = k;
        this.imgBtns = imgBtns;
        this.savedImage = savedImage;
    }

    @Override
    public void run(){
        imgBtns[k].setImageBitmap(savedImage);
    }

}
