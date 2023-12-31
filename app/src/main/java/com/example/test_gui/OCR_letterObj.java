package com.example.test_gui;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class OCR_letterObj {
    ArrayList<ArrayList<Byte2>> letterA;
    int width;

    public OCR_letterObj(byte[][] a) {
        letterA = new ArrayList<>();
        width = a[0].length;
        for (int i = 1; i < a.length - 1; i++) {
            letterA.add(new ArrayList<>());
            for (int j = 1; j < a[i].length - 1; j++) {
                int count = 1;
                while (j < a[i].length - 1 && a[i][j] == a[i][j + 1]) {
                    count++;
                    j++;
                }
                letterA.get(i - 1).add(new Byte2(a[i][j], count));
            }
        }
    }

    public byte[][] to2D() {
        byte[][] a = new byte[letterA.size() + 1][width];

        for (int i = 0; i < letterA.size() - 1; i++) {
            int pos = 1;
            for (int j = 0; j < letterA.get(i).size(); j++) {
                int count = 0;
                Byte2 n = letterA.get(i).get(j);
                while (pos < a[i].length - 1 && count < n.count) {
                    a[i + 1][pos] = n.value;
                    pos++;
                    count++;
                }
            }
        }
        return a;
    }

    public class Byte2 {
        byte value, count;

        public Byte2(int v, int c) {
            value = (byte)v;
            count = (byte)c;
        }

        @NonNull
        public String toString() {
            return "(" + value + "," + count + ")";
        }
    }
}
