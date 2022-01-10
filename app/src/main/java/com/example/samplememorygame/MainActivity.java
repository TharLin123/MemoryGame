package com.example.samplememorygame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button fetchBtn;
    private EditText url;
    private Thread bkgdThread;
    private int[] imgBtnsID = new int[20];
    private ImageButton[] imgBtns = new ImageButton[20];
    private String[] links = new String[20];
    private Context context = this;
    private ArrayList<Bitmap> savedImages = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView progressText;
    private int counter = 0;
    private ArrayList<Integer> selection = new ArrayList<>();
    private int progressBarMax = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchBtn = findViewById(R.id.fetchBtn);
        url = findViewById(R.id.enterURL);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        for(int i =0; i<20; i++){
            imgBtnsID[i] = getResources().getIdentifier("img"+ (i+1),"id",this.getPackageName());
            imgBtns[i] = findViewById(imgBtnsID[i]);
            imgBtns[i].setOnClickListener(this);
        }
        fetchBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.fetchBtn){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(url.getWindowToken(), 0);

            selection.clear();
            savedImages.clear();

            for (ImageButton btn : imgBtns){
                btn.setImageResource(0);
                btn.setForeground(null);
            }

            progressBar.setProgress(0);
            progressText.setText("");
            //progressText.setText(getString(R.string.progressText,0,progressBar.getMax()));

           if(bkgdThread !=null){
               bkgdThread.interrupt();
            }
               File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
               bkgdThread = new Thread(new Runnable() {
                   @Override
                   public void run() {
                       try {
                           String input = url.getText().toString();
                           if(input.isEmpty()|| input == null){
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       progressText.setText("You forgot to fill up the url field");
                                       progressBar.setVisibility(View.GONE);
                                   }
                               });
                               return;
                           } else{
                               Document doc = Jsoup.connect(input).get();
                               Elements elements = doc.select("img[src$=.jpg]");
                               if(elements.size()<6){
                                   runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           progressText.setText("Sorry. There are insufficient images available to" +
                                                   " start the game. Please try another search.");
                                       }
                                   });
                                   return;
                               } else if(elements.size()<20){
                                   progressBarMax = elements.size();
                                   progressBar.setMax(progressBarMax);
                               } else{
                                   progressBarMax = 20;
                                   progressBar.setMax(progressBarMax);
                               }

                               for(int i=0; i<progressBarMax;i++){
                                   if(Thread.interrupted()){
                                       runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               for (ImageButton btn : imgBtns){
                                                   btn.setImageResource(0);
                                               }
                                               progressBar.setProgress(0);
                                               progressText.setText("");
                                               //progressText.setText(getString(R.string.progressText,0,progressBarMax));
                                           }
                                       });
                                       return;
                                   }
                                   links[i] = elements.get(i).attr("src");
                                   saveImage(links[i],i+1,dir);
                                   Bitmap imageToAdd = savedImages.get(i);
                                   int k = i;
                                   runOnUiThread(new MyRunnable(k,imgBtns,imageToAdd) {
                                       @Override
                                       public void run() {
                                           imgBtns[k].setImageBitmap(imageToAdd);
                                           imgBtns[k].setBackgroundResource(0);
                                           progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#74F94F")));
                                           progressBar.setVisibility(View.VISIBLE);
                                           progressBar.setProgress(k+1);
                                           if(k+1 < 20){
                                               progressText.setText(getString(R.string.progressText,k+1,progressBarMax));
                                           } else{
                                               progressText.setText("Please select 6 images to start the game.");
                                           }
                                       }
                                   });
                               }
                           }
                       }
                       catch(IllegalArgumentException e){
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   progressText.setText("Please enter a valid search term");
                               }
                           });
                       }
                       catch (Exception e){
                           e.printStackTrace();
                       }
                   }
               });
               bkgdThread.start();

        } else if(Arrays.asList(Arrays.stream(imgBtnsID).boxed().toArray(Integer[]::new))
                .contains(Integer.valueOf(id))){
            if(progressBar.getProgress() <progressBarMax){
                Toast.makeText(MainActivity.this, "Please wait for all images to be fully" +
                        " downloaded.", Toast.LENGTH_LONG).show();
            } else if (progressBar.getProgress() == progressBarMax){
                Integer selected = Integer.valueOf(getArrayIndex(imgBtnsID,id)+1);
                ImageButton selectedBtn = findViewById(id);
                if(!selection.contains(selected)){
                    selection.add(selected);
                    progressText.setText(getString(R.string.select,selection.size()));
                    selectedBtn.setForeground(ResourcesCompat.getDrawable(getResources(),R.drawable.tick,null));
                    if(selection.size() == 6){
                        startGame(selection);
                    }
                } else{
                    selection.remove(selected);
                    progressText.setText(getString(R.string.select,selection.size()));
                    selectedBtn.setForeground(null);
                }

            }
        }
    }

    protected void startGame(ArrayList<Integer> selectedIndices){
        Intent intent = new Intent(MainActivity.this, Game.class);
        intent.putExtra("idx",selectedIndices);
        startActivity(intent);
    }

    protected int getArrayIndex(int[] arr, int value){
        int k=0;
        for(int i=0;i<arr.length;i++){

            if(arr[i]==value){
                k=i;
                break;
            }
        }
        return k;
    }

    protected void saveImage(String link, int i, File dir){
        Bitmap imageSaved = null;
        InputStream in = null;
        FileOutputStream out = null;
        try{
            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            in = conn.getInputStream();

            String destFileName = "img" + i;
            File destFile = new File(dir,destFileName);
            out = new FileOutputStream(destFile);

            byte[] buf = new byte[1024];

            int bytesRead = -1;

            while((bytesRead = in.read(buf)) != -1){
                out.write(buf,0,bytesRead);
            }

            imageSaved = BitmapFactory.decodeFile(destFile.getAbsolutePath());

            savedImages.add(imageSaved);

            out.close();
            in.close();

        }catch (InterruptedIOException e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    savedImages.clear();
                    for (ImageButton btn : imgBtns){
                        btn.setImageResource(0);
                        progressBar.setProgress(0);
                        progressText.setText("");
                        Toast.makeText(MainActivity.this, "Download was interrupted", Toast.LENGTH_LONG);
                        //progressText.setText(getString(R.string.progressText,0,progressBarMax));
                    }
                }
            });

        }
        catch(Exception e){
            e.printStackTrace();

        }
        finally{
            if(in != null){
                try{
                    in.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            if(out != null){
                try{
                    out.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}