package com.logica.ndk.tm.validation.utils;

import java.io.*;

/**
 * @author Tomas Mriz (Logica)
 */
public class Utils {

    public static byte[] loadMessage(String s)
        throws IOException
    {
        File file = new File(s);
        if(!file.exists())
            throw new IOException("File : " + s + " does not exist.");
        RandomAccessFile randomaccessfile = new RandomAccessFile(s, "r");
        byte abyte0[] = new byte[(int)randomaccessfile.length()];
        try {
            randomaccessfile.read(abyte0);
        } catch(IOException ioexception)
        {
            if(randomaccessfile != null)
                randomaccessfile.close();
            throw ioexception;
        }
        randomaccessfile.close();
        return abyte0;
    }
}
