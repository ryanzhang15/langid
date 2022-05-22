/*
A complex algorithm (designed by me of course) to match an inputted paragraph to data collected from machine learning.
 */

package com.ryanzhang.languageMatcher;

import com.ryanzhang.panic.panic;
import com.ryanzhang.reencoder.reencoder;
import com.ryanzhang.utf8reader.fileReadDevice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class languageMatcher {

    private final String mainPath;
    public String paragraphPath; //public to be changed during testing
    private int n;
    private String[] lang;

    private languageData[] data;
    private fileReadDevice paragraphData;

    //initializer
    public languageMatcher(String initMainPath, String initParagraphPath) throws IOException {
        mainPath = initMainPath;
        paragraphPath = initParagraphPath;
        //scan the directory of files for further processing. This only has to be done once, as languageData can be reused.
        scanLanguageDirectories();
    }

    public String match() throws IOException { //return value is String for testing purposes later
        panic.log("Matching...", "match");
        //first, input the paragraph to be used for matching.
        paragraphData();
        //then, start matching.
        return matchingAlgorithm();
    }

    private void scanLanguageDirectories() throws IOException {

        //open directory
        File langDirectory = new File(mainPath);

        //check if directory exists
        if(!langDirectory.exists()) panic.fatalError("Language data files do not exist. Please run the program again, choosing the option to process language data.");
        if(!langDirectory.isDirectory()) panic.fatalError(mainPath+" is not a directory, as it should be");
        panic.log("Successfully opened "+mainPath, "scanLanguageDirectories");

        //check if info.txt exists
        File infoFile = new File(langDirectory, "info.txt");
        if(!infoFile.exists()) panic.fatalError("Given directory "+mainPath+" does not contain info.txt, as required");
        panic.log("Successfully opened info.txt in "+mainPath, "scanLanguageDirectories");

        //read from info.txt, assuming it is in the right format
        Scanner sc = new Scanner(infoFile);
        //read number of languages
        n = sc.nextInt();
        sc.skip("\n");
        //read the language names into lang
        lang = new String[n];
        for(int i = 0; i < n; ++i) lang[i] = sc.nextLine();

        panic.log("Successfully read in all language names", "scanLanguageDirectories");

        //update languageData array
        data = new languageData[n];

        //for each language, read in the data.
        for(int i = 0; i < n; ++i) {

            //open some files
            panic.log("Processing "+lang[i]+"...", "scanLanguageDirectories");
            File dir = new File(langDirectory, lang[i]);
            if(!dir.exists()) panic.fatalError("Given language "+dir.getAbsolutePath()+" does not exist, as required.");
            File freqData = new File(dir, "a.freqdata");
            if(!freqData.exists()) panic.fatalError("Given language "+dir.getAbsolutePath()+" does not contain a.freqdata, as required.");
            panic.log("Successfully opened a.freqdata in "+dir.getAbsolutePath(), "scanLanguageDirectories");

            //read in some numbers
            sc = new Scanner(freqData);
            int initn = sc.nextInt();
            int initm = sc.nextInt();
            data[i] = new languageData(initn, initm);
            for(int j = 0; j <= data[i].n; ++j) { //read in frequency data
                data[i].a[j] = sc.nextInt();
                data[i].b[j] = sc.nextInt();
            }

            //open more files
            File diData = new File(dir, "a.didata");
            if(!diData.exists()) panic.fatalError("Given language "+dir.getAbsolutePath()+" does not contain a.didata, as required.");
            panic.log("Successfully opened a.didata in "+dir.getAbsolutePath(), "scanLanguageDirectories");
            sc = new Scanner(diData);

            //read in more numbers
            data[i].dic = sc.nextInt();
            for(int j = 0; j <= data[i].n; ++j) for(int k = 0; k <= data[i].n; ++k) data[i].di[j][k] = sc.nextInt();

        }

        //done!
        panic.log("Successfully inputted machine learning data from "+langDirectory, "scanLanguageDirectories");

    }

    private void paragraphData() throws IOException {

        //check that the file exists
        File fl = new File(paragraphPath);
        if(!fl.exists()) panic.fatalError("Given input paragraph "+fl.getAbsolutePath()+" does not exist, as required.");
        panic.log("Successfully opened "+fl.getAbsolutePath(), "paragraphData");

        //warn if the file size is small
        long sz = Files.size(Paths.get(fl.getAbsolutePath()));
        if(sz == 0) panic.fatalError("Text sample from "+fl.getAbsolutePath()+" is empty."); //file is empty
        else if(sz < 350) System.out.println("Warning: Your inputted text file is quite short. The accuracy of the output may be affected. For more accurate results, try a longer sample."); //text file size small, 350 bytes is from experimentation

        //get a fileReadDevice to process the file
        paragraphData = new fileReadDevice(paragraphPath);
        paragraphData.retrieve(); // get content

    }

    private String matchingAlgorithm() {

        //step 1: select languages based off of letter frequency, giving them a score.
        double[] fq = new double[n];
        double[] di = new double[n];
        Integer[] rk = new Integer[n]; //for storing rank of frequencies
        for(int i = 0; i < n; ++i) rk[i] = i; //initialize rk
        for(int i = 0; i < n; ++i) fq[i] = freqMatching(i); //get the frequency score
//        for(int i = 0; i < n; ++i) fq[i] *= diMatching(i); //get the digraph score

        //sort rank based on frequency
        Arrays.sort(rk, (a, b) -> (int)(fq[a]-fq[b])); //uses cool Java 8 function lambdas, I'm so cool

        int pointer = 0; //keep a pointer, such that languages 0~pointer will be considered
        while(pointer < n-1 && fq[rk[pointer+1]]/fq[rk[0]] < 129) ++pointer; //we will take a difference of a certain ratio
        if(pointer == 0) { //only a single language to consider
            panic.log("Decision made from letter frequency, making guess...", "matchingAlgorithm");
            return match(lang[rk[0]]);
        }
        for(int i = 0; i <= pointer; ++i) di[rk[i]] = diMatching(rk[i]);
        int min = 0;
        for(int i = 1; i <= pointer; ++i) if(di[rk[i]] < di[rk[min]]) min = i;
        return match(lang[rk[min]]);
    }

    //match from one-letter frequencies.
    private double freqMatching(int dx) {
        ArrayList<Integer> a = new ArrayList<>(paragraphData.a);
        reencoder rc = new reencoder(data[dx]); //based on language data for language dx, encode
        rc.rEncode(a);
        int[] fq = new int[data[dx].n+1]; //find out the new frequencies
        int noMatch = 0;
        for(int i : a) if(i != -1) ++fq[i];
        else ++noMatch;
        double score = 0, pw = 1.84989; //pw is just a parameter
        for(int i = 0; i <= data[dx].n; ++i) score += Math.pow(pw, Math.abs((double)1000*data[dx].b[i]/(data[dx].m)-(double)1000*fq[i]/(a.size()))); //my "complicated" formula
        score += Math.pow(pw, (double)1374*noMatch/a.size()); //these strange numbers are parameters
//        panic.debug("Language is "+lang[dx]+", score is "+score);
        return score;
    }

    //match from two-letter frequencies, or digraphs
    private double diMatching(int dx) {
        ArrayList<Integer> a = new ArrayList<>(paragraphData.a);
        reencoder rc = new reencoder(data[dx]); //based on language data for language dx, encode
        rc.rEncode(a); //I love using classes, a feature much more convenient in Java. Being able to do this is great
        int[][] fq = new int[data[dx].n+1][data[dx].n+1];
        int dic = 0; //dic is digraph count
        int noMatch = 0; //letter does not appear in language data
        for(int i = 0; i+1 < a.size(); ++i) {
            ++dic;
            if (a.get(i) != -1 && a.get(i + 1) != -1) ++fq[a.get(i)][a.get(i + 1)];
            else ++noMatch;
        }
        double score = 0, pw = 1.95808; //pw is just a parameter
        for(int i = 0; i <= data[dx].n; ++i) for(int j = 0; j <= data[dx].n; ++j) score += Math.pow(pw, Math.abs((double)680*data[dx].di[i][j]/(data[dx].dic)-(double)680*fq[i][j]/dic)); //my "complicated" formula
        score += Math.pow(pw, (double)873*noMatch/dic)*a.size(); //these strange numbers are parameters
        panic.debug("Language is "+lang[dx]+", score is "+score);
        return score;
    }

    private String noMatch() {
        System.out.println("Sorry, we were unable to match your inputted paragraph with a language.");
        return "No Match";
    }

    private String match(String l) {
        System.out.println("Your inputted paragraph is in the language: "+l+".");
        return l;
    }

}
