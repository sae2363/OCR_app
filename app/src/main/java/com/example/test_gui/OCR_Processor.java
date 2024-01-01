package com.example.test_gui;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OCR_Processor {
    byte[][] baseImg;
    ArrayList<ArrayList<Integer>> spaceLM, indexsM;
    int lines;
    ArrayList<Integer> splitAt;
    boolean spacePlus;
    Lock lock;
    ArrayList<char[]> string;

    public OCR_Processor() {

    }

    public String processArray(byte[][] a) throws InterruptedException {
        spacePlus = false;
        lock = new ReentrantLock();
        baseImg = a;
        spaceLM = new ArrayList<>();
        indexsM = new ArrayList<>();
        splitAt = new ArrayList<>();
        baseImg = a;
        string = new ArrayList<>();
        Align_Array();
        ArrayList<ArrayList<OCR_letterObj>> LArray = new ArrayList<>();
        for (int i = 2; i < lines; i++) {
            int splitAtsize = splitAt.size();
            spaceLM.add(new ArrayList<>());
            indexsM.add(new ArrayList<>());
            LArray.add(isolate_letters(new ArrayList<>(), (i - 2), i));
            makeSpaceA(indexsM.get(i - 2), spaceLM.get(i - 2));
            if (spacePlus) {
                for (int k = splitAt.size() - splitAtsize; k > 0; k--) {
                    int value = spaceLM.get(i - 2).indexOf(splitAt.get(splitAt.size() - k)), x = 1;
                    while (value == -1) {
                        value = spaceLM.get(i - 2).indexOf(splitAt.get(splitAt.size() - k) + x);
                        if (value == -1) {
                            value = spaceLM.get(i - 2).indexOf(splitAt.get(splitAt.size() - k) - x);
                        }
                        x++;
                    }
                    if (value == 0)
                        value = -1;
                    for (int j = value + 1; j < spaceLM.get(i - 2)
                            .size(); j++) {
                        if (j == -1) {
                            j = 0;
                        }
                        spaceLM.get(i - 2).set(j, spaceLM.get(i - 2).get(j) + 1);
                    }
                }
                spacePlus = false;
            }
            string.add(new char[LArray.get(i - 2).size() + spaceLM.get(i - 2).size()]);
        }
        System.gc();
        return string(LArray);
    }

    public void Align_Array() {
        make_line();
    }

    // Takes every letter array and compared it to pre defined character with the
    // highest matching being
    // put in to the final string
    public String string(ArrayList<ArrayList<OCR_letterObj>> LArray) throws InterruptedException {
        OCR_Letter letters = new OCR_Letter();
        StringBuilder s = new StringBuilder();
        int avgH = 0, avgW = 0, count = 0;
        // finding average height of letters in row
        for (ArrayList<OCR_letterObj> a : LArray) {
            for (OCR_letterObj n : a) {
                avgH += n.letterA.size();
                avgW += n.width;
                count++;
            }
        }

        avgH = (int) (avgH / (double) count);
        avgW = (int) (avgW / (double) count);
        ExecutorService executor = Executors.newFixedThreadPool(1);

        for (int l = 0; l < LArray.size(); l++) {
            int i = 0;
            int offset = 0;
            while (i < LArray.get(l).size()) {
                // comparing each single letter to all possible characters
                for (int k = 0; k < 4; k++) {
                    if (i < LArray.get(l).size()) {
                        executor.submit(new compair(letters, LArray.get(l).get(i), avgH, l, i + offset));
                        i++;
                        if (spaceLM.get(l).contains(i)) {
                            string.get(l)[i + offset] = ' ';
                            offset++;
                        }
                    }
                }
            }
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        for (char[] a : string) {
            for (char n : a) {
                s.append(n);

            }
            s.append('\n');
        }
        return s.toString();
    }

    public void modifySA(char c, int r, int col) {
        try {
            lock.lock();
            string.get(r)[col] = c;
        } finally {
            lock.unlock();
        }
    }

    // compairs single array to a group of characters, scales each letter to the
    // base case
    public void compair(List<byte[][]> letter, OCR_letterObj base, double[] data, int start) {
        double max = -1, maxIndex = -1;
        for (int j = 0; j < letter.size(); j++) {
            byte[][] temp = letter.get(j);
            byte[][] a = base.to2D();
            if (a.length > temp.length) {
                a = OCR_Cont.scaleYDown(a, temp.length);
            } else {
                a = OCR_Cont.scaleYUp(a, temp.length);
            }
            if (a[0].length > temp[0].length) {
                a = OCR_Cont.scaleXDown(a, temp[0].length);
            } else {
                a = OCR_Cont.scaleXUp(a, temp[0].length);
            }
            double overlap = overLap(a, temp);
            if (overlap > max) {
                max = overlap;
                maxIndex = j;
            }
        }
        data[start] = max;
        data[1 + start] = maxIndex;
    }

    // hard code rules due to font
    public String edgeCase(String LAdd, byte[][] temp, int h) {
        if (LAdd.equals("!") || LAdd.equals("i")) {
            // print2DArray(temp);
            if (!gap(temp)) {
                LAdd = "l";
            }
        }
        if (LAdd.equals("@") && gap(temp)) {
            LAdd = "?";
        }
        if (LAdd.equals("/") && temp[0].length * 2 > temp.length)
            LAdd = "ff";
        if (LAdd.equals("/") && temp[0].length * 6 > temp.length) {
            LAdd = "l";
        }
        if (LAdd.equals(".") && temp.length * 2 < temp[0].length)
            LAdd = "-";
        if (LAdd.equals("-") && temp.length > h)
            LAdd = "l";
        if (LAdd.equals("-") && temp.length > temp[0].length)
            LAdd = ",";
        return LAdd;
    }

    // same as above but it return a arrays with all overlap values
    public ArrayList<Double> compair2(List<byte[][]> letter, OCR_letterObj base) {
        ArrayList<Double> al = new ArrayList<>();
        for (int j = 0; j < letter.size(); j++) {
            byte[][] temp = letter.get(j);
            byte[][] a = base.to2D();
            if (a.length > temp.length) {
                a = OCR_Cont.scaleYDown(a, temp.length);
            } else {
                a = OCR_Cont.scaleYUp(a, temp.length);
            }
            if (a[0].length > temp[0].length) {
                a = OCR_Cont.scaleXDown(a, temp[0].length);
            } else {
                a = OCR_Cont.scaleXUp(a, temp[0].length);
            }
            double overlap = overLap(a, temp);
            al.add(overlap);
        }
        return al;
    }

    // determine how much overlap there is, not overlap subtract 1, overlap add 1,
    // if both are 0 then do nothing
    public double overLap(byte[][] a1, byte[][] a2) {
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

    // turn large array in to single letters
    public ArrayList<OCR_letterObj> isolate_letters(ArrayList<OCR_letterObj> a, int indexInA, int topN) {
        int bottem, top = 0;
        // find top and bottem of each row
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
                a.add(new OCR_letterObj(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1)));
            } else {
                if (splitcheck(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1))) {
                    splitLetter(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1), a);
                    spacePlus = true;
                    splitAt.add(a.size() - 2);
                } else {
                    a.add(new OCR_letterObj(copy2d(top, bottem, indexs.get(i), indexs.get(r) + 1)));
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

    // determine if a letter array is really 2 letters
    public boolean splitcheck(byte[][] a) {
        int rl = 0, x = 0;
        while (rl < a.length && !lineCheckH(a, rl, 0, (a[rl].length / 4) * 3 - 1, a[rl].length - 1)) {
            rl++;
        }
        rl++;

        x = a[rl].length - 2;
        while (x > 0 && a[rl][x] == 1) {
            x--;
        }
        while (x > 0 && lineCheckV(rl, a.length, x, 0, a)) {
            x--;
        }
        if (x > 0)
            x--;
        if (lineCheckV(0, a.length - 1, x, 0, a) == true && true == lineCheckV(rl, a.length - 1, x, 0, a))
            return false;
        return lineCheckV(0, a.length, x, 0, a) != lineCheckV(rl, a.length, x, 0, a);
    }

    // determine where spaces are
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

    // method to find if there is a gap in letter like i has a gap between dot and
    // line
    public boolean gap(byte[][] a) {
        int t = 0, b = a.length - 2, b2;
        boolean value = false;
        while (!lineCheckH(a, b, 0, 0, a[0].length) && !lineCheckH(a, b - 1, 0, 0, a[0].length)) {
            b--;
        }
        while (lineCheckH(a, b, 0, 0, a[0].length) && lineCheckH(a, b - 1, 0, 0, a[0].length)) {
            b--;
        }
        b--;
        b2 = b;
        while (b2 != t && !lineCheckH(a, b2, 0, 0, a[0].length)) {
            b2--;
        }
        if (b2 != 0)
            value = true;
        return value;
    }

    // upgrade with array and a value which checks how far right a letters goes by
    // going up down or diagonaly
    // simple way to increse speed, check for height of left and right letter
    public void splitLetter(byte[][] a, ArrayList<OCR_letterObj> al) {
        int x = 0;
        int rl = 0;
        // using left 1/4 go down till hit letter then move left until there is no 1's
        // below rl
        while (rl < a.length && !lineCheckH(a, rl, 0, (a[rl].length / 4) * 3 - 1, a[rl].length - 1)) {
            rl++;
        }
        rl++;

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
        byte[][] rightLetter = copy2d(a, rl - 1, a.length, x, a[0].length);
        byte[][] temp = fillXY(copy2d(a, 0, a.length, 0, a[0].length), rl - 1, a.length, x, a[0].length);
        byte[][] leftLetter = copy2d(temp, 0, a.length, 0, a[0].length);
        al.add(new OCR_letterObj(trim(leftLetter)));
        al.add(new OCR_letterObj(trim(rightLetter)));
    }

    // find line which is stright relative to text
    public void make_line() {
        int l = 0, r = 0;
        byte lineNum = 3;
        for (int i = 0; i < baseImg[0].length && l < baseImg.length - 1; i++) {
            baseImg[l][i] = 2;
            lines = 2;
        }
        while (l < baseImg.length - 1 && r < baseImg.length - 1) {
            while (l < baseImg.length - 1 && r < baseImg.length - 1 && !lineCheckH(l, r, 0, 0, baseImg[0].length - 1)) {
                l++;
                r++;
            }
            while (lineCheckH(l, r, 0, 0, baseImg[0].length - 1)) {
                l++;
                if (lineCheckH(l, r, 0, 0, baseImg[0].length - 1))
                    r++;
            }
            for (int i = 0; i < baseImg[0].length && l < baseImg.length - 1; i++) {
                baseImg[l][i] = lineNum;
                lines = lineNum;
            }
            lineNum++;
            l++;
            r++;
        }
        // write code to get line to top of word first then get it to the underside
    }

    // true=line hit more then n% of line characters
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

    public boolean lineCheckH(byte[][] a, int y, int n, int l, int r) {
        // l+r is the left and right x values
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

    public boolean lineCheckV(int t, int b, int r, int n, byte[][] a) {
        int hit = 0;
        for (int i = t; i < b; i++) {
            if (a[i][r] == 1) {
                hit++;
            }
        }
        return hit > ((b - t) * (n / 100.0));
    }

    // returns a new 2d array with one extra cell around the area copied
    public byte[][] copy2d(int t, int b, int l, int r) {
        while (!lineCheckH(t, t, 0, l, r))
            t++;
        while (!lineCheckH(b, b, 0, l, r))
            b--;

        byte[][] a = new byte[(b - t + 1) + 2][(r - l) + 2];
        for (int i = 0; i <= b - t; i++) {
            for (int j = 0; j < (r - l); j++) {
                a[i + 1][j + 1] = baseImg[t + i][l + j];
            }
        }
        return a;
    }

    // copy section of array provided
    public byte[][] copy2d(byte[][] aO, int t, int b, int l, int r) {
        byte[][] a = new byte[(b - t + 1) + 2][(r - l) + 2];
        // print2DArray(aO);
        for (int i = 0; i < b - t; i++) {
            for (int j = 0; j < (r - l); j++) {
                a[i + 1][j + 1] = aO[t + i][l + j];
            }
        }
        // print2DArray(a);
        return a;
    }

    public static void print2DArray(byte[][] a) {
        for (byte[] n : a) {
            System.out.print("[");
            for (int i : n) {
                if (i == 0) {
                    System.out.print(" ");
                } else {
                    System.out.print(i);
                }
            }
            System.out.println("]");
        }
    }

    // copy entire array
    public byte[][] copy2d2(byte[][] a) {
        byte[][] newA = new byte[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                newA[i][j] = a[i][j];
            }
        }
        return newA;
    }

    // fill provided square with 0
    public byte[][] fillXY(byte[][] a, int top, int bottem, int l, int r) {
        for (int i = top; i < bottem; i++) {
            for (int j = l; j < r; j++) {
                a[i][j] = 0;
            }
        }
        return a;
    }

    // trim letter array to contain gap of only 1 around it
    public byte[][] trim(byte[][] a) {
        try {
            int t = 0, b = a.length - 1, l = 0, r = a[0].length - 1;
            while (!lineCheckH(a, t, 1, l, r)) {
                t++;
            }
            while (!lineCheckH(a, b, 0, l, r))
                b--;
            while (!lineCheckV(t, b, l, 0, a))
                l++;
            while (!lineCheckV(t, b, r, 0, a))
                r--;
            byte[][] a2 = new byte[b - t + 2][r - l + 2];
            a2 = copy2d(a, t, b, l, r);
            return a2;
        } catch (Exception e) {
            System.out.println("Error in trim");
            return new byte[1][1];
        }
    }

    // trim but for larger arrays
    public byte[][] trimL(byte[][] a) {
        int t = 0, b = a.length - 1, l = 0, r = a[0].length - 1;
        while (!lineCheckH(a, t, 1, l, r)) {
            t++;
        }
        while (!lineCheckH(a, b, 0, l, r))
            b--;
        while (!lineCheckV(t, b, l, 0, a))
            l++;
        while (!lineCheckV(t, b, r, 0, a))
            r--;
        byte[][] a2 = new byte[b - t + 4][r - l + 4];
        a2 = copy2d(a, t, b, l, r);
        return a2;
    }

    public class compair implements Runnable {
        OCR_Letter letters;
        OCR_letterObj temp;
        int avgH, row, col;

        public compair(OCR_Letter l, OCR_letterObj la, int h, int r, int c) {
            letters = l;
            temp = la;
            avgH = h;
            row = r;
            col = c;
        }

        @Override
        public void run() {
            double[] data = new double[8];
            data[0]=-2;
            data[2]=-2;
            compair(letters.low, temp, data, 0);
            compair(letters.cap, temp, data, 2);
            compair(letters.pun, temp, data, 4);
            compair(letters.edge, temp, data, 6);
            String LAdd = "";
            // check if the array is small then it must be puncuation
            if (temp.letterA.size() < avgH / 2 && data[5] != -1) {
                ArrayList<Double> temp2 = compair2(letters.pun, temp);
                double max = -2;
                int maxIndex = -1;
                for (int k = 0; k < temp2.size() - 1; k++) {
                    if (letters.punListHeight.get(k) == 0 && temp2.get(k) > max) {
                        max = temp2.get(k);
                        maxIndex = k;
                    }
                }
                LAdd = "" + letters.punList.get(maxIndex);
            } else {
                double firstV = Integer.MIN_VALUE, firstL = -1, secondV = Integer.MIN_VALUE, secondL = -1;
                for (int d = 0; d < data.length; d += 2) {
                    if (data[d] > firstV) {
                        secondV = firstV;
                        secondL = firstL;
                        firstV = data[d];
                        firstL = d;
                    } else if (data[d] > secondV) {
                        secondV = data[d];
                        secondL = d;
                    }

                }
                int value = ((int) firstL) / 2;
                if ((firstL == 3 && data[5] != -1 && letters.punListHeight.get((int) data[5]) == 0
                        && temp.letterA.size() > avgH / 2.0) || firstL > 3 && data[5] == 19) {
                    value = ((int) secondL) / 2;
                }
                switch (value) {
                    case 0:
                        LAdd = "" + ((char) (97 + data[1]));
                        break;
                    case 1:
                        LAdd = "" + ((char) (65 + data[3]));
                        break;
                    case 2:
                        LAdd = "" + letters.punList.get((int) data[5]);
                        break;
                    case 3:
                        LAdd = "" + letters.edgeList.get((int) data[7]);
                        break;
                }

            }
            // hard coded stuff
            if(LAdd.equals("!") && !gap(temp.to2D())){
                LAdd="f";
            }
            LAdd = edgeCase(LAdd, temp.to2D(), avgH);
            modifySA(LAdd.charAt(0), row, col);
        }
    }
}
