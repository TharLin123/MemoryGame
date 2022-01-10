package com.example.samplememorygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.sax.Element;
import android.widget.ImageButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloader {
    private Context context;
    private ImageButton[] imgBtns;


    public ImageDownloader(Context context, ImageButton[] imgBtns){
        this.context = context;
        this.imgBtns = imgBtns;
    }

    private String[] links = new String[20];

    protected boolean downloadImage(String url){
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try{
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("img[src]");
            if(elements.size()<6){
                return false;
            }else{
                for(int i=0; i<20;i++){
                    links[i] = elements.get(i).attr("src");
                    Bitmap savedImage = saveImage(links[1],i+1,dir);
                    imgBtns[i].setImageBitmap(savedImage);

                }
                return true;
            }
        } catch(Exception e){
            return false;
        }
    }

    protected Bitmap saveImage(String link, int i, File dir){
        Bitmap imageSaved = null;
        try{
            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            InputStream in = conn.getInputStream();

            String destFileName = "img" + i;
            File destFile = new File(dir,destFileName);
            FileOutputStream out = new FileOutputStream(destFile);

            byte[] buf = new byte[1024];

            int bytesRead = -1;

            while((bytesRead = in.read(buf)) != -1){
                out.write(buf,0,bytesRead);
            }

            imageSaved = BitmapFactory.decodeFile(destFile.getAbsolutePath());

            out.close();
            in.close();
            return imageSaved;

        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


}
