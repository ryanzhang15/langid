/*
A class to read the contents of a file, then save it into an array, converting UTF-8 encoded chars to ints.
Use: initialize with file path, then, call retrieve() to put contents into ArrayList a.
 */

package com.ryanzhang.utf8reader;

import com.ryanzhang.panic.panic;

import java.io.IOException;
import java.lang.Integer;
import java.util.ArrayList;

public class fileReadDevice {

    public ArrayList<Integer> a; //for storing data of file
    private final customInputDevice in; //input device

    //initializer
    public fileReadDevice(String initFilePath) throws IOException {

        in = new customInputDevice(initFilePath); //customInputDevice already handles errors, no error handling needed
        a = new ArrayList<>(0);

        panic.log("Read device set up successfully.", "fileReadDevice");

    }

    //retrieve contents
    public void retrieve() throws IOException {

        for (int i = in.read(); i != -1; i = in.read()) { //continually read integers until end of file

            //we must ignore the digits 1~9, as well as '[' and ']', as these come in abundance from footnotes from
            //wikipedia articles, where some language samples are taken from.
            if(i >= 48 && i <= 57) continue; //is a number
            if(i == 91 || i == 93) continue; //is one of []

            //we must also remove line breaks and spaces, compressing these into a single space in order to filter out
            //formatting issues, which are unrelated to specific language but are instead specific to the language
            //samples themselves.
            if(i == 10 || i == 13 || i == 9) i = 32; //convert \n, \r and \t to a space
            if(!a.isEmpty() && a.get(a.size()-1) == 32 && i == 32) continue; //only add space if the last character was not already a space

            a.add(i);
        }
        if(a.get(a.size()-1) == 32) a.remove(a.size()-1); //remove trailing spaces, just in case this affects learning
        a.trimToSize(); //possibly save some memory?????

        //done
        panic.log("Contents read successfully.", "retrieve");

    }

    //close
    public void close() throws IOException {
        in.close();
    }

}
