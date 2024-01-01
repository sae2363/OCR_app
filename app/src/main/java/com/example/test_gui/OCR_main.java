package com.example.test_gui;
//to do fix split error as if the e is lower then the t then it dosnt work

import static android.graphics.Bitmap.createScaledBitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OCR_main {
    @SuppressLint("StaticFieldLeak")
    static Context r;
    static String out = "";
    static long begin;
    static boolean startOCR;
    static double ram=0;

    public static void main2(Bitmap b) throws InterruptedException {
        OCR_Processor pro = new OCR_Processor();

        startOCR=false;
        double ram=0;
        //b = BitmapFactory.decodeResource(r.getResources(), R.drawable.block2mini);
        //error in img to array out of bound
        double value=1.0;
        new Thread(() -> {
            try {
                double avgMb=0,total=0;
                int i=0;
                while (i<10000&&Objects.equals(OCR_main.out, "")) {
                    double mb=(double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                            / (1024 * 1024);
                    if(mb>10){
                        avgMb+=mb;
                        total++;
                    }
                    Thread.sleep(25);
                    i++;
                }
                OCR_main.ram=avgMb/total;
            } catch (Exception ignored) {
            }

        }).start();
        begin = System.currentTimeMillis();
        Bitmap b2 = reduceColorDepth(b);
        b.recycle();
        System.out.println(time()+" color");
        byte[][] ba=imgToArray(b2);
        b2.recycle();
        System.gc();
        startOCR=true;
        System.out.println(time()+" before");
        out = pro.processArray(ba);
        System.out.println(out);
        long end = System.currentTimeMillis();
        long time = end - begin;
        System.out.println("Elapsed Time: " + time + " milli seconds"+"   Average Ram usage: "+ram);
    }
    public static long time()
    {
        return System.currentTimeMillis()-begin;
    }
    private static Bitmap reduceColorDepth(Bitmap originalBitmap) {
        // Create a new bitmap with RGB_565 color configuration
        Bitmap reducedColorDepthBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.RGB_565);

        // Create a canvas and draw the original bitmap onto the new bitmap
        Canvas canvas = new Canvas(reducedColorDepthBitmap);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(originalBitmap, 0, 0, paint);

        return reducedColorDepthBitmap;
    }

    public static Bitmap scaleBI(Bitmap pic, double scale) {
        if(scale>0.99&&scale<1.01)
        {
            return pic;
        }
        return createScaledBitmap(pic, (int)(pic.getWidth() * scale), (int)(pic.getHeight() * scale), true);
    }

    public static byte[][] imgToArray(Bitmap pic) throws InterruptedException {
        int h = pic.getHeight()-1, w = pic.getWidth()-1,l=0,t=0;
        int avgWhite = getAvgColor(pic);
        while (!lineCheckH(pic, t+2, 0, w, avgWhite)) {
            t+=2;
        }
        while (!lineCheckH(pic, h-2, 0, w, avgWhite)) {
            h-=2;
        }
        while (!lineCheckV(pic, t, h, l+2, avgWhite)) {
            l+=2;
        }
        while (!lineCheckV(pic, t, h, w-2, avgWhite)) {
            w-=2;
        }
        byte[][] a = new byte[h-t + 2][w-l + 2];
        int[] pixels;
        for (int i = 0; i < h - t; i++) {
            pixels = new int[w-l-1];
            pic.getPixels(pixels,0,pic.getWidth(),l,i+t,w-l-1,1);
            for (int j = 1; j < w-l-1; j++) {
                if (hexIsBlack(pixels[j], j+l, i+t, avgWhite)) {
                    a[i][j] = 1;
                }
            }
            if(i%10==0)
                System.gc();
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
    public static void print2DArray(byte[][] a) {
        for (byte[] n : a) {
            StringBuilder s= new StringBuilder("[");
            for (byte i : n) {
                if (i == 0) {
                    s.append(" ");
                } else {
                    s.append(i);
                }
            }
            System.out.println(s+"]");
        }
    }

    public void intClass(@Nullable Context c) {
        r = c;
    }

    public String go(Bitmap b) throws IOException, InterruptedException {
        main2(b);
        return out;
    }

    public static boolean hexIsBlack(int pixel, int x, int y, int w) {

        // Extract RGB components
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);

        return red < Color.red(w)/2 && green < Color.green(w)/2 && blue < Color.blue(w)/2;
    }
    public static boolean lineCheckH(Bitmap img, int y, int l, int r, int w) {
        // l+r is the left and right x values
        int hit = 0;
        for (int i = l; i < r; i++) {
            int pixel = img.getPixel(i, y);

            // Extract RGB components
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);
            if (red < Color.red(w)/2 && green < Color.green(w)/2 && blue < Color.blue(w)/2) {
                hit++;
            }
        }
        return hit > 0;
    }
    public static boolean lineCheckV(Bitmap img, int t, int b, int r, int w) {
        int hit = 0;
        for (int i = t; i < b; i++) {
            int pixel = img.getPixel(r,i );

            // Extract RGB components
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);
            if (red < Color.red(w)/2 && green < Color.green(w)/2 && blue < Color.blue(w)/2) {
                hit++;
            }
        }
        return hit > 0;
    }
}
