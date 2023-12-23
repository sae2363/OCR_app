package com.example.test_gui;
//to do fix split error as if the e is lower then the t then it dosnt work
import static android.graphics.Bitmap.createScaledBitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class OCR_main {
    @SuppressLint("StaticFieldLeak")
    static Context r;
    static String out = "";

    public static void main(String[] args) throws IOException {
        OCR_Processor pro = new OCR_Processor();
        Bitmap bm = BitmapFactory.decodeResource(r.getResources(), R.drawable.img1);
        out = pro.processArray(imgToArray(scaleBI(bm, 1), 25));
    }
    public static void main2(Bitmap b) throws IOException {
        OCR_Processor pro = new OCR_Processor();
        //Bitmap bm = BitmapFactory.decodeResource(r.getResources(), R.drawable.the);
        //error in img to array out of bound
        double value=1.0;
        if(b.getWidth()>2000)
            value=0.5;
        //print2DArray(imgToArray(scaleBI(b, value),25));
        System.out.println(b.getHeight()+" "+b.getWidth());
        out = pro.processArray(imgToArray(scaleBI(b, value), 25));
    }

    public static Bitmap scaleBI(Bitmap pic, double scale) {
        if(scale==1.0)
        {
            return pic;
        }
        return createScaledBitmap(pic, (int)(pic.getWidth() * scale), (int)(pic.getHeight() * scale), true);
    }

    public static int[][] imgToArray(Bitmap pic, int t) {
        int h = pic.getHeight(), w = pic.getWidth();
        int avgWhite = getAvgColor(pic);
        int[][] a = new int[h + 2][w + 2];
        for (int i = 1; i < h; i++) {
            for (int j = 1; j < w; j++) {
                if (hexIsBlack(pic, j, i, avgWhite)) {
                    a[i][j] = 1;
                }
            }
        }
        return a;
    }

    public static int getAvgColor(Bitmap img) {
        double avgR = 0, avgG = 0, avgB = 0;
        int total = 1;
        for (int i = 0; i < img.getHeight(); i+=2) {
            for (int j = 0; j < img.getWidth(); j+=2) {
                int pixel = img.getPixel(j, i);

                // Extract RGB components
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                avgR = (avgR * (total-1) + red) / total;
                avgG = (avgG * (total-1) + blue) / total;
                avgB = (avgB * (total-1) + green) / total;
                total++;
            }
        }
        return Color.rgb((int) avgR, (int) avgG, (int) avgB);
    }
    public static void print2DArray(int[][] a) {
        for (int[] n : a) {
            StringBuilder s= new StringBuilder("[");
            for (int i : n) {
                if (i == 0) {
                    s.append(" ");
                } else {
                    s.append(i);
                }
            }
            System.out.println(s+"]");
        }
    }

    public void intClass(@Nullable Context c) throws IOException {
        r = c;
    }

    public String go(Bitmap b) throws IOException {
        main2(b);
        return out;
    }

    public static boolean hexIsBlack(Bitmap bitmap, int x, int y, int w) {
        // Get the color of the pixel at position (x, y)
        int pixel = bitmap.getPixel(x, y);

        // Extract RGB components
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);

        // Convert RGB to 8-bit color

        return red < Color.red(w)/2 && green < Color.green(w)/2 && blue < Color.blue(w)/2;
    }
}
