/*
A helper class, storing language data. For use between machineLearner and languageMatcher.
 */

package com.ryanzhang.languageMatcher;

public class languageData {

    public int n; //number of characters this language has, maximum 2000
    public int m; //total number of letters collected in samples, for use with ratios later
    public int[] a, b; //two arrays, showing letter encoding in UTF-8 and its frequency

    public int dic; //digraph, or two-letter sequences count
    public int[][] di; //two-dimensional array for digraphs, or two letter sequences

    //initializer
    public languageData(int initN, int initM) {
        n = initN;
        m = initM;
        a = new int[n+1];
        b = new int[n+1];
        dic = 0;
        di = new int[n+1][n+1];
    }

}
