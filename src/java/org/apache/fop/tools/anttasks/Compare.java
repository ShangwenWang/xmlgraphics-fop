/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.tools.anttasks;

import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tools.ant.BuildException;
import java.text.DateFormat;

/**
 * This class is an extension of Ant, a script utility from
 * http://ant.apache.org.
 * It provides methods to compare two files.
 */

public class Compare {
    
    private static final boolean IDENTICAL_FILES = true;
    private static final boolean NOTIDENTICAL_FILES = false;

    private String referenceDirectory, testDirectory;
    private String[] filenameList;
    private String filenames;
    private BufferedInputStream oldfileInput;
    private BufferedInputStream newfileInput;

    /**
     * Sets directory for test files.
     * @param testDirectory the test directory
     */
    public void setTestDirectory(String testDirectory) {
        if (!(testDirectory.endsWith("/") | testDirectory.endsWith("\\"))) {
            testDirectory += File.separator;
        }
        this.testDirectory = testDirectory;
    }

    /**
     * Sets directory for reference files.
     * @param referenceDirectory the reference directory
     */
    public void setReferenceDirectory(String referenceDirectory) {
        if (!(referenceDirectory.endsWith("/")
                | referenceDirectory.endsWith("\\"))) {
            referenceDirectory += File.separator;
        }
        this.referenceDirectory = referenceDirectory;
    }

    /**
     * Sets the comma-separated list of files to process.
     * @param filenames list of files, comma-separated
     */
    public void setFilenames(String filenames) {
        StringTokenizer tokens = new StringTokenizer(filenames, ",");
        List filenameListTmp = new java.util.ArrayList(20);
        while (tokens.hasMoreTokens()) {
            filenameListTmp.add(tokens.nextToken());
        }
        filenameList = new String[filenameListTmp.size()];
        filenameList = (String[])filenameListTmp.toArray(new String[0]);
    }

    private boolean compareBytes(File oldFile, File newFile) {
        try {
            oldfileInput =
                new BufferedInputStream(new java.io.FileInputStream(oldFile));
            newfileInput =
                new BufferedInputStream(new java.io.FileInputStream(newFile));
            int charactO = 0;
            int charactN = 0;
            boolean identical = true;

            while (identical & (charactO != -1)) {
                if (charactO == charactN) {
                    charactO = oldfileInput.read();
                    charactN = newfileInput.read();
                } else {
                    return NOTIDENTICAL_FILES;
                }
            }
            return IDENTICAL_FILES;
        } catch (IOException io) {
            System.err.println("Task Compare - Error: \n" + io.toString());
        }
        return NOTIDENTICAL_FILES;
    }

    private boolean compareFileSize(File oldFile, File newFile) {
        if (oldFile.length() != newFile.length()) {
            return NOTIDENTICAL_FILES;
        } else {
            return IDENTICAL_FILES;
        }
    }    // end: compareBytes

    private boolean filesExist(File oldFile, File newFile) {
        if (!oldFile.exists()) {
            System.err.println("Task Compare - ERROR: File "
                               + referenceDirectory + oldFile.getName()
                               + " doesn't exist!");
            return false;
        } else if (!newFile.exists()) {
            System.err.println("Task Compare - ERROR: File " + testDirectory
                               + newFile.getName() + " doesn't exist!");
            return false;
        } else {
            return true;
        }
    }

    private void writeHeader(PrintWriter results) {
        String dateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.MEDIUM).format(new Date());
        results.println("<html><head><title>Test Results</title></head><body>\n");
        results.println("<h2>Compare Results<br>");
        results.println("<font size='1'>created " + dateTime
                        + "</font></h2>");
        results.println("<table cellpadding='10' border='2'><thead>"
                        + "<th align='center'>reference file</th>"
                        + "<th align='center'>test file</th>"
                        + "<th align='center'>identical?</th></thead>");


    }

    /**
     * Main method of task compare
     * @throws BuildException If the execution fails.
     */
    public void execute() throws BuildException {
        boolean identical = false;
        File oldFile;
        File newFile;
        try {
            PrintWriter results =
                new PrintWriter(new java.io.FileWriter("results.html"), true);
            this.writeHeader(results);
            for (int i = 0; i < filenameList.length; i++) {
                oldFile = new File(referenceDirectory + filenameList[i]);
                newFile = new File(testDirectory + filenameList[i]);
                if (filesExist(oldFile, newFile)) {
                    identical = compareFileSize(oldFile, newFile);
                    if (identical) {
                        identical = compareBytes(oldFile, newFile);
                    }
                    if (!identical) {
                        System.out.println("Task Compare: \nFiles "
                                           + referenceDirectory
                                           + oldFile.getName() + " - "
                                           + testDirectory
                                           + newFile.getName()
                                           + " are *not* identical.");
                        results.println("<tr><td><a href='"
                                        + referenceDirectory
                                        + oldFile.getName() + "'>"
                                        + oldFile.getName()
                                        + "</a> </td><td> <a href='"
                                        + testDirectory + newFile.getName()
                                        + "'>" + newFile.getName() + "</a>"
                                        + " </td><td><font color='red'>No</font></td></tr>");
                    } else {
                        results.println("<tr><td><a href='"
                                        + referenceDirectory
                                        + oldFile.getName() + "'>"
                                        + oldFile.getName()
                                        + "</a> </td><td> <a href='"
                                        + testDirectory + newFile.getName()
                                        + "'>" + newFile.getName() + "</a>"
                                        + " </td><td>Yes</td></tr>");
                    }
                }
            }
            results.println("</table></html>");
        } catch (IOException ioe) {
            System.err.println("ERROR: " + ioe);
        }
    }    // end: execute()

}

