package com.example.test_gui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.graphics.Color;

import java.util.*;

public class OCR_Letter {
    ArrayList<int[][]> cap, low, pun, edge;
    List<Character> punList;
    List<String> edgeList;
    int[][] capA, lowA, punA, edgeA;
    Context r = OCR_main.r;

    public OCR_Letter() {
        Bitmap bm1 = BitmapFactory.decodeResource(r.getResources(), R.drawable.letter_cap);
        Bitmap bm2 = BitmapFactory.decodeResource(r.getResources(), R.drawable.letter_low);
        Bitmap bm3 = BitmapFactory.decodeResource(r.getResources(), R.drawable.pun);
        Bitmap bm4 = BitmapFactory.decodeResource(r.getResources(), R.drawable.edge2);

        punList = new ArrayList<>();
        edgeList = new ArrayList<>();
        capA = imgToArray(bm1, 15);
        lowA = imgToArray(bm2, 15);
        punA = imgToArray(bm3, 15);
        edgeA = imgToArray(bm4, 15);
        cap = createArrays(capA);
        low = createArrays(lowA);
        pun = createArrays(punA);
        edge = createArrays(edgeA);
        Collections.addAll(punList, '!', '“', '#', '&', '‘', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=',
                '>', '?', '@', '`');
        Collections.addAll(edgeList, "ff", "ft");
    }

    public int[][] getChar(char a) {
        int[][] array = {};
        if (a < 96) {
            array = low.get(a - 97);
        } else {
            array = cap.get(a - 64);
        }
        return array;
    }

    public ArrayList<int[][]> createArrays(int[][] baseImg) {
        ArrayList<int[][]> a = new ArrayList<>();
        int top = 0, bottem = baseImg.length, width = baseImg[0].length;
        // System.out.println(baseImg[0].length);
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
        int i = 0, r;
        while (i < indexs.size() - 1) {
            r = i + 1;
            while (r < indexs.size() - 1 && indexs.get(r) == (indexs.get(r + 1) - 1))
                r++;
            // System.out.println(top + " " + bottem + " " + i + " " + r);// indexs.get(i)+"
            // "+indexs.get(r));
            if (a.size() == 1 && baseImg.length == 140) {
                r += 3;
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
            for (int[] ints : a) {
                if (ints[coloum] == 1) {
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

    public boolean lineCheckV(int t, int b, int r, int n, int[][] baseImg) {
        int hit = 0;
        for (int i = t; i < b; i++) {
            if (baseImg[i][r] == 1) {
                hit++;
            }
        }
        return hit > ((b - t) * (n / 100.0));
    }

    public boolean lineCheckH(int y, int l, int r, int n, int[][] baseImg) {
        int hit = 0;
        for (int i = l; i < r; i++) {
            if (baseImg[y][i] == 1) {
                hit++;
            }
        }
        return hit > ((r - l) * (n / 100.0));
    }

    public int[][] copy2d(int t, int b, int l, int r, int[][] baseImg) {
        while (!lineCheckH(t, l, r, 0, baseImg))
            t++;
        b--;
        while (!lineCheckH(b, l, r, 0, baseImg))
            b--;
        int[][] a = new int[(b - t) + 2][(r - l) + 2];
        for (int i = 0; i < b - t; i++) {
            for (int j = 0; j < (r - l); j++) {
                a[i + 1][j + 1] = baseImg[t + i][l + j];
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
