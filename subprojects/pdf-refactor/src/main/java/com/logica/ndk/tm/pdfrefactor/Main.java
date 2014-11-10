package com.logica.ndk.tm.pdfrefactor;

import java.io.IOException;



/**
 * Milos
 *
 */
public class Main {
    
//    final static String STORAGE = "C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\PDF\\";
    
    public static void main( String[] args ) throws IOException
    {
       System.out.println("Path: "+args[0]);
       Refactor refactor = new Refactor();
       int count = refactor.execute(args[0]);
       System.out.println("Refactored "+count+" files.");
    }
}
