/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.pdfrefactor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

/**
 *
 * @author kovalcikm
 */
public class Refactor {

    public int execute(String path) throws IOException {
        int count = 0;
        boolean succ = false;

        File source = new File(path);

        if (!source.exists()) {
            System.out.println(source.getAbsolutePath() + " does not exist. Refactoring aborted!");
            return 0;
        }
        File statusFile = new File(source.getAbsolutePath() + File.separator + "status.txt");

        FileUtils.write(statusFile, "Not Refactored CDMs:\n");
        FileFilter filter = new PrefixFileFilter("CDM_");

        int toRefactorCount = source.listFiles(filter).length;
        System.out.println("Folders to refactor:" + toRefactorCount);
        System.out.println("");
        File[] listFiles = source.listFiles(filter);

        String uuid;
        String hashedCdmId;
        String firstLevelFolderName;
        String secondLevelFolderName;
        String thirdLevelFolderName;
        File targetDir;
        for (File cdmFolder : listFiles) {
            uuid = cdmFolder.getName().substring(4);
            hashedCdmId = DigestUtils.md5Hex(uuid);
            firstLevelFolderName = hashedCdmId.substring(0, 2); //first three from hashedCdmId
            secondLevelFolderName = hashedCdmId.substring(2, 4); //second three from hashedCdmId
            thirdLevelFolderName = hashedCdmId.substring(hashedCdmId.length() - 2, hashedCdmId.length()); //last three from hashedCdmId

            targetDir = new File(path + File.separator + firstLevelFolderName + File.separator + secondLevelFolderName + File.separator + thirdLevelFolderName + File.separator + uuid);
            System.out.println("CDM: " + cdmFolder.getName() + " copying to " + targetDir.getPath());
            for (File f : cdmFolder.listFiles()) {
                for (int i = 0; i < 3; i++) {
                    try {
                        System.out.println("Copying file: " + f.getPath() + " to " + targetDir.getPath() + ". Time: " + System.currentTimeMillis());
                        FileUtils.copyFileToDirectory(f, targetDir);
                        if (FileUtils.sizeOf(f) == 0) {
                            throw new Exception("Zero size.");
                        }
                        System.out.println("Copying finished.");
                        succ = true;
                        break;
                    } catch (Exception e) {
                        if (i == 2) {
                            System.out.println("It was unable to copy file " + f.getPath() + " to " + targetDir.getPath() + " or file has size 0.");
                            succ = false;
                            break;
                        }
                        System.out.println("Exception in copying file: " + f.getAbsolutePath() + " Trying again.");
                    }
                }
            }
            if (succ == true) {
                count++;
                System.out.println(cdmFolder.getName() + " succesfully refactored.");
                System.out.println(count + "/" + toRefactorCount);
                System.out.println("");
                succ = false;
            } else {
                FileUtils.write(statusFile, "Refactor not succesfull for: " + cdmFolder.getName() + "\n");
                System.out.println("Refactor not succesfull for: " + cdmFolder.getName());
            }
        }
        
        System.out.println(count + " refactored.");
        FileUtils.write(statusFile, "Process finished!");
        return count;
    }
}
