package com.ryanzhang;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.ryanzhang.languageMatcher.languageMatcher;
import com.ryanzhang.machineLearner.machineLearner;
import com.ryanzhang.panic.panic;
import com.ryanzhang.utf8reader.customInputDevice;

import javax.sound.midi.SysexMessage;

public class Main {

    static Scanner sc;

    public static void main(String[] args) throws IOException {

        sc = new Scanner(System.in); //initialize scanner
        promptAndRun(); //run as usual

//        test(); //do testing to see how accurate my algorithm is, for Ryan's use only

        System.out.println("\n\nExiting..."); // message to make sure run was successful
        sc.close();

    }
 
    static void test() throws IOException { //run program against many, many test samples (for Ryan's use only)
        String a = "/Users/ryanzhang/Desktop/here/langIdData", b = "/Users/ryanzhang/Documents/GitHub/testdata";
        final int sampleCount = 1000;
        int wrong = 0;
        languageMatcher lm = new languageMatcher(a, "");
        for(int i = 1; i <= sampleCount; ++i) {
            lm.paragraphPath = b+"/"+i+".txt";
            String x = lm.match(); //my algorithm matching result
            Scanner sc = new Scanner(new File(b+"/"+i+".ans"));
            String y = sc.nextLine(); //correct answer
            if(!x.equals(y)) {
                System.out.println("Failed test "+i+", expected "+y+", found "+x);
                ++wrong;
            } else System.out.println("Good, identified "+x);
        }
        System.out.println();
        System.out.println();
        System.out.println("Total "+(sampleCount-wrong)+" out of "+sampleCount+" right, accuracy "+(double)(sampleCount-wrong)/sampleCount*100+"%");
    }

    static void promptAndRun() throws IOException { //the main function to prompt and run program, for user use
        //welcome prompt
        System.out.println("\nWelcome to langid, a program to identify the language of text samples!");
        System.out.println("Created by Ryan Zhang.\n\n");

        //choose between two options
        System.out.println("Which mode would you like to run the program in? Please type in the respective number, and press enter.\n");
        System.out.println("[1] Language Identification. The program will guess what language a text sample is in.");
        System.out.println("[2] Process Language Data. The program will read in a training packet, and save this language data for language identification later. (See docs: https://docs.google.com/document/d/1cvP3L6FXvW7cxBLKZgRqQQtcNERisW0DjeMR1g8Ych8/edit?usp=sharing)");

        //allow some input, rejecting unwanted input
        char x;
        while(true) { //we will break this loop later
            String rd = sc.nextLine();
            x = rd.isEmpty() ? 0 : rd.charAt(0); //extract one integer
            if(x != '1' && x != '2') panic.error("Please input either 1 or 2."); //reject input
            else break; //accept input
        }
        if(x == '1') matchLanguage(); //mode 1, language matching
        else processData(); //mode 2, data processing
    }

    static void processData() throws IOException {
        System.out.println("\nInput the filepath of the language data package (please include the full filepath):"); //prompt
        String dataPath; //for inputting
        do {
            dataPath = sc.nextLine();
            dataPath = dataPath.trim();
        } while (dataPath.isEmpty()); //ignore empty lines, break only when the line is not empty

        //leave the rest to the machineLearner class
        System.out.println("Received, crunching numbers...\n"); //assure the user that the program is working
        machineLearner ml = new machineLearner(dataPath);
        ml.learn();
        ml.outputData("src/com/ryanzhang"); //save data within working directory, for convenience
        System.out.println("Successfully generated and saved language data! You may now use this program in language identification mode, where it will use this new data to match languages.");
    }

    static void matchLanguage() throws IOException {
        System.out.println("\nInput the filepath of the text sample (in a plain text file, encoded with UTF-8 (See docs: https://docs.google.com/document/d/1cvP3L6FXvW7cxBLKZgRqQQtcNERisW0DjeMR1g8Ych8/edit?usp=sharing):"); //prompt
        String dataPath; //for inputting
        do {
            dataPath = sc.nextLine();
            dataPath = dataPath.trim();
        } while (dataPath.isEmpty()); //ignore empty lines, break only when the line is not empty


        //leave the rest to languageMatcher class
        System.out.println("Received, crunching numbers...\n"); //assure the user that the program is working
        languageMatcher lm = new languageMatcher("src/com/ryanzhang/langIdData", dataPath);
        lm.match(); //exciting!
    }

}
