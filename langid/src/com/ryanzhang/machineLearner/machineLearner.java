/*
Class for machine learning.
This will process the directory of languages, and output some data based on these languages for language identification
later on.
 */

package com.ryanzhang.machineLearner;

import com.ryanzhang.languageMatcher.languageData;
import com.ryanzhang.panic.panic;
import com.ryanzhang.reencoder.reencoder;
import com.ryanzhang.utf8reader.fileReadDevice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;
import java.lang.Math;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class machineLearner {

    private final String mainPath;
    private int n;
    private String[] lang;

    private languageData[] data;

    //initializer
    public machineLearner(String initMainPath) {
        mainPath = initMainPath;
    }

    public void learn() throws IOException {
        //First, scan the list of languages for further processing. The rest is handled in that function
        scanLanguageDirectories();
    }

    private void scanLanguageDirectories() throws IOException {

        //open directory
        File langDirectory = new File(mainPath);

        //check if directory exists
        if(!langDirectory.exists()) panic.fatalError("Given directory "+mainPath+" does not exist");
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

        //we can close the scanner, may as well save some resources
        sc.close();

        //update languageData array
        data = new languageData[n];

        //for every language, we should further process it
        for(int i = 0; i < n; ++i) {
            panic.log("Processing "+lang[i]+"...", "scanLanguageDirectories");
            processLanguage(new File(langDirectory, lang[i]), i);
            panic.log("Successfully processed language "+lang[i]+"!!!", "scanLanguageDirectories");
        }

        //done
        panic.log("Successfully processed all "+n+" languages. Exiting.", "scanLanguageDirectories");

    }

    //gather machine learning data for an individual language
    private void processLanguage(File dir, int dx) throws IOException { //dir is directory of language samples, dx is language index

        int sampleCount; //number of samples

        //infoFile should contain number of files in this directory
        File infoFile = new File(dir, "info.txt");
        if(!infoFile.exists()) panic.fatalError("Given language "+dir.getAbsolutePath()+" does not contain info.txt, as required");
        panic.log("Successfully opened info.txt in "+dir.getAbsolutePath(), "processLanguage");

        //read in number of samples for this language
        Scanner sc = new Scanner(infoFile);
        sampleCount = sc.nextInt();
        sc.close();

        fileReadDevice[] fl = new fileReadDevice[sampleCount]; //fl is an array, holding the language samples.
        for(int i = 0; i < sampleCount; ++i) {
            fl[i] = new fileReadDevice((new File(dir, i+1+".txt")).getAbsolutePath()); //load in the language sample
            fl[i].retrieve(); //get contents
        }

        //first, we will need to re-encode the letters for this language, keeping only the most popular 2000 characters.
        //we will need an array to use as a map, to map characters to their frequency.
        //to do this, we need to find out the array size, which is the maximum encoded character.
        //utf-8 guarantees this is less than 2 to the power of 22, which even the worst mac has enough memory for.

        int max = -1; //will hold maximum value for encoded character
        for(fileReadDevice i : fl) max = Math.max(max, Collections.max(i.a));
        ++max; //increment max so that the maximum value can be stored in an index

        Integer[] freq = new Integer[max]; //frequency of letters
        Integer[] rk = new Integer[max]; //rank of frequency of letters, to be used later

        for(int i = 0; i < max; ++i) { //initialize rank and freq array
            rk[i] = i;
            freq[i] = 0;
        }
        for(fileReadDevice i : fl) for(int j : i.a) ++freq[j]; //update frequency of all letters in all samples

        //custom function lambda to sort rk based on frequency
        Arrays.sort(rk, (a, b) ->
             //here, rk holds indexes. So, we compare indexes
             freq[b]-freq[a]
        );

        int pointer = 0, letterSum = 0;
        //increase pointer until it reaches 2000, or there are no more letters left
        while(pointer < Math.min(2000, max-2) && freq[rk[pointer+1]] > 0) ++pointer;
        //go through the language samples, and add up the amount of letters
        for(fileReadDevice i : fl) letterSum += i.a.size();

        //initialize languageData
        data[dx] = new languageData(pointer, letterSum);

        //save this letter frequency data into data array
        for(int i = 0; i <= pointer; ++i) {
            data[dx].a[i] = rk[i];
            data[dx].b[i] = freq[rk[i]];
        }


        panic.log("Beginning re-encoding...", "processLanguage");

        //re-encoding!
        reencoder rec = new reencoder(data[dx]);
        for(fileReadDevice i : fl) {
            rec.rEncode(i.a);
            //tally up two-letter sequences
            for(int j = 0; j+1 < i.a.size(); ++j) {
                ++data[dx].dic;
                ++data[dx].di[i.a.get(j)][i.a.get(j+1)];
            }
        }

        //finished letter and digraph frequency

        panic.log("Successfully completed letter and digraph frequency computation!", "processLanguage");

    }

    //print out the gathered language data into a directory
    public void outputData(String dirPath) throws IOException {
        File mainDir = new File(dirPath);
        if(!mainDir.exists()) panic.fatalError("Given directory "+mainDir.getAbsolutePath()+" does not exist.");
        panic.log("Successfully opened "+mainDir.getAbsolutePath(), "printData");
        mainDir = new File(dirPath, "langIdData");
        mainDir.mkdir();

        //print out an info.txt file, containing number of languages as well as language names.
        File infoFile = new File(mainDir, "info.txt");
        infoFile.createNewFile();
        FileWriter op = new FileWriter(infoFile);
        op.write(n+"\n");
        for(String i : lang) op.write(i+"\n");
        op.flush();
        panic.log("Wrote to "+infoFile.getAbsolutePath(), "printData");

        //for each language, print its data.
        for(int i = 0; i < n; ++i) {
            //create mother directory
            File langDir = new File(mainDir, lang[i]);
            langDir.mkdir();

            //create frequency data file
            File freqData = new File(langDir, "a.freqdata");
            freqData.createNewFile();

            //output frequency data
            op = new FileWriter(freqData);
            op.write(data[i].n+" "+data[i].m+"\n");
            for(int j = 0; j <= data[i].n; ++j) op.write(data[i].a[j]+" "+data[i].b[j]+"\n");
            op.flush();

            //create digraph file
            File diData = new File(langDir, "a.didata");
            diData.createNewFile();

            //output digraph data
            op = new FileWriter(diData);
            op.write(data[i].dic+"\n");
            for(int j = 0; j <= data[i].n; ++j) {
                for(int k = 0; k <= data[i].n; ++k) op.write(data[i].di[j][k]+" ");
                op.write("\n");
            }
            op.flush();

        }

        //done
        op.close();
        panic.log("Successfully outputted language data!", "printData");

    }

}
