package com.logica.ndk.tm.validation.printer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.logica.ndk.tm.utilities.em.ValidationViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomas Mriz (Logica)
 */
public class FilePrinter extends AbstractPrinter {

    protected final transient Logger logger = LoggerFactory.getLogger(
            FilePrinter.class);
    private String destination;
    private boolean append;
    private String prefix;

    public FilePrinter(String destination,
            List<ValidationViolation> violations, boolean append,
            String prefix) {
        super(violations);
        if (destination == null || destination.isEmpty()) {
            throw new NullPointerException("destination cannot be empty");
        }
        this.destination = destination;
        this.append = append;
        this.prefix = prefix;
    }

    @Override
    public void printErrors() {
        checkDestination();
        printToFile();
    }

    private void printToFile() {
        if (violations != null) {
            FileWriter fos = null;
            try {
                File outputFile = createOutputFile();
                fos = new FileWriter(outputFile, append);
                BufferedWriter bos = new BufferedWriter(fos);
                if (violations.size() > 0) {
                    bos.write("Processing PSP in directory " + prefix + " " + violations.size() + " ERRORS!");
                    bos.newLine();
                    bos.flush();
                    for (ValidationViolation violation : violations) {
                        bos.write("ERROR: Violation type: " + violation.getViolationCode());
                        bos.write(". Violation message: "
                                + violation.getViolationDescription());
                        bos.newLine();
                        bos.flush();
                    }
                }else{
                    bos.write("Processing PSP in directory " + prefix + " OK!");
                    bos.newLine();
                    bos.flush();
                }

            } catch (IOException e) {
                logger.error("Unable to write output.", e);
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("Unable to close output file");
                }
            }
        }
    }

    private void checkDestination() {
        File log = new File(destination);
        if (!log.getParentFile().exists()) {
            logger.info(String.format(
                    "Destination directory: %s not found. Creating "
                    + "all directories in path.", destination));
            log.getParentFile().mkdirs();
        }
    }

    private File createOutputFile() throws IOException {
        File tempFile = new File(destination);
        File outputFile = tempFile;

        if (prefix != null) {
            String outputFileName = prefix + "_" + tempFile.getName();
            outputFile = new File(tempFile.getParentFile(), outputFileName);
        }

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        return outputFile;
    }
}
