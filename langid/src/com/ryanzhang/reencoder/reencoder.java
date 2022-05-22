/*
A helper class, holding methods to read in letter frequency data and an ArrayList, then re-encode the letters based on
their frequency, limiting the maximum encoded value to 2000.
 */

package com.ryanzhang.reencoder;

import com.ryanzhang.languageMatcher.languageData;

import java.util.ArrayList;

public class reencoder {

    private final languageData data;

    //initializer
    public reencoder(languageData init) {
        data = init;
    }

    public void rEncode(ArrayList<Integer> a) { //re-encode arraylist from languageData
        int max = -1; //max value in UTF-8 encoding
        for(int i : data.a) max = Math.max(max, i);
        int[] map = new int[max+1];
        for(int i = 0; i <= data.n; ++i) map[data.a[i]] = i+1; //create map in the form of an array
        for(int i = 0; i < a.size(); ++i) {
            int vl = a.get(i);
            if(vl != -1 && vl <= max) a.set(i, map[vl]-1);
            else a.set(i, -1);
        }
    }
}


