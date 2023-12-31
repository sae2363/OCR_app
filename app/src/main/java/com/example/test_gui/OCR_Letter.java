package com.example.test_gui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.graphics.Color;

import java.util.*;

public class OCR_Letter {
    ArrayList<byte[][]> cap, low, pun, edge;
    List<Character> punList;
    List<String> edgeList;
    byte[][] capA, lowA, punA, edgeA;
    List<Integer> punListHeight;
    Context r = OCR_main.r;


    public OCR_Letter() {
        Bitmap bm1 = BitmapFactory.decodeResource(r.getResources(), R.drawable.letter_cap);
        Bitmap bm2 = BitmapFactory.decodeResource(r.getResources(), R.drawable.letter_low);
        Bitmap bm3 = BitmapFactory.decodeResource(r.getResources(), R.drawable.pun);
        Bitmap bm4 = BitmapFactory.decodeResource(r.getResources(), R.drawable.edge2);
        punList = new ArrayList<>();
        edgeList = new ArrayList<>();
        punListHeight = new ArrayList<>();
        capA = imgToArray(bm1);
        lowA = imgToArray(bm2);
        punA = imgToArray(bm3);
        edgeA = imgToArray(bm4);
        cap = createArrays(capA);
        low = createArrays(lowA);
        pun = createArrays(punA);
        edge = createArrays(edgeA);
        Collections.addAll(punList, '!', '“', '#', '&', '‘', '(', ')', '*', '+', ',', '-', '.', 'l', ':', ';', '<', '=',
                '>', '?', '@','`');
        bm1.recycle();bm2.recycle();bm3.recycle();bm4.recycle();
        capA=null;lowA=null;punA=null;edgeA=null;
        Collections.addAll(punListHeight,1,0,1,1,0,1,1,0,0,0,0,0,1,1,1,1,0,1,1,1);
        Collections.addAll(edgeList, "ff","ft","rv", "tw", "rt", "fy", "fo","tw","fa");
    }

    public byte[][] getChar(char a) {
        byte[][] array = {};
        if (a < 96) {
            array = low.get(a - 97);
        } else {
            array = cap.get(a - 64);
        }
        return array;
    }

    public ArrayList<byte[][]> createArrays(byte[][] baseImg) {
        ArrayList<byte[][]> a = new ArrayList<>();
        int top = 0, bottem = baseImg.length, width = baseImg[0].length;
        ArrayList<Integer> indexs = new ArrayList<>();
        // code to isolate letter by gap between them
        int temp = 0;
        while (!lineCheckV(top, bottem, temp, 1, baseImg))
            temp++;

        for (int i = temp; i < width; i++) {
            if (lineCheckV(top, bottem, i, 0, baseImg)) {
                indexs.add(i);
            }
        }
        // starting at temp create the indvidual arrays
        int i = 0, r = 0;
        while (i < indexs.size() - 1) {
            r = i + 1;
            while (r < indexs.size() - 1 && indexs.get(r) == (indexs.get(r + 1) - 1))
                r++;
            if(pun!=null&&r+10 < indexs.size() - 1 && indexs.get(r+10) == (indexs.get(r + 11) - 1)
                    &&(indexs.get(r+10)-(indexs.get(r)))<25){
                r+=10;
                while (r < indexs.size() - 1 && indexs.get(r) == (indexs.get(r + 1) - 1))
                    r++;
            }
            if (a.size() == 1 && baseImg.length == 140) {
                r++;
                while (r < indexs.size() - 1 && indexs.get(r) == (indexs.get(r + 1) - 1))
                    r++;
                a.add(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1, baseImg));
            } else {
                a.add(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1, baseImg));
            }
            i = r + 1;
            while (!lineCheckV(top, bottem, temp, 1, baseImg))
                i++;
        }
        return a;
    }

    public static byte[][] imgToArray(Bitmap pic) {
        int h = pic.getHeight(), w = pic.getWidth();
        byte[][] a = new byte[h + 2][w + 2];
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
            for (byte[] ints : a) {
                if (ints[coloum] == 1) {
                    go = false;
                    coloum--;
                }
            }
            coloum++;
        }
        byte[][] a2 = new byte[h + 2][w - coloum + 2];
        for (int i = 0; i < h; i++) {
            if (h + 2 - coloum >= 0)
                System.arraycopy(a[i], coloum, a2[i], 0, h + 2 - coloum);
        }
        return a;
    }

    public boolean lineCheckV(int t, int b, int r, int n, byte[][] baseImg) {
        int hit = 0;
        for (int i = t; i < b; i++) {
            if (baseImg[i][r] == 1) {
                hit++;
            }
        }
        return hit > ((b - t) * (n / 100.0));
    }

    public boolean lineCheckH(int y, int l, int r, int n, byte[][] baseImg) {
        int hit = 0;
        for (int i = l; i < r; i++) {
            if (baseImg[y][i] == 1) {
                hit++;
            }
        }
        return hit > ((r - l) * (n / 100.0));
    }

    public byte[][] copy2d(int t, int b, int l, int r, byte[][] baseImg) {
        while (!lineCheckH(t, l, r, 0, baseImg))
            t++;
        b--;
        while (!lineCheckH(b, l, r, 0, baseImg))
            b--;
        byte[][] a = new byte[(b - t) + 2][(r - l) + 2];
        for (int i = 0; i < b - t; i++) {
            if (r - l >= 0) System.arraycopy(baseImg[t + i], l, a[i + 1], 1, r - l);
        }
        return a;
    }

    public static void print2DArray(byte[][] a) {
        for (byte[] n : a) {
            System.out.print("[");
            for (int i : n) {
                if (i == 0) {
                    System.out.print(" ");
                } else {
                    System.out.print(String.valueOf(i));
                }
            }
            System.out.println("]");
        }
    }

    //x is column and y is row start at top left
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
