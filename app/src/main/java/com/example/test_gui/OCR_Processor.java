package com.example.test_gui;

import java.util.*;

public class OCR_Processor {
    int[][] baseImg;
    ArrayList<ArrayList<Integer>> spaceLM, indexsM;
    int lines;
    boolean spacePlus;

    public OCR_Processor() {

    }

    public String processArray(int[][] a) {
        spacePlus = false;
        baseImg = a;
        spaceLM = new ArrayList<>();
        indexsM = new ArrayList<>();
        baseImg = trimL(a);
        a=null;
        //print2DArray(baseImg);
        Align_Array();
        ArrayList<ArrayList<int[][]>> LArray = new ArrayList<>();
        for (int i = 2; i < lines; i++) {
            spaceLM.add(new ArrayList<>());
            indexsM.add(new ArrayList<>());
            LArray.add(isolate_letters(new ArrayList<int[][]>(), (i - 2), i));
            // issue is in isolate
            makeSpaceA(indexsM.get(i - 2), spaceLM.get(i - 2));
            if (spacePlus) {
                for (int j = 0; j < spaceLM.get(i - 2).size(); j++) {
                    spaceLM.get(i - 2).set(j, spaceLM.get(i - 2).get(j) + 1);
                }
                spacePlus = false;
            }
        }
        String s = string(LArray);
        return s;
    }

    public void Align_Array() {
        make_line();
    }

    public String string(ArrayList<ArrayList<int[][]>> LArray) {
        OCR_Letter letters = new OCR_Letter();
        String s = "";
        for (int i = 0; i < spaceLM.get(0).size(); i++) {
            // spaceLM.get(0).set(i,spaceLM.get(0).get(i)+1);
        }

        for (int l = 0; l < LArray.size(); l++) {
            for (int i = 0; i < LArray.get(l).size(); i++) {
                //print2DArray(LArray.get(l).get(i));
                double[] data = new double[8];
                if (l == 0 && i > 13) {
                    // print2DArray(LArray.get(l).get(i));
                }
                //print2DArray(LArray.get(l).get(i));
                compair(letters.low, LArray.get(l).get(i), data, 0);
                compair(letters.cap, LArray.get(l).get(i), data, 2);
                compair(letters.pun, LArray.get(l).get(i), data, 4);
                compair(letters.edge, LArray.get(l).get(i), data, 6);
                String space = "", LAdd = "";
                if (spaceLM.get(l).contains(i))
                    space = " ";
                // if (l == 0)
                // System.out.println(Arrays.toString(data));
                if (data[0] > data[2]) {
                    LAdd = "" + ((char) (97 + data[1]));
                } else {
                    if (data[2] > data[4]) {
                        LAdd = "" + ((char) (65 + data[3]));
                    } else {
                        if (data[5] > data[7] && data[5] != -1) {
                            LAdd = "" + letters.punList.get((int) data[5]);
                        } else {
                            if (data[7] != -1) {
                                LAdd = "" + letters.edgeList.get((int) data[7]);
                            }
                        }
                    }
                }
                if (LAdd.equals("!") || LAdd.equals("i")) {

                    if (!gap(LArray.get(l).get(i))) {
                        LAdd = "l";
                    }
                }
                if (LAdd.equals("I") && s.charAt(s.length() - 1) != '.') {
                    LAdd = "l";
                }
                s = s + space + LAdd;

            }
            s = s + "\n";
        }
        return s;
    }

    public boolean gap(int[][] a) {
        int t = 0, b = a.length - 2, b2;
        boolean value = false;
        while (!lineCheckH(a, b, b, 0, 0, a[0].length) && !lineCheckH(a, b - 1, b - 1, 0, 0, a[0].length)) {
            b--;
        }
        while (lineCheckH(a, b, b, 0, 0, a[0].length) && lineCheckH(a, b - 1, b - 1, 0, 0, a[0].length)) {
            b--;
        }
        b--;
        b2 = b;
        while (b2 != t && !lineCheckH(a, b2, b2, 0, 0, a[0].length)) {
            b2--;
        }
        if (b2 != 0)
            value = true;
        return value;
    }

    public void compair(ArrayList<int[][]> letter, int[][] base, double[] data, int start) {
        double max = 0, maxIndex = -1;
        for (int j = 0; j < letter.size(); j++) {
            int[][] temp = letter.get(j);
            int[][] a = copy2d2(base);
            if (a.length > temp.length) {
                a = OCR_Scale.scaleYDown(a, temp.length);
            } else {
                a = OCR_Scale.scaleYUp(a, temp.length);
            }
            if (a[0].length > temp[0].length) {
                a = OCR_Scale.scaleXDown(a, temp[0].length);
            } else {
                a = OCR_Scale.scaleXUp(a, temp[0].length);
            }
            double overlap = overLap(a, temp);
            if (overlap > max) {
                max = overlap;
                maxIndex = j;
            }
        }
        data[0 + start] = max;
        data[1 + start] = maxIndex;
    }

    public double overLap(int[][] a1, int[][] a2) {
        int total = 0;
        for (int i = 0; i < a1.length; i++) {
            for (int j = 0; j < a1[0].length; j++) {
                if (a1[i][j] == a2[i][j] && a1[i][j] != 0) {
                    total++;
                }
                if (a1[i][j] != a2[i][j] && (a1[i][j] != 0 || a2[i][j] != 0)) {
                    total--;
                }
            }
        }
        return (double) total / (a1.length * a1[0].length);
    }

    public ArrayList<int[][]> isolate_letters(ArrayList<int[][]> a, int indexInA, int topN) {
        int bottem = 0, top = 0;
        while (baseImg[top][1] != topN)
            top++;
        bottem = top;
        ArrayList<Integer> indexs = indexsM.get(indexInA);
        for (int i = top + 1; i < baseImg.length; i++) {
            if (baseImg[i][0] != 0 && baseImg[i][0] != 1) {
                bottem = i;
                i = baseImg.length + 1;
            }
        }
        indexs = new ArrayList<>();
        // code to isolate letter by gap between them
        int temp = 0;
        while (!lineCheckV(top, bottem, temp, 0))
            temp++;
        for (int i = temp; i < baseImg[0].length - 1; i++) {
            if (lineCheckV(top, bottem, i, 0)
                    || lineCheckV(top, bottem, i - 1, 0) && lineCheckV(top, bottem, i + 1, 0)) {
                indexs.add(i);
            }
        }
        // System.out.println(indexs);
        // starting at temp create the indvidual arrays
        int i = 0, r = 0;
        while (i < indexs.size() - 2) {
            r = i + 1;
            while (r < (indexs.size() - 3) && (indexs.get(r) == (indexs.get(r + 1) - 1)))
                r++;
            // print2DArray(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1));
            // idea for more advance one go left to right documenting how far right the
            // first letter goes
            // then go to most far right one and if there is a space below it not mentioned
            // in the array then it found the second letter
            if ((bottem - top > ((indexs.get(r) + 1) - indexs.get(i)) * 1.4)) {
                a.add(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1));
                // see why w is werid
                // if(i==0&&indexInA==0)
                // print2DArray(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1));
            } else {
                if (splitcheck(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1))) {
                    //print2DArray(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1));
                    splitLetter(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1), a);
                    spacePlus = true;
                } else {
                    a.add(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1));
                }
            }
            i = r + 1;
            while (!lineCheckV(top, bottem, temp, 0)) {
                i++;
            }
        }
        indexsM.set(indexInA, indexs);
        return a;

    }

    public boolean splitcheck(int[][] a) {
        //print2DArray(a);
        int rl = 0, x = 0;
        while (rl < a.length && !lineCheckH(a, rl, rl, 0, (a[rl].length / 3) * 2 - 1, a[rl].length-1)) {
            //System.out.println(rl);
            rl++;
        }
        x = a[rl].length - 1;
        while (x > 0 && a[rl][x] == 0) {
            x--;
        }
        while (x > 0 && a[rl][x] == 1) {
            x--;
        }
        while (x > 0 && lineCheckV(rl, a.length, x, 0, a)) {
            x--;
        }
        int x2=x;
        while (x2 > 0 && !lineCheckV(rl, a.length, x2, 0, a)) {
            x2--;
        }
        return lineCheckV(0, a.length, x, 0, a) != lineCheckV(rl, a.length, x, 0, a);
    }

    public void makeSpaceA(ArrayList<Integer> a, ArrayList<Integer> spaceL) {
        double avg = 0;// , avg2 = 0;
        int total = 0, loc = 1;
        for (int i = 0; i < a.size() - 1; i++) {
            int space = a.get(i + 1) - a.get(i);
            if (space >= 3) {
                avg = (space + avg * total) / (1.0 + total);
                total++;
            }
        }
        total = 0;
        avg *= 1.25;
        for (int i = 0; i < a.size() - 1; i++) {
            int space = a.get(i + 1) - a.get(i);
            if (space > 2) {
                if (space > avg) {
                    spaceL.add(loc);
                }
                loc++;
            }

        }
    }

    // upgrade with array and a value which checks how far right a letters goes by
    // going up down or diagonaly
    // simple way to increse speed, check for height of left and right letter
    public void splitLetter(int[][] a, ArrayList<int[][]> al) {
        int lowerBound = a.length / 2, x = 0, y = a.length - 2;
        while (a[lowerBound][x] == 0) {
            x++;
        }
        while (a[lowerBound][x] == 1) {
            x++;
        }
        x += 3;
        while (lineCheckH(a, y, y, 0, x, a[0].length)) {
            y--;
        }

        int[][] rightLetter = copy2d(a, y, a.length, x, a[0].length);
        int[][] temp = fillXY(copy2d(a, 0, a.length, 0, a[0].length), y, a.length, x, a[0].length);
        int[][] leftLetter = copy2d(temp, 0, a.length, 0, a[0].length);
        // print2DArray(a);
        al.add(trim(leftLetter));
        al.add(trim(rightLetter));
    }

    // find line which is stright relative to text
    public void make_line() {
        int l = 0, r = 0, lineNum = 3;
        for (int i = 0; i < baseImg[0].length && l < baseImg.length - 1; i++) {
            baseImg[l][i] = 2;// (int)Math.round(l+(slope*i))][i]=1;
            lines = 2;
        }
        while (l < baseImg.length - 1 && r < baseImg.length - 1) {
            while (l < baseImg.length - 1 && r < baseImg.length - 1 && !lineCheckH(l, r, 0, 0, baseImg[0].length - 1)) {
                l++;
                r++;
            }
            // System.out.println("touch" + l + " " + r);
            while (lineCheckH(l, r, 0, 0, baseImg[0].length - 1)) {
                l++;
                if (lineCheckH(l, r, 0, 0, baseImg[0].length - 1))
                    r++;
            }
            // double slope= (r-l)/baseImg.length;
            for (int i = 0; i < baseImg[0].length && l < baseImg.length - 1; i++) {
                baseImg[l][i] = lineNum;// (int)Math.round(l+(slope*i))][i]=1;
                lines = lineNum;
            }
            lineNum++;
            l++;
            r++;
        }
        // write code to get line to top of word first then get it to the underside
    }

    // true=line hit less then n% of line characters
    public boolean lineCheckH(int l, int r, int n) {
        // double slope = (r - l) / baseImg.length;
        int hit = 0;
        for (int i = 0; i < baseImg.length; i++) {
            if (baseImg[l][i] == 1) {
                hit++;
            }
        }
        return hit > ((r - l) * (n / 100.0));
    }

    public boolean lineCheckH(int l, int r, int n, int x, int y) {
        // double slope = (r - l) / baseImg.length;
        // l+r is the left and right Y values
        int hit = 0;
        for (int i = x; i < y; i++) {
            if (baseImg[l][i] == 1) {
                hit++;
            }
        }
        return hit > ((y - x) * (n / 100.0));
    }

    public boolean lineCheckH(int[][] a, int y, int DNE, int n, int l, int r) {
        // double slope = (r - l) / baseImg.length;
        // l+r is the left and right Y values
        int hit = 0;
        for (int i = l; i < r; i++) {
            if (a[y][i] == 1) {
                hit++;
            }
        }
        return hit > ((r - l) * (n / 100.0));
    }

    // true=line hit more then n% of line characters
    public boolean lineCheckV(int t, int b, int r, int n) {
        int hit = 0;
        for (int i = t; i < b; i++) {
            if (baseImg[i][r] == 1) {
                hit++;
            }
        }
        return hit > ((b - t) * (n / 100.0));
    }

    public boolean lineCheckV(int t, int b, int r, int n, int[][] a) {
        int hit = 0;
        for (int i = t; i < b; i++) {
            if (a[i][r] == 1) {
                hit++;
            }
        }
        return hit > ((b - t) * (n / 100.0));
    }

    // returns a new 2d array with one extra cell around the area copied
    public int[][] copy2d(int t, int b, int l, int r) {
        while (!lineCheckH(t, t, 0, l, r))
            t++;
        while (!lineCheckH(b, b, 0, l, r))
            b--;

        int[][] a = new int[(b - t + 1) + 2][(r - l) + 2];
        for (int i = 0; i <= b - t; i++) {
            for (int j = 0; j < (r - l); j++) {
                a[i + 1][j + 1] = baseImg[t + i][l + j];
            }
        }
        return a;
    }

    public int[][] copy2d(int[][] aO, int t, int b, int l, int r) {
        int[][] a = new int[(b - t + 1) + 2][(r - l) + 2];
        // print2DArray(aO);
        for (int i = 0; i < b - t; i++) {
            for (int j = 0; j < (r - l); j++) {
                a[i + 1][j + 1] = aO[t + i][l + j];
            }
        }
        // print2DArray(a);
        return a;
    }

    public static void print2DArray(int[][] a) {
        for (int[] n : a) {
            String s="[";
            for (int i : n) {
                if (i == 0) {
                    s+=" ";
                } else {
                    s+=i;
                }
            }
            System.out.println(s+"]");
        }
    }

    public int[][] copy2d2(int[][] a) {
        int[][] newA = new int[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                newA[i][j] = a[i][j];
            }
        }
        return newA;
    }

    public int[][] fillXY(int[][] a, int top, int bottem, int l, int r) {
        for (int i = top; i < bottem; i++) {
            for (int j = l; j < r; j++) {
                a[i][j] = 0;
            }
        }
        return a;
    }

    public int[][] trim(int[][] a) {
        int t = 0, b = a.length - 1, l = 0, r = a[0].length - 1;
        while (!lineCheckH(a, t, t, 1, l, r)) {
            t++;
        }
        while (!lineCheckH(a, b, b, 0, l, r))
            b--;
        while (!lineCheckV(t, b, l, 0, a))
            l++;
        while (!lineCheckV(t, b, r, 0, a))
            r--;
        int[][] a2 = new int[b - t + 2][r - l + 2];
        a2 = copy2d(a, t, b, l, r);
        return a2;
    }

    public int[][] trimL(int[][] a) {
        int t = 0, b = a.length - 1, l = 0, r = a[0].length - 1;
        while (!lineCheckH(a, t, t, 1, l, r)) {
            t++;
        }
        while (!lineCheckH(a, b, b, 0, l, r))
            b--;
        while (!lineCheckV(t, b, l, 0, a))
            l++;
        while (!lineCheckV(t, b, r, 0, a))
            r--;
        int[][] a2 = new int[b - t + 4][r - l + 4];
        a2 = copy2d(a, t, b, l, r);
        return a2;
    }

}
