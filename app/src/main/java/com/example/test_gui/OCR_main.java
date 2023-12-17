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
        Bitmap bm = BitmapFactory.decodeResource(r.getResources(), R.drawable.img1);
        print2DArray(imgToArray(scaleBI(b, 1),25));
        out = pro.processArray(imgToArray(scaleBI(b, 1), 25));
    }

    public static Bitmap scaleBI(Bitmap pic, double scale) {
        return createScaledBitmap(pic, (int)(pic.getWidth() * scale), (int)(pic.getHeight() * scale), true);
    }

    public static int[][] imgToArray(Bitmap pic, int t) {
        int h = pic.getHeight(), w = pic.getWidth();
        int[][] a = new int[h + 2][w + 2];
        for (int i = 1; i < h; i++) {
            for (int j = 1; j < w; j++) {
                if (hexIsBlack(pic, j, i, 15)) {
                    a[i][j] = 1;
                }
            }
        }
        boolean go = true;
        int coloum = 0;
        while (go) {
            for (int i = 0; i < a.length; i++) {
                if (a[i][coloum] == 1) {
                    go = false;
                    coloum--;
                }
            }
            coloum++;
        }
        int[][] a2 = new int[h + 2][w - coloum + 2];
        for (int i = 0; i < h; i++) {
            for (int j = coloum; j < h + 2; j++) {
                a2[i][j - coloum] = a[i][j];
            }
        }
        return a;
    }

    public static void print2DArray(int[][] a) {
        for (int[] n : a) {
            System.out.print("[");
            for (int i : n) {
                if (i == 0) {
                    System.out.print(" ");
                } else {
                    System.out.print(i + "");
                }
            }
            System.out.println("]");
        }
    }

    public void intClass(@Nullable Context c) throws IOException {
        r = c;
    }

    public String go(Bitmap b) throws IOException {
        main2(b);
        return out;
    }

    public static boolean hexIsBlack(Bitmap bitmap, int x, int y, int t) {
        // Get the color of the pixel at position (x, y)
        int pixel = bitmap.getPixel(x, y);

        // Extract RGB components
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);

        // Convert RGB to 8-bit color
        return red < t && green < t && blue < t;
    }
}
