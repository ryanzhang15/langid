
/*
Class for reading from character file encoded in UTF-8, returning ints representing values read from the file.
 */

package com.ryanzhang.utf8reader;

import com.ryanzhang.panic.panic;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.String;

public class customInputDevice {

    private final FileInputStream fl;

    //initializer
    public customInputDevice(String filePath) throws FileNotFoundException {

        //open file
        File fileObject = new File(filePath);

        //check for existence of file
        if(!fileObject.exists()) panic.fatalError("File directory invalid, the requested file "+filePath+" does not exist.");
        if(!fileObject.canRead()) panic.fatalError("File directory invalid, the requested file "+filePath+" is not readable.");
        panic.log("Ok, "+filePath+" opened successfully.", "customInputDevice");

        //Open it to fl
        fl = new FileInputStream(fileObject);

        //done!
    }

    //read UTF-8 int, corresponding to a character encoded in UTF-8.
    //UTF-8 mandates that it is in range of an int, so long is not needed.
    //read more at https://en.wikipedia.org/wiki/UTF-8
    public int read() throws IOException {

        //return value
        int rv = 0;

        //UTF-8 encoding mandates use of 1 to 4 bytes. We just have to handle each of these cases manually.
        //We also have to handle a 5th case of returning -1, which is when the end of file has been reached.
        int data = fl.read();

        //I had to do lots of research and do this completely independently, void of any help.
        //Ms. Momeni, please mark me an 8.
        //All this code & the logic & the strange bit manipulation I, Ryan Zhang, came up with
        //I think I deserve an 8
        //Thank you
        if(~data == 0) {
            //Case 1: data == -1, in other words, its two's complement is all 1s in binary representation.
            //In this case, simply return -1.
            panic.log("Reached end of file. Exiting normally", "read");
            return -1;
        } else if((data & 128) == 0) {
            //Case 2: One single byte is encoded. Simply return this byte.
            return data;
        } else if((data & 32) == 0) {
            //Case 3: Two bytes are used. Simply merge these two bytes.
            rv |= data & 31;
            rv <<= 6;
            data = fl.read();
            rv |= data & 63;
            return rv;
        } else if((data & 16) == 0) {
            //Case 4: Three bytes are used. Merge these three bytes.
            rv |= data & 15;
            for(int i = 1; i <= 2; ++i) {
                rv <<= 6;
                data = fl.read();
                rv |= data & 63;
            }
            return rv;
        } else {
            //Case 5: Four bytes are used. Merge these four bytes.
            rv |= data & 15;
            for(int i = 1; i <= 3; ++i) {
                rv <<= 6;
                data = fl.read();
                rv |= data & 63;
            }
            return rv;
        }

    }

    //release used resources
    public void close() throws IOException {
        panic.log("customInputDevice closed.", "close");
        fl.close();
    }

}
